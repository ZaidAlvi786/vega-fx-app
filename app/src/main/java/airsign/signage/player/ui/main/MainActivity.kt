package airsign.signage.player.ui.main

import airsign.signage.player.R
import airsign.signage.player.data.MediaFactory.create
import airsign.signage.player.domain.IPlaylistController
import airsign.signage.player.domain.model.Content
import airsign.signage.player.ui.fragments.message.MessageFragment
import airsign.signage.player.ui.fragments.registration.RegistrationCodeFragment
import airsign.signage.player.ui.main.viewmodel.MainViewModel
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.os.Handler
import android.os.Looper
import airsign.signage.player.service.WatchdogService
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), IPlaylistController {

    companion object {
        const val ACTION_SCHEDULE_START = "all.sign.player.SCHEDULE_START"
        const val ACTION_SCHEDULE_END = "all.sign.player.SCHEDULE_END"
    }

    private val TAG: String = MainActivity::class.java.name

    private lateinit var mainViewModel: MainViewModel

    private lateinit var mMainUI: ConstraintLayout
    private var wakeLock: PowerManager.WakeLock? = null
    
    private val pulseHandler = Handler(Looper.getMainLooper())
    private val pulseRunnable = object : Runnable {
        override fun run() {
            WatchdogService.sendPulse(this@MainActivity)
            pulseHandler.postDelayed(this, 20000L) // Pulse every 20s
        }
    }



    private val asyncScope = CoroutineScope(Job() + Dispatchers.IO)
    private lateinit var downloadView: TextView

    private val scheduleReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "BroadcastReceiver.onReceive called - action: ${intent?.action}")
            when (intent?.action) {
                ACTION_SCHEDULE_START -> {
                    Log.d(TAG, "✓ Received schedule start broadcast")
                    val scheduleId = intent.getStringExtra("schedule_id")
                    Log.d(TAG, "Starting playlist for schedule: $scheduleId")
//                    mainViewModel.startSignage()
                }
                ACTION_SCHEDULE_END -> {
                    Log.d(TAG, "✓ Received schedule end broadcast")
                    val scheduleId = intent.getStringExtra("schedule_id")
                    Log.d(TAG, "Stopping playlist for schedule: $scheduleId")
//                    mainViewModel.stopSignage()
                }
                else -> {
                    Log.w(TAG, "Unknown action received: ${intent?.action}")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)

        mMainUI = findViewById(R.id.mainConstraintLayoutHome)
        downloadView = findViewById(R.id.downloadView)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        mainViewModel.viewState.observe(this) {
            when (it) {
                is MainViewModel.ViewState.StartSignage -> displayMedia()

                is MainViewModel.ViewState.DisplayMessage -> {
                    loadFragment(MessageFragment.newInstance(it.message,it.connected))
                }

                is MainViewModel.ViewState.DisplayCode -> {
                    loadFragment(RegistrationCodeFragment.newInstance(it.code))
                }

                else -> {}
            }
        }

        try {
            val filter = IntentFilter().apply {
                addAction(ACTION_SCHEDULE_START)
                addAction(ACTION_SCHEDULE_END)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(scheduleReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
                Log.d(TAG, "Schedule receiver registered (API 33+) with actions: $ACTION_SCHEDULE_START, $ACTION_SCHEDULE_END")
            } else {
                registerReceiver(scheduleReceiver, filter)
                Log.d(TAG, "Schedule receiver registered (legacy) with actions: $ACTION_SCHEDULE_START, $ACTION_SCHEDULE_END")
            }
            Log.d(TAG, "✓ BroadcastReceiver registration successful")
        } catch (e: Exception) {
            Log.e(TAG, "✗ BroadcastReceiver registration failed: $e", e)
        }


    }

    override fun onResume() {
        super.onResume()
        hideSystemUI(mMainUI)
        acquireWakeLock()

        mainViewModel.onAppResumed()
        mainViewModel.startSignage()
        
        startPulse()
    }

    override fun onBackPressed() {
        // Do nothing to prevent exiting the signage app accidentally
        Log.d(TAG, "onBackPressed: Back button ignored to maintain signage loop")
    }

    override fun onPause() {
        super.onPause()

        removeFragments()
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        stopPulse()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")

        try {
            unregisterReceiver(scheduleReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering schedule receiver", e)
        }

        asyncScope.cancel()
        releaseWakeLock()
    }

    private fun removeFragments(){
        val fragmentManager = supportFragmentManager
        val fragment = fragmentManager.findFragmentById(R.id.media_container)

        if (fragment != null) {
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.remove(fragment)
            fragmentTransaction.commitAllowingStateLoss()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.media_container, fragment).commit()
    }

    private fun loadFragment(media: Content) {
        val fragment = create(media)
        fragment.addPlaylistController(this)

        loadFragment(fragment)
    }

    private fun displayMedia() {
        nextMedia()
        downloadView.visibility = View.GONE
    }

    override fun nextMedia() {
        val media = mainViewModel.nextMedia()
        loadFragment(media)
    }

    private fun acquireWakeLock() {
        // Keep screen on
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Keep CPU awake (Partial WakeLock)
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Aircast::SyncWakeLock")
        }
        
        if (wakeLock?.isHeld == false) {
            Log.d(TAG, "Acquiring Partial WakeLock to prevent system freeze")
            wakeLock?.acquire(10 * 60 * 1000L /*10 minutes*/)
        }
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            Log.d(TAG, "Releasing Partial WakeLock")
            wakeLock?.release()
        }
    }

    private fun hideSystemUI(view: View) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, view).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun startPulse() {
        Log.d(TAG, "Starting resilience heartbeats")
        pulseHandler.removeCallbacks(pulseRunnable)
        pulseHandler.post(pulseRunnable)
    }

    private fun stopPulse() {
        Log.d(TAG, "Stopping resilience heartbeats")
        pulseHandler.removeCallbacks(pulseRunnable)
    }

}