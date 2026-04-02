package airsign.signage.player.data.downloader

import airsign.signage.player.BuildConfig
import airsign.signage.player.data.MediaFactory.IMAGE
import airsign.signage.player.data.MediaFactory.PDF
import airsign.signage.player.data.MediaFactory.VIDEO
import airsign.signage.player.domain.model.Content
import android.content.Context
import android.util.Log
import com.downloader.Progress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

class FileManager(private val context: Context) {

    companion object {
        private const val TAG = "FileManager"
        const val WORKING_DIR = "media_store"
        const val WORKING_DIR_TEM = "media_store_temp"
        private const val FOLDER_NAME = BuildConfig.CACHE_FOLDER
    }

    suspend fun deleteTempFile(filename: String) = withContext(Dispatchers.IO) {
        val tempFile = File(getCachePath(WORKING_DIR_TEM), "$filename.temp")
        if (tempFile.exists() && tempFile.delete()) {
            Log.d(TAG, "File deleted: $filename")
        } else {
            Log.w(TAG, "File not found for deletion: $filename")
        }
    }

    suspend fun moveFile(filename: String) = withContext(Dispatchers.IO) {
        val source = File(getCachePath(WORKING_DIR_TEM), filename)
        val destination = File(getCachePath(WORKING_DIR), filename)

        if (source.exists()) {
            if (source.renameTo(destination)) {
                Log.d(TAG, "File moved: $filename")
            } else {
                Log.e(TAG, "Failed to move file: $filename")
            }
        } else {
            Log.w(TAG, "Source file not found: $filename")
        }
    }

    fun getProgressDisplayLine(progress: Progress): String {
        val currentMB = formatMB(progress.currentBytes)
        val totalMB = formatMB(progress.totalBytes)
        return "$currentMB/$totalMB"
    }

    suspend fun isMediaDownloadable(filename: String): Boolean {
        return !(isFileCached(filename) || isDownloading(filename))
    }

   private suspend fun isFileCached(filename: String): Boolean = withContext(Dispatchers.IO) {
        File(getCachePath(WORKING_DIR),filename).exists()
    }

   private suspend fun isDownloading(filename: String): Boolean = withContext(Dispatchers.IO) {
        File(getCachePath(WORKING_DIR_TEM), "${filename}.temp").exists()
    }

    suspend fun getFilePath(filename: String): String = withContext(Dispatchers.IO) {
        "${getCachePath(WORKING_DIR)}/${filename}"
    }

    suspend fun isCached(filename: String): Boolean = isFileCached(filename)

    /**
     * Delete a specific media file from cache
     * @param filename The filename to delete
     * @return true if file was deleted, false otherwise
     */
    suspend fun deleteMediaFile(filename: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(getCachePath(WORKING_DIR), filename)
            if (file.exists() && file.delete()) {
                Log.i(TAG, "Deleted media file from cache: $filename")
                true
            } else {
                Log.w(TAG, "Media file not found or could not be deleted: $filename")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting media file: $filename", e)
            false
        }
    }

    private fun formatMB(bytes: Long): String {
        return String.format(Locale.ENGLISH, "%.2f MB", bytes / (1024.0 * 1024.0))
    }


    suspend fun clearCache(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val cacheDir = File(context.cacheDir, FOLDER_NAME)
                deleteDir(cacheDir)
            } catch (e: java.lang.Exception) {
                false
            }
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir == null) return false
        if (dir.isDirectory) {
            val children: Array<String> = dir.list() ?: return false
            for (child in children) {
                val success = deleteDir(File(dir, child))
                if (!success) return false
            }
        }
        return dir.delete()
    }


    fun getCachePath(subDir: String): String {
        val path = File(context.cacheDir, "$FOLDER_NAME/$subDir")
        if (!path.exists()) path.mkdirs()
        return path.absolutePath
    }

    suspend fun getNonCachedMedia(mediaList: List<Content>): List<Pair<String, String>> {
        val pendingDownloads = mutableListOf<Pair<String, String>>()

        // Create a defensive snapshot to avoid ConcurrentModificationException if the list is updated during iteration
        val mediaListSnapshot = synchronized(mediaList) { ArrayList(mediaList) }

        if (mediaListSnapshot.isEmpty()) {
            Log.d(TAG, "getNonCachedMedia: media list is empty")
            return emptyList()
        }

        for (media in mediaListSnapshot) {
            if (!media.isDownloadable()) continue

            val fileName = media.filename ?: continue

            if (isMediaDownloadable(media.url)) {
                pendingDownloads += media.url to fileName
                Log.d(TAG, "Queued media for download: $fileName")
            }
        }

        Log.i(TAG, "getNonCachedMedia: ${pendingDownloads.size} files pending download")
        return pendingDownloads
    }

    private fun Content.isDownloadable(): Boolean {
        return type == IMAGE || type == VIDEO || type == PDF
    }
}
