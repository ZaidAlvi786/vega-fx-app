package airsign.signage.player.data.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Window
import androidx.annotation.RequiresApi

object ControlUtils {
    fun convertVolumeFrom100To15(volume: Int): Int {
        // Ensure volume is within the valid range (0 to 100)
        val clampedVolume = volume.coerceIn(0, 100)
        return (clampedVolume * 15) / 100
    }

    fun setMediaVolume(context: Context, volume: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val clampedVolume = volume.coerceIn(0, maxVolume)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, clampedVolume, 0)
    }

    private fun convertBrightness(brightnessValue: Int): Int {
        // Ensure volume is within the valid range (0 to 100)
        val clampedVolume = brightnessValue.coerceIn(0, 100)
        return (clampedVolume * 255) / 100
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun setBrightness(context: Context, brightnessValue: Int) {
        val clampedBrightness = convertBrightness(brightnessValue)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(context)) {
            // For API level 23 and above, and if the app has the WRITE_SETTINGS permission,
            // we can directly change the system screen brightness.
            Settings.System.putInt(
                context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, clampedBrightness
            )
        } else {
            // For API level 22 and below, or if the app doesn't have the WRITE_SETTINGS permission,
            // we can use a workaround by creating a new system settings intent.
            val newSettingsValue = if (clampedBrightness == 0) {
                1 // We set 1 instead of 0 to avoid unintended behavior on some devices.
            } else {
                clampedBrightness
            }

            try {
                // The action may differ on some devices, try multiple possible actions.
                val actions = arrayOf(
                    Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                )

                for (action in actions) {
                    val intent = Intent(action)
                    intent.data = Uri.parse("package:" + context.packageName)
                    context.startActivity(intent)
                    break // Only start the first found action
                }
            } catch (e: ActivityNotFoundException) {
                // Handle the exception or show a message that the settings page is not available.
                e.printStackTrace()
            }
        }
    }
    fun setBrightness(window: Window?, brightnessValue: Int) {
        if (window == null) return
        val layoutParams = window.attributes
        val brightness = brightnessValue / 100f
        Log.d(TAG, "applying new brightness $brightness")

        layoutParams.screenBrightness = brightness // Map value to the 0.0 to 1.0 range
        window.attributes = layoutParams
    }

    private const val TAG = "ControlUtils"
}