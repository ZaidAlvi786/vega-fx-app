package airsign.signage.player.data.downloader

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.lifecycle.Observer
import androidx.work.Constraints.Builder
import androidx.work.Data
import androidx.work.NetworkType.CONNECTED
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkContinuation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkManager.getInstance
import com.downloader.PRDownloader.getStatus
import com.downloader.Status.QUEUED
import com.downloader.Status.RUNNING
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import airsign.signage.player.domain.model.DownloadStatus

class WorkContractor private constructor(context: Context) : ContextWrapper(context) {

    private val downloadList = ArrayList<String>()
    private var lastContinuation: WorkContinuation? = null  // NEW
    private val seenFilenames = HashSet<String>() // prevent duplicates within a batch


    private val _downloadProgress = MutableSharedFlow<DownloadStatus>(replay = 0)
    val downloadProgress = _downloadProgress.asSharedFlow()

    suspend fun notifyProgress(status: DownloadStatus) {
        status.downloaded = downloaded
        status.totalFiles = totalFiles
        _downloadProgress.emit(status)
    }

    private var totalFiles = 0
    private var downloaded = 1

    fun updateDownloadComplete() {
        downloaded += 1
    }


    @SuppressLint("RestrictedApi")
    fun downloadPlaylistMediaSequentially(mediaList: List<Pair<String,String>>) {
        val workManager = getInstance(applicationContext)
        // Cancel any queued/ongoing chain from previous rapid updates to avoid unbounded chaining
        workManager.cancelAllWorkByTag(DOWNLOAD_CHAIN_TAG)
        lastContinuation = null
        downloadList.clear()
        seenFilenames.clear()

        val newWorkRequests = mutableListOf<OneTimeWorkRequest>()

        downloaded = 1
        totalFiles = 0

        mediaList.forEach { media ->
            // Deduplicate filenames within the same batch
            if (!seenFilenames.add(media.second)) {
                Log.d(TAG, "Duplicate filename in batch, skipping: ${media.second}")
                return@forEach
            }
            if (isWorkAlreadyEnqueued(media.first)) {
                Log.d(TAG, "Download for $media is already in progress or queued, skipping.")
                return@forEach
            }

            downloadList.add(media.first)
            totalFiles += 1

            val data = Data.Builder()
                .putString(EXTRA_URL_ADDRESS, media.first)
                .putString(EXTRA_FILE_NAME, media.second)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(data)
                .setConstraints(Builder().setRequiredNetworkType(CONNECTED).build())
                .addTag(media.second)
                .addTag(DOWNLOAD_CHAIN_TAG)
                .build()

            newWorkRequests.add(workRequest)
        }

        if (newWorkRequests.isNotEmpty()) {
            // Chain the new requests to the previous continuation
            var continuation = if (lastContinuation == null) {
                workManager.beginWith(newWorkRequests.first())
            } else {
                lastContinuation!!.then(newWorkRequests.first())
            }

            newWorkRequests.drop(1).forEach { workRequest ->
                continuation = continuation.then(workRequest)
            }

            val finalRequest = newWorkRequests.last()
            detectWorkStatus(finalRequest)

            continuation.enqueue()
            lastContinuation = continuation // Update the last continuation
        }
    }

    private fun detectWorkStatus(finalRequest: OneTimeWorkRequest) {

        CoroutineScope(Dispatchers.Default).launch {
            withContext(Dispatchers.Main) {
                WorkManager.getInstance(applicationContext)
                    .getWorkInfoByIdLiveData(finalRequest.id)
                    .observeForever(object : Observer<WorkInfo> {
                        override fun onChanged(value: WorkInfo) {

                            if (value.state == WorkInfo.State.SUCCEEDED) {
                                Log.d(TAG, "✅ All files downloaded successfully!")
                                CoroutineScope(Dispatchers.IO).launch {
                                    notifyProgress(
                                        DownloadStatus(downloadComplete = true)
                                    )
                                }
                                getInstance(applicationContext)
                                    .getWorkInfoByIdLiveData(finalRequest.id)
                                    .removeObserver(this)
                            } else if (value.state == WorkInfo.State.FAILED || value.state == WorkInfo.State.CANCELLED) {
                                Log.e(TAG, "❌ Download failed or cancelled: ${value.state}")

                                CoroutineScope(Dispatchers.IO).launch {
                                    notifyProgress(
                                        DownloadStatus(downloadComplete = true)
                                    )
                                }
                                getInstance(applicationContext)
                                    .getWorkInfoByIdLiveData(finalRequest.id)
                                    .removeObserver(this)
                            }
                        }
                    })
            }
        }


    }

    private fun isWorkAlreadyEnqueued(fileName: String): Boolean {
        return downloadList.contains(fileName)
    }

    fun removeDownloadId(downloadId: String) {
        downloadList.remove(downloadId)
    }

    suspend fun isDownloaderRunning(): Boolean {
        return withContext(Dispatchers.IO) {
            for (downloadId in downloadList) {
                if (getStatus(downloadId.toInt()) == RUNNING || getStatus(downloadId.toInt()) == QUEUED) return@withContext true
            }
            false
        }
    }

    companion object {
        private var instance: WorkContractor? = null
        const val EXTRA_URL_ADDRESS = "extra_url_address"
        const val EXTRA_FILE_NAME = "extra_file_name"
        private const val TAG = "WorkContractor"
        private const val DOWNLOAD_CHAIN_TAG = "playlist_download_chain"

        fun init(context: Context) {
            if (instance == null) instance = WorkContractor(context)
        }

        fun get(): WorkContractor {
            return instance ?: throw UnsupportedOperationException("Init WorkContractor first")
        }
    }
}
