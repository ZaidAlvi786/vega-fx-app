package airsign.signage.player.data.repository

import airsign.signage.player.data.local.dao.ApiResponseDao
import airsign.signage.player.data.local.dao.PairingCodeDao
import airsign.signage.player.data.local.entity.ApiResponseEntity
import airsign.signage.player.data.local.entity.PairingCodeEntity
import airsign.signage.player.data.remote.ApiResult
import airsign.signage.player.data.remote.CheckPairingResponse
import airsign.signage.player.data.remote.DeviceApiService
import airsign.signage.player.data.remote.GenerateCodeResponse
import android.util.Log
import airsign.signage.player.data.remote.HeartbeatRequest
import airsign.signage.player.data.remote.HeartbeatResponse
import airsign.signage.player.data.remote.PlaylistResponse
import airsign.signage.player.data.utils.BasePref
import airsign.signage.player.domain.model.PairingSession
import airsign.signage.player.domain.repository.DeviceRepository
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import javax.inject.Inject

class DeviceRepositoryImpl @Inject constructor(
    private val apiService: DeviceApiService,
    private val pref: BasePref,
    private val apiResponseDao: ApiResponseDao,
    private val pairingCodeDao: PairingCodeDao
) : DeviceRepository {

    private val gson = Gson()

    override suspend fun generatePairingCode(): ApiResult<PairingSession> {
        return apiCall(
            endpoint = ENDPOINT_GENERATE_CODE,
            call = { apiService.generatePairingCode() },
            map = { response ->
                response
                    .toPairingSession()
                    .also { cacheUnpairedDevice(it) }
            }
        )
    }

    override suspend fun checkPairingStatus(code: String): ApiResult<CheckPairingResponse> =
        apiCall(
            endpoint = ENDPOINT_CHECK_PAIRING,
            call = { apiService.checkPairingStatus(code) },
            map = { it }
        )

    override suspend fun sendHeartbeat(
        deviceId: String,
        appVersion: String,
        osVersion: String,
        lastSyncTime: Long?
    ): ApiResult<HeartbeatResponse> = apiCall(
        endpoint = ENDPOINT_HEARTBEAT,
        call = {
            apiService.sendHeartbeat(
                HeartbeatRequest(
                    deviceId = deviceId,
                    appVersion = appVersion,
                    osVersion = osVersion,
                    lastSyncTime = lastSyncTime
                )
            )
        },
        map = { it }
    )

    override suspend fun postScreenDetails(deviceId: String, screenDetailsJson: String): ApiResult<Unit> {
        return apiCall(
            endpoint = ENDPOINT_SCREEN_DETAILS,
            call = {
                apiService.postScreenDetails(screenDetailsJson)
            },
            map = { Unit }
        )
    }

    override suspend fun fetchCurrentPlaylist(deviceId: String): ApiResult<PlaylistResponse> {
        return apiCall(
            endpoint = ENDPOINT_CURRENT_PLAYLIST,
            call = { apiService.fetchCurrentPlaylist(deviceId) },
            map = { response ->
                response
            }
        )
    }

    override fun persistDeviceId(deviceId: String) {
        pref.setString(BasePref.DEVICE_ID, deviceId)
    }

    override fun getPersistedDeviceId(): String? {
        return pref.getString(BasePref.DEVICE_ID).takeIf { it.isNotBlank() }
    }

    override fun clearDeviceData() {
        pref.setString(BasePref.DEVICE_ID, "")
    }

    override suspend fun getCachedPairingSession(): PairingSession? {
        return pairingCodeDao.getLatest()?.toPairingSession()
    }

    override suspend fun clearCachedPairingSession() {
        pairingCodeDao.clear()
    }


    override suspend fun getPersistApiResponse(endpoint: String): String? {
        val entity = apiResponseDao.getLatest(endpoint) ?: return null
        val payload = entity.payload ?: return null
        return runCatching {
            payload
        }.getOrNull()
    }


    override suspend fun clearCachedPlaylist() {
        apiResponseDao.delete(ENDPOINT_CURRENT_PLAYLIST)
    }

    private suspend fun <T, R> apiCall(
        endpoint: String,
        call: suspend () -> Response<T>,
        map: suspend (T) -> R
    ): ApiResult<R> {
        return try {
            val response = call()
            val statusCode = response.code()
            when {
                statusCode == 401 -> {
                    val errorBody = extractErrorBody(response)
                    recordApiResponse(endpoint, statusCode, "Unauthorized: $errorBody")
                    ApiResult.Unauthorized
                }
                statusCode == 404 -> {
                    val errorBody = extractErrorBody(response)
                    recordApiResponse(endpoint, statusCode, "NotFound: $errorBody")
                    ApiResult.NotFound
                }
                response.isSuccessful -> {
                    val body = response.body()
                    if (body != null) {
                        val mapped = map(body)
                        val payloadJson = runCatching { gson.toJson(body) }.getOrNull()
                        recordApiResponse(endpoint, statusCode, payloadJson)
                        
                        if (endpoint == ENDPOINT_HEARTBEAT) {
                            Log.i("HeartbeatTrace", "Heartbeat Payload: $payloadJson")
                        }
                        
                        ApiResult.Success(mapped)
                    } else {
                        Log.w("NetworkTrace", "[$endpoint] Empty response body with code 2xx")
                        recordApiResponse(endpoint, statusCode, "Empty response body")
                        ApiResult.Failure(statusCode, "Empty response body")
                    }
                }
                else -> {
                    val errorBody = extractErrorBody(response)
                    recordApiResponse(endpoint, statusCode, errorBody)
                    Log.e("NetworkTrace", "[$endpoint] API Error: $statusCode ${response.message()} | Body: $errorBody")
                    ApiResult.Failure(statusCode, response.message(), errorBody = errorBody)
                }
            }
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            Log.e("NetworkTrace", "[$endpoint] Request Exception: ${throwable.message}", throwable)
            recordApiResponse(endpoint, null, throwable.message)
            ApiResult.Failure(message = throwable.message, throwable = throwable)
        }
    }

    private suspend fun recordApiResponse(endpoint: String, statusCode: Int?, payload: String?) {
        val entity = ApiResponseEntity(
            endpoint = endpoint,
            statusCode = statusCode,
            payload = payload,
            timestamp = System.currentTimeMillis()
        )
        apiResponseDao.insert(entity)
    }

    private fun extractErrorBody(response: Response<*>): String? {
        return runCatching { response.errorBody()?.string() }.getOrNull()
    }

    private suspend fun cacheUnpairedDevice(session: PairingSession) {
        val entity = PairingCodeEntity(
            code = session.code,
            expiresAtEpochMillis = session.expiresAt.toEpochMilli(),
            pairingId = session.pairingId,
            createdAt = System.currentTimeMillis()
        )
        pairingCodeDao.upsert(entity)
    }

    private fun GenerateCodeResponse.toPairingSession(): PairingSession {
        val expiry = try {
            OffsetDateTime.parse(expiresAt).toInstant()
        } catch (exception: DateTimeParseException) {
            Instant.now().plusSeconds(DEFAULT_PAIRING_EXPIRY_SECONDS)
        }
        val safePairingId = pairingId ?: ""

        return PairingSession(
            code = code,
            expiresAt = expiry,
            pairingId = safePairingId
        )
    }

    private fun PairingCodeEntity.toPairingSession(): PairingSession {
        return PairingSession(
            code = code,
            expiresAt = Instant.ofEpochMilli(expiresAtEpochMillis),
            pairingId = pairingId
        )
    }

    companion object {
        private const val ENDPOINT_GENERATE_CODE = "api/devices/generate-code"
        const val ENDPOINT_CHECK_PAIRING = "api/devices/check-pairing"
        private const val ENDPOINT_HEARTBEAT = "api/devices/heartbeat"
        const val ENDPOINT_CURRENT_PLAYLIST = "api/devices/current-playlist"
        private const val ENDPOINT_SCREEN_DETAILS = "api/devices/screen-details"

        private const val DEFAULT_PAIRING_EXPIRY_SECONDS = 120L
        private const val DEFAULT_MEDIA_DURATION = 30
        private const val DEFAULT_PLAYLIST_NAME = "Aircast Playlist"
    }
}

