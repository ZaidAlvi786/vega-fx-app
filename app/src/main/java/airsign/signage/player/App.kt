package airsign.signage.player

import airsign.signage.player.data.downloader.WorkContractor
import airsign.signage.player.ui.CrashActivity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import com.downloader.PRDownloader
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    private val TAG = "App"

    override fun onCreate() {
        super.onCreate()
        PRDownloader.initialize(this)
        WorkContractor.init(this)

    }

    private class RelaunchExceptionHandler(val context: Context) : Thread.UncaughtExceptionHandler {
        override fun uncaughtException(thread: Thread, throwable: Throwable) {
            val intent = Intent(context, CrashActivity::class.java).apply {
                putExtra("message", throwable.stackTraceToString())
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }

            FirebaseCrashlytics.getInstance().recordException(throwable)

            Log.d("ApplicationError", "uncaughtException: ${throwable.stackTraceToString()}")
            context.startActivity(intent)

            try {
                Thread.sleep(2000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(1)
        }
    }
}
