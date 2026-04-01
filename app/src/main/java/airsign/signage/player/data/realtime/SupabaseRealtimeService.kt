package airsign.signage.player.data.realtime

import airsign.signage.player.BuildConfig
import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseRealtimeService @Inject constructor(
    private val supabaseClient: SupabaseClient
) {

    companion object {
        private const val TAG = "SupabaseRealtimeService"
        private const val DEVICES_TABLE = "device_sync_state"
        private const val SCHEMA_PUBLIC = "public"
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _deviceUpdates = MutableSharedFlow<String>(
        replay = 0, extraBufferCapacity = 1
    )
    val deviceUpdates: Flow<String> = _deviceUpdates.asSharedFlow()

    private var activeChannel: RealtimeChannel? = null
    private var subscriptionJob: Job? = null

    fun subscribe(deviceId: String) {
        serviceScope.launch {
            unsubscribeInternal()

            try {
                Log.i(TAG, "Subscribing to realtime updates for deviceId=$deviceId")
                val channel = supabaseClient.realtime.channel("device:$deviceId")

                // Listen to any changes (Insert/Update) to ensure we don't miss Upserts
                subscriptionJob =
                    channel.postgresChangeFlow<PostgresAction>(schema = SCHEMA_PUBLIC) {
                        table = DEVICES_TABLE
                        filter = "device_id=eq.$deviceId"
                    }.onEach { action ->
                        Log.d(TAG, "Realtime activity detected: ${action.javaClass.simpleName} for $deviceId")
                        _deviceUpdates.emit(deviceId)
                    }.catch { error ->
                        Log.e(TAG, "Realtime flow error for $deviceId", error)
                    }.launchIn(serviceScope)

                channel.subscribe()
                activeChannel = channel

                // Monitor channel status for debugging
                channel.status.onEach { status ->
                    Log.d(TAG, "Channel status for $deviceId: $status")
                }.launchIn(serviceScope)

                Log.i(TAG, "Realtime subscription initiated for deviceId=$deviceId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to subscribe to realtime updates for deviceId=$deviceId", e)
            }
        }
    }

    fun unsubscribe() {
        serviceScope.launch { unsubscribeInternal() }
    }

    private suspend fun unsubscribeInternal() {
        try {
            subscriptionJob?.cancel()
            subscriptionJob = null

            activeChannel?.unsubscribe()
            activeChannel = null

            Log.i(TAG, "Realtime subscription cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "Error while unsubscribing from realtime", e)
        }
    }

    fun close() {
        serviceScope.launch {
            try {
                unsubscribeInternal()
                supabaseClient.realtime.close()
                Log.i(TAG, "Realtime client closed")
            } catch (e: Exception) {
                Log.e(TAG, "Error closing realtime client", e)
            }
        }
    }
}