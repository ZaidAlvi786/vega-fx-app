package airsign.signage.player.data.utils

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.DisplayMetrics
import android.view.WindowManager
import org.json.JSONException
import org.json.JSONObject
import airsign.signage.player.data.utils.RootUtils.Companion.isDeviceRooted

// this class interacts with device & collect the information
class DeviceInfoUtils(
    private val mNetworkUtils: NetworkUtils,
   private val application: Application,
    private val mBasePref: BasePref
) {
    private val mWindowManager: WindowManager =
        application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mMetrics: DisplayMetrics = DisplayMetrics()

    val details: JSONObject
        get() {
            val jsonObject = JSONObject()
            return try {
                jsonObject.put("mac", mac())
                val details = JSONObject()
                details.put("ip", mNetworkUtils.iPAddress)
                details.put("height", height())
                details.put("width", width())
                details.put("device_name",Build.MODEL)
                details.put("software", airsign.signage.player.BuildConfig.VERSION_NAME)
                details.put("os_version", Build.VERSION.RELEASE)
                details.put("manufacture", Build.MANUFACTURER)
                jsonObject.put("ram", getMemory())
                details.put("root_level", isDeviceRooted())
                jsonObject.put("storage", storage())
                jsonObject.put("device_details", details)
                JSONObject().put("detail", jsonObject)
            } catch (e: JSONException) {
                e.printStackTrace()
                jsonObject
            }
        }

    fun getDetailsWithDeviceId(deviceId: String): JSONObject {
        val jsonObject = JSONObject()
        return try {
            // Use device_id instead of mac
            jsonObject.put("device_id", deviceId)
            val details = JSONObject()
            details.put("ip", mNetworkUtils.iPAddress)
            details.put("height", height())
            details.put("width", width())
            details.put("device_name", Build.MODEL)
            details.put("software", airsign.signage.player.BuildConfig.VERSION_NAME)
            details.put("os_version", Build.VERSION.RELEASE)
            details.put("manufacture", Build.MANUFACTURER)
            jsonObject.put("ram", getMemory())
            details.put("root_level", isDeviceRooted())
            jsonObject.put("storage", storage())
            jsonObject.put("device_details", details)
            JSONObject().put("detail", jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
            jsonObject
        }
    }

    override fun toString(): String {
        return details.toString()
    }

    private fun storage(): JSONObject {
        val stat = StatFs(Environment.getDataDirectory().path)
        val totalInternalStorage = stat.blockSizeLong * stat.blockCountLong / (1024 * 1024)
        val availableInternalStorage = stat.blockSizeLong * stat.availableBlocksLong / (1024 * 1024)

        val storage = JSONObject()
        storage.put("total", "$totalInternalStorage MB")
        storage.put("free", "$availableInternalStorage MB")

        return storage

    }
    private fun getMemory(): JSONObject {
        val activityManager =
            application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalRAM = memoryInfo.totalMem / (1024 * 1024)
        val freeRAM = memoryInfo.availMem / (1024 * 1024)

        val ramObject = JSONObject()

        ramObject.put("total", "$totalRAM MB")
        ramObject.put("free", "$freeRAM MB")

        return ramObject
    }


    private fun height(): Int {
        return mMetrics.heightPixels + navigationBarHeight
    }

    fun mac() : String{
        return mNetworkUtils.macAddress
    }
    private fun width(): Int {
        return mMetrics.widthPixels
    }

    private val navigationBarHeight: Int
        get() {
            val usableHeight = mMetrics.heightPixels
            mWindowManager.defaultDisplay.getRealMetrics(mMetrics)
            val realHeight = mMetrics.heightPixels
            return if (realHeight > usableHeight) realHeight - usableHeight else 0
        }

    init {
        mWindowManager.defaultDisplay.getMetrics(mMetrics)
    }
}