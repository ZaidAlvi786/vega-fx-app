package airsign.signage.player.ui

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import airsign.signage.player.R

class CrashActivity : AppCompatActivity() {
    private val TAG = "CrashActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash)

        val message = findViewById<TextView>(R.id.crash_message)
        message.text = intent.getStringExtra("message")

        Log.d(TAG, "message received !!!!")

    }
}