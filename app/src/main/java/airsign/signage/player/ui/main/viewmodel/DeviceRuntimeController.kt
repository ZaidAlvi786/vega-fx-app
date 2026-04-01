package airsign.signage.player.ui.main.viewmodel

import airsign.signage.player.BuildConfig
import airsign.signage.player.data.realtime.SupabaseDeviceRepository
import airsign.signage.player.data.realtime.SupabaseRealtimeService
import airsign.signage.player.data.remote.ApiResult
import airsign.signage.player.domain.repository.DeviceRepository
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRuntimeController @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val realtimeService: SupabaseRealtimeService,
    private val supabaseDeviceRepository: SupabaseDeviceRepository
) {
    companion object {
        private const val HEARTBEAT_INTERVAL_MS = 30_000L
        private const val HEARTBEAT_RETRY_BASE_MS = 5_000L
        private const val HEARTBEAT_RETRY_MAX_MS = 60_000L
    }

    private var heartbeatJob: Job? = null
    private var realtimeJob: Job? = null

    private  val TAG = "DeviceRuntimeController"

    fun start(
        scope: CoroutineScope,
        deviceId: String,
        onPlaylistUpdate: suspend () -> Unit,
        onUnauthorized: () -> Unit
    ) {
        startHeartbeat(scope, deviceId, onUnauthorized)
        startRealtime(scope, deviceId, onPlaylistUpdate)
    }

    fun stop() {
        heartbeatJob?.cancel()
        realtimeJob?.cancel()
        realtimeService.unsubscribe()
    }

    private fun startHeartbeat(
        scope: CoroutineScope,
        deviceId: String,
        onUnauthorized: () -> Unit
    ) {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch(Dispatchers.IO) {
            var retryDelayMs = HEARTBEAT_RETRY_BASE_MS
            while (isActive) {
                Log.d(TAG, "sending heartbeat !!!.")
                when (deviceRepository.sendHeartbeat(
                    deviceId,
                    BuildConfig.VERSION_NAME,
                    Build.VERSION.SDK_INT.toString()
                )) {
                    is ApiResult.Unauthorized -> {
                        onUnauthorized()
                        return@launch
                    }

                    is ApiResult.Failure -> {
                        Log.d(TAG, "heart beat failed ...!")
                        delay(retryDelayMs)
                        retryDelayMs = (retryDelayMs * 2).coerceAtMost(HEARTBEAT_RETRY_MAX_MS)
                        continue
                    }

                    is ApiResult.Success -> {
                        Log.d(TAG, "heartbeat success ...!")
                        retryDelayMs = HEARTBEAT_RETRY_BASE_MS
                    }

                    ApiResult.NotFound -> {
                        onUnauthorized()
                        return@launch
                    }
                }
                delay(HEARTBEAT_INTERVAL_MS)
            }
        }
    }

    private fun startRealtime(
        scope: CoroutineScope,
        deviceId: String,
        onPlaylistUpdate: suspend () -> Unit
    ) {
        scope.launch {
            runCatching {
                ensureDeviceRegistered(deviceId)
            }.onSuccess {
                startDeviceSubscription(deviceId, onPlaylistUpdate, scope)
            }.onFailure { throwable ->
                Log.e(TAG, "Realtime initialization failed for deviceId=$deviceId", throwable)
            }
        }
    }

    private suspend fun ensureDeviceRegistered(deviceId: String) {
        val exists = supabaseDeviceRepository.deviceExists(deviceId)

        if (exists) {
            Log.d(TAG, "Device already registered: $deviceId")
            return
        }

        Log.d(TAG, "Device not found. Registering: $deviceId")

        val registered = supabaseDeviceRepository.registerDevice(deviceId)
        if (!registered) {
            Log.d(TAG, "Failed to register deviceId=$deviceId in Supabase")
        }

        Log.d(TAG, "Device successfully registered: $deviceId")
    }

    private fun startDeviceSubscription(
        deviceId: String,
        onPlaylistUpdate: suspend () -> Unit,
        scope: CoroutineScope
    ) {
        Log.d(TAG, "Subscribing to realtime updates for deviceId=$deviceId")

        realtimeJob?.cancel()
        realtimeJob = null

        realtimeService.subscribe(deviceId)

        realtimeJob = realtimeService.deviceUpdates
            .onEach {
                Log.d(TAG, "Realtime update received for deviceId=$deviceId")
                onPlaylistUpdate()
            }.catch { throwable ->
                Log.e(TAG, "Realtime stream error for deviceId=$deviceId", throwable)
            }.launchIn(scope)
    }

}
