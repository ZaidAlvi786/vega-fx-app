package airsign.signage.player.data.repository

import airsign.signage.player.data.local.dao.ApiResponseDao
import airsign.signage.player.data.local.entity.ApiResponseEntity
import airsign.signage.player.data.remote.ApiResult
import airsign.signage.player.data.remote.DeviceApiService
import airsign.signage.player.data.remote.PlaylistResponse
import airsign.signage.player.domain.repository.PlaylistRepository
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import javax.inject.Inject

class PlaylistRepositoryImpl @Inject constructor(
    private val apiService: DeviceApiService,
    private val apiResponseDao: ApiResponseDao
) : PlaylistRepository {

    private val gson = Gson()
    private val endpoint = "api/devices/current-playlist"

    override suspend fun fetchPlaylist(deviceId: String): ApiResult<PlaylistResponse> {
        return try {
            val response = apiService.fetchCurrentPlaylist(deviceId)
            val statusCode = response.code()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    savePlaylist(body)
                    ApiResult.Success(body)
                } else {
                    ApiResult.Failure(statusCode, "Empty playlist")
                }
            } else {
                if (statusCode == 401) ApiResult.Unauthorized else ApiResult.Failure(statusCode, response.message())
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            ApiResult.Failure(message = e.message, throwable = e)
        }
    }

    override suspend fun getCachedPlaylist(): PlaylistResponse? {
        val entity = apiResponseDao.getLatest(endpoint) ?: return null
        return entity.payload?.let {
            runCatching { gson.fromJson(it, PlaylistResponse::class.java) }.getOrNull()
        }
    }

    override suspend fun savePlaylist(response: PlaylistResponse) {
        val payload = gson.toJson(response)
        apiResponseDao.insert(
            ApiResponseEntity(
                endpoint = endpoint,
                statusCode = 200,
                payload = payload,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    override suspend fun clearCache() {
        apiResponseDao.delete(endpoint)
    }
}
