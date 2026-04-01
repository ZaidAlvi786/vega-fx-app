package airsign.signage.player.domain.repository

import airsign.signage.player.data.remote.ApiResult
import airsign.signage.player.data.remote.CheckPairingResponse
import airsign.signage.player.data.remote.HeartbeatResponse
import airsign.signage.player.data.remote.PlaylistResponse
import airsign.signage.player.domain.model.PairingSession
interface DeviceRepository {

    suspend fun generatePairingCode(): ApiResult<PairingSession>

    suspend fun checkPairingStatus(code: String): ApiResult<CheckPairingResponse>

    suspend fun sendHeartbeat(
        deviceId: String,
        appVersion: String,
        osVersion: String
    ): ApiResult<HeartbeatResponse>

    suspend fun postScreenDetails(deviceId: String, screenDetailsJson: String): ApiResult<Unit>

    suspend fun fetchCurrentPlaylist(deviceId: String): ApiResult<PlaylistResponse>

    fun persistDeviceId(deviceId: String)

    fun getPersistedDeviceId(): String?

    fun clearDeviceData()

    suspend fun getCachedPairingSession(): PairingSession?

    suspend fun clearCachedPairingSession()

    suspend fun getPersistApiResponse(endpoint : String): String?

    suspend fun clearCachedPlaylist()
}


