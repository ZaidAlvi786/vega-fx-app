package airsign.signage.player.ui

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import airsign.signage.player.R

import android.widget.Button
import android.content.Intent

class CrashActivity : AppCompatActivity() {
    private val TAG = "CrashActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash)

        val message = findViewById<TextView>(R.id.crash_message)
        message.text = intent.getStringExtra("message")

        findViewById<Button>(R.id.btn_restart).setOnClickListener {
            Log.d(TAG, "Restart button clicked - relaunching player")
            val intent = Intent(this, SplashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        Log.d(TAG, "message received !!!!")

    }
}