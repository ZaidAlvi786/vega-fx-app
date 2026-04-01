package airsign.signage.player.data.downloader

import airsign.signage.player.data.downloader.FileManager.Companion.WORKING_DIR_TEM
import airsign.signage.player.data.downloader.WorkContractor.Companion.EXTRA_FILE_NAME
import airsign.signage.player.data.downloader.WorkContractor.Companion.EXTRA_URL_ADDRESS
import airsign.signage.player.domain.model.DownloadStatus
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader.download
import com.downloader.Progress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CountDownLatch

class DownloadWorker(private val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private var downloadId = -1
    private val mFileManager = FileManager(context)
    private val asyncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var DISPLAY_FILE_NAME: String = ""

    override fun doWork(): Result {

        val downloadPath = inputData.getString(EXTRA_URL_ADDRESS)
        val fileName = inputData.getString(EXTRA_FILE_NAME)
        if (downloadPath == null || fileName == null) {
            Log.w(TAG, "Null path found")
            return Result.failure()
        }

        DISPLAY_FILE_NAME = fileName
        return try {
            val url = URL(downloadPath)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                Log.i(TAG, "Server response: ${connection.responseCode}")
                return Result.failure()
            } else {
                val latch = CountDownLatch(1)

                downloadFile(downloadPath, fileName, latch)

                latch.await()

                Result.success()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
            Result.failure()
        }
    }

    private fun downloadFile(
        downloadPath: String, filename: String, latch: CountDownLatch
    ) {
        val outputFile = File(mFileManager.getCachePath(WORKING_DIR_TEM))

        downloadId = download(downloadPath, outputFile.absolutePath, filename).build()
            .setOnProgressListener(this::handleProgress)
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    downComplete(filename)
                    latch.countDown() // Notify that the download is complete
                }

                override fun onError(error: Error) {
                    downloadError(error, filename)
                    latch.countDown() // Notify that the download failed
                }
            })
    }


    private fun handleProgress(progress: Progress) {
        val percent = (progress.currentBytes * 100 / progress.totalBytes).toInt()

        Log.d(TAG, "Download progress: $percent% for $DISPLAY_FILE_NAME")

        CoroutineScope(Dispatchers.IO).launch {
            WorkContractor.get().notifyProgress(
                DownloadStatus(fileName = DISPLAY_FILE_NAME, progress = percent)
            )
        }
    }


    private fun downloadError(error: Error, filename: String) {
        Log.w(TAG, "Download error: $filename")
        WorkContractor.get().removeDownloadId(filename)
        asyncScope.launch { mFileManager.deleteTempFile(filename) }
    }

    private fun downComplete(filename: String) {
        Log.d(TAG, "downComplete: ")
        WorkContractor.get().removeDownloadId(filename)
        WorkContractor.get().updateDownloadComplete()
        asyncScope.launch { mFileManager.moveFile(filename) }
    }

    companion object {
        private const val TAG = "DownloadWorker"
    }
}