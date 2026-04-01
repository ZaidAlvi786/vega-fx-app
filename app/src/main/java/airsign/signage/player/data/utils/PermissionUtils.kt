package airsign.signage.player.data.utils

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.TargetApi
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.requestPermissions

class PermissionUtils constructor(private val context: Activity) {
    private val PERMISSIONS = arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)

    companion object {
        const val REQUEST_CODE = 124392
    }

//    @TargetApi(23)
//    fun hasPermissions(): Boolean {
//        for (permission in PERMISSIONS)
//            if (context.checkSelfPermission(permission) != PERMISSION_GRANTED)
//                return false
//
//        return true
//    }

    @TargetApi(23)
    fun canWriteSettings(): Boolean {
        return Settings.System.canWrite(context)
    }

    fun requestStoragePermission() {
        requestPermissions(context, PERMISSIONS, REQUEST_CODE)
    }

    fun checkPermission11(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else true
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun requestWriteSettings() {
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
                break
            }
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

}