package airsign.signage.player.domain.repository

import airsign.signage.player.data.remote.ApiResult
import airsign.signage.player.data.remote.PlaylistResponse

interface PlaylistRepository {
    suspend fun fetchPlaylist(deviceId: String): ApiResult<PlaylistResponse>
    suspend fun getCachedPlaylist(): PlaylistResponse?
    suspend fun savePlaylist(response: PlaylistResponse)
    suspend fun clearCache()
}
