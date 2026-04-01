package airsign.signage.player.data.utils

import java.io.File
import java.io.IOException

class RootUtils {
    companion object {
        fun isDeviceRooted(): Boolean {
            return isRooted() && isRootPermissionGranted()
        }

        private fun isRootPermissionGranted(): Boolean {
            return try {
                val p = Runtime.getRuntime().exec("su")
                true
            } catch (e: IOException) {
                false
            }
        }

       private fun isRooted(): Boolean {
            val fileNames = arrayOf(
                "/system/app/Superuser.apk",
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su"
            )

            for (fileName in fileNames) {
                if (File(fileName).exists()) {
                    return true
                }
            }

            return false
        }
    }

}