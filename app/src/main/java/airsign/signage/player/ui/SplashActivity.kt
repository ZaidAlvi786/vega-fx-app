package airsign.signage.player.ui

import airsign.signage.player.R
import airsign.signage.player.data.utils.BasePref
import airsign.signage.player.data.utils.PermissionUtils
import airsign.signage.player.ui.main.MainActivity
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.UiModeManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri.parse
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import android.provider.Settings.canDrawOverlays
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private val TAG = "SplashActivity"
    private lateinit var mPermissionUtils: PermissionUtils

    @Inject
    lateinit var mPref: BasePref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        val mainContainer = findViewById<ConstraintLayout>(R.id.mainConstraintLayout)
        hideSystemUI(mainContainer)

        mPermissionUtils = PermissionUtils(this)
    }


    override fun onResume() {
        super.onResume()
        handlePermissions()
    }

    private fun startApp() {
        openApp()
    }

    private fun openApp() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000)
    }

    private fun hideSystemUI(view: View) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, view).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }


    private fun shouldRequestOverlayOnThisDevice(): Boolean {
        try {
            val uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
            val isTv = uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
            return !isTv && hasOverlayIntentHandler()
        } catch (e: Exception) {
            return false
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun hasOverlayIntentHandler(): Boolean {
        return try {
            val intent = Intent(ACTION_MANAGE_OVERLAY_PERMISSION, parse("package:$packageName"))
            packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null
        } catch (e: Exception) {
            false
        }
    }


    private fun overlaySettingsPopup() {
        val message = "This app needs overlay permission to function properly "

        AlertDialog.Builder(this).setTitle("Overlay Permission Required").setMessage(message)
            .setPositiveButton("Open Settings") { _, _ ->
                try {
                    val uri = parse("package:$packageName")
                    val intent = Intent(ACTION_MANAGE_OVERLAY_PERMISSION, uri)
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    startApp()
                } catch (e: Exception) {
                    startApp()
                }
            }.show()
    }

    private fun handlePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldRequestOverlayOnThisDevice()) {
                if (!canDrawOverlays(this)) {
                    Log.w(TAG, "Overlay permission not granted (API ${Build.VERSION.SDK_INT})")
                    overlaySettingsPopup()
                } else {
                    Log.w(TAG, "Overlay permission granted (API ${Build.VERSION.SDK_INT})")
                    startApp()
                }
            } else {
                Log.w(TAG, "Overlay permission not needed on this TV/Fire device")
                startApp()
            }
        } else {
            startApp()
        }
    }
}