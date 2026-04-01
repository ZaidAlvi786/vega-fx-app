package airsign.signage.player.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.os.Build
import androidx.annotation.RequiresApi

class ConnectivityChangeReceiver(private val connectivityCallback: ConnectivityCallback) :
    BroadcastReceiver() {

    private var flag = false

    interface ConnectivityCallback {
        fun onNetworkConnected()
        fun onNetworkDisconnected()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceive(context: Context, intent: Intent) {
        if (!flag) {
            flag = true
            return
        }
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        if (networkCapabilities != null && (networkCapabilities.hasTransport(TRANSPORT_WIFI) || networkCapabilities.hasTransport(
                TRANSPORT_CELLULAR
            ))
        ) {
            // Connected to a network
            connectivityCallback.onNetworkConnected()
        } else {
            // Not connected to any network
            connectivityCallback.onNetworkDisconnected()
        }
    }
}
