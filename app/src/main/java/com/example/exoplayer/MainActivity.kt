package com.example.exoplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.media3.exoplayer.ExoPlayer
import com.example.exoplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var isMediaPlayerServiceRunning = false
    private lateinit var binding: ActivityMainBinding

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val isAirplaneModeEnabled = intent?.getBooleanExtra("state", false) ?: return

            // checking whether airplane mode is enabled or not
            if (isAirplaneModeEnabled) {
                // showing the toast message if airplane mode is enabled
                Toast.makeText(context, "Airplane Mode Enabled", Toast.LENGTH_LONG).show()
                binding.btnAirplane.text = "Airplane Mode Enabled"
                binding.btnAirplane.setBackgroundColor( resources.getColor(R.color.red))
            } else {
                // showing the toast message if airplane mode is disabled
                Toast.makeText(context, "Airplane Mode Disabled", Toast.LENGTH_LONG).show()
                binding.btnAirplane.text = "Airplane Mode Disabled"
                binding.btnAirplane.setBackgroundColor( resources.getColor(R.color.grey))
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED).also {

            registerReceiver(broadcastReceiver, it)
        }

        binding.btn1.setOnClickListener {
            goToPlayerPage()
        }

    }

    private fun goToPlayerPage(){
        val intent = Intent(this, MediaPlayerActvity::class.java)
        startActivity(intent)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(broadcastReceiver)
    }
}