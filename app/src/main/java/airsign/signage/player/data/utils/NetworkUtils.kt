package airsign.signage.player.data.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.provider.Settings
import android.text.format.Formatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.net.URL
import java.util.Collections

// This class interacts with the device network and collects the ip address
class NetworkUtils(private val context: Context) {
    private val mWifiManager: WifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    val macAddress: String
        get() { return uID() }

    @SuppressLint("HardwareIds")
    private fun uID(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }


    private fun macAddress(): String {
        try {
            val all: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (!nif.name.equals("wlan0", ignoreCase = true)) continue

                val macBytes = nif.hardwareAddress ?: return ""
                val res1 = StringBuilder()

                for (b in macBytes) res1.append(String.format("%02X:", b))
                if (res1.isNotEmpty()) res1.deleteCharAt(res1.length - 1)

                return res1.toString()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return ""
    }

    val iPAddress: String?
        get() {
            val ip = Formatter.formatIpAddress(mWifiManager.connectionInfo.ipAddress)
            return if (ip == "0.0.0.0") ethernetIP else ip
        }
    private val ethernetIP: String?
        get() {
            try {
                val en = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val intf = en.nextElement()
                    val enumIpAddr = intf.inetAddresses
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress = enumIpAddr.nextElement()
                        if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) return inetAddress.getHostAddress()
                    }
                }
            } catch (ex: SocketException) {
                ex.printStackTrace()
            }
            return null
        }

    companion object {
        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

        suspend fun isNetworkAvailable(): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val url = URL("https://www.google.com")
                    val urlConnection = url.openConnection() as HttpURLConnection
                    urlConnection.connectTimeout = 2000
                    urlConnection.connect()
                    urlConnection.responseCode == 200
                } catch (e: IOException) {
                    false
                }
            }
        }
    }


}