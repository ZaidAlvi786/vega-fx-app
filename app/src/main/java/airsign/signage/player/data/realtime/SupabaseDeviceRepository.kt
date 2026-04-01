package airsign.signage.player.data.realtime

import airsign.signage.player.BuildConfig
import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.engine.cio.CIO
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseDeviceRepository @Inject constructor() {

    companion object {
        private const val TAG = "SupabaseDeviceRepo"
        private const val DEVICES_TABLE = "device_sync_state"
    }

    private val supabaseClient: SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
        install(Realtime)
        httpEngine = CIO.create()
    }

    suspend fun deviceExists(deviceId: String): Boolean {
        return try {
            val response = supabaseClient.from(DEVICES_TABLE).select {
                    filter { eq("device_id", deviceId) }
                    limit(1)
                }.decodeAs<List<JsonObject>>()

            response.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking device existence: $deviceId", e)
            false
        }
    }

    suspend fun registerDevice(deviceId: String): Boolean {
        return try {
            Log.d(TAG, "Registering device: $deviceId")
            val currentTime = System.currentTimeMillis()

            val payload = buildJsonObject {
                put("device_id", deviceId)
                put("lastupdate", currentTime)
            }

            supabaseClient.from(DEVICES_TABLE).insert(payload)
            Log.i(TAG, "Device successfully registered: $deviceId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error registering device: $deviceId", e)
            false
        }
    }
}
