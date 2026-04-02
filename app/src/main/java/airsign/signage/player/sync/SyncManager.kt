package airsign.signage.player.sync

import airsign.signage.player.data.downloader.FileManager
import airsign.signage.player.data.remote.ApiResult
import airsign.signage.player.data.remote.PlaylistResponse
import airsign.signage.player.domain.repository.PlaylistRepository
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val fileManager: FileManager
) {
    private val TAG = "SyncManager"

    suspend fun sync(deviceId: String): ApiResult<PlaylistResponse> = withContext(Dispatchers.IO) {
        val cached = playlistRepository.getCachedPlaylist()
        val result = playlistRepository.fetchPlaylist(deviceId)

        if (result is ApiResult.Success) {
            val newPlaylist = result.data
            if (cached != null) {
                diffAndCleanup(cached, newPlaylist)
            }
        }
        return@withContext result
    }

    private suspend fun diffAndCleanup(old: PlaylistResponse, new: PlaylistResponse) {
        val oldMediaMap = old.mediaItems.associateBy { it.id }
        val newMediaMap = new.mediaItems.associateBy { it.id }

        // Cleanup deleted or changed media
        for (oldMsg in old.mediaItems) {
            val newMsg = newMediaMap[oldMsg.id]
            if (newMsg == null || hasChanged(oldMsg, newMsg)) {
                Log.d(TAG, "Cleaning up stale media: ${oldMsg.name}")
                fileManager.deleteMediaFile(oldMsg.name)
            }
        }
    }

    private fun hasChanged(old: airsign.signage.player.data.remote.MediaItem, new: airsign.signage.player.data.remote.MediaItem): Boolean {
        // High priority: Checksum
        if (old.checksum != null && new.checksum != null) {
            return old.checksum != new.checksum
        }
        // Fallback: last_modified
        return old.last_modified != new.last_modified
    }
}
