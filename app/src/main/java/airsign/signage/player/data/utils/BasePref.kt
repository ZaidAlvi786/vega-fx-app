package airsign.signage.player.data.utils

import android.app.Application
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

// This class save data in secure EncryptedSharedPreferences
class BasePref(private val context: Application) {
    init {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            mPref = EncryptedSharedPreferences.create(
                context,
                "secure_signage_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            mPref = context.getSharedPreferences("legacy_prefs", android.content.Context.MODE_PRIVATE)
        }
    }

    fun setString(key: String?, value: String?) {
        mPref!!.edit().putString(key, value).apply()
    }

    fun getString(key: String?): String {
        return mPref!!.getString(key, "")!!
    }

    fun setBoolean(key: String, value: Boolean) {
        mPref!!.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String): Boolean {
        return mPref!!.getBoolean(key, false)
    }

    fun setInt(key: String?, value: Int) {
        mPref!!.edit().putInt(key, value).apply()
    }

    fun getInt(key: String): Int {
        return mPref!!.getInt(key, -1)
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return mPref!!.getInt(key, defaultValue)
    }

    fun remove(key: String?) {
        mPref!!.edit().remove(key).apply()
    }

    companion object {
        private var mPref: SharedPreferences? = null
        const val DEVICE_ID = "device_id"
        const val AUTH_TOKEN = "auth_token"
    }

}