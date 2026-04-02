package airsign.signage.player.service

import airsign.signage.player.R
import airsign.signage.player.ui.SplashActivity
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.concurrent.atomic.AtomicLong

/**
 * WatchdogService: A Foreground Service that ensures the app stays alive.
 * It expects a "Pulse" intent from the UI every 15-30 seconds.
 * If pulses stop during active signage, it relaunch the app.
 */
class WatchdogService : Service() {

    companion object {
        private const val TAG = "WatchdogService"
        private const val NOTIFICATION_ID = 101
        private const val CHANNEL_ID = "ResilienceChannel"
        
        const val ACTION_PULSE = "airsign.signage.player.ACTION_PULSE"
        const val ACTION_STOP = "airsign.signage.player.ACTION_STOP"
        
        private const val CHECK_INTERVAL_MS = 30000L // Check every 30s
        private const val PULSE_TIMEOUT_MS = 60000L  // Restart if no pulse for 60s
        
        private val lastPulseTime = AtomicLong(0L)
        private var isMonitoring = false

        fun sendPulse(context: Context) {
            val intent = Intent(context, WatchdogService::class.java).apply {
                action = ACTION_PULSE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, WatchdogService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val watchdogHandler = Handler(Looper.getMainLooper())
    private val watchdogRunnable = object : Runnable {
        override fun run() {
            checkResilience()
            watchdogHandler.postDelayed(this, CHECK_INTERVAL_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "WatchdogService created - initializing resilience monitor")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Monitoring player health..."))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PULSE -> {
                if (!isMonitoring) {
                    Log.i(TAG, "First pulse received - starting watchdog loop")
                    isMonitoring = true
                    lastPulseTime.set(System.currentTimeMillis())
                    watchdogHandler.removeCallbacks(watchdogRunnable)
                    watchdogHandler.post(watchdogRunnable)
                } else {
                    lastPulseTime.set(System.currentTimeMillis())
                }
            }
            ACTION_STOP -> {
                Log.i(TAG, "Stop action received - shutting down resilience monitor")
                isMonitoring = false
                watchdogHandler.removeCallbacks(watchdogRunnable)
                stopForeground(true)
                stopSelf()
            }
        }
        return START_STICKY // OS will attempt to restart service if killed
    }

    private fun checkResilience() {
        if (!isMonitoring) return

        val now = System.currentTimeMillis()
        val lastPulse = lastPulseTime.get()
        val diff = now - lastPulse

        Log.d(TAG, "Health Check: Last pulse was ${diff / 1000}s ago")

        if (diff > PULSE_TIMEOUT_MS) {
            Log.e(TAG, "WATCHDOG TRIGGERED: No pulse for ${diff / 1000}s. Attempting app recovery...")
            relaunchApp()
            // Reset timer so we don't spam restarts
            lastPulseTime.set(System.currentTimeMillis())
        }
    }

    private fun relaunchApp() {
        try {
            val launchIntent = Intent(this, SplashActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(launchIntent)
            Log.i(TAG, "Relaunch intent sent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to relaunch app", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Signage Health Monitor"
            val descriptionText = "Ensures the signage player stays active 24/7"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Aircast Player Resilience")
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.w(TAG, "WatchdogService destroyed - monitoring stopped")
        watchdogHandler.removeCallbacks(watchdogRunnable)
    }
}
