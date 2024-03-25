package com.example.exoplayer

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import com.example.exoplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMainBinding

    private lateinit var service: MusicPlayerService
    private var isServiceBound = false
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val musicPlayerBinder = service as MusicPlayerService.MusicPlayerBinder
            this@MainActivity.service = musicPlayerBinder.getService()
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false
        }

    }

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

    override fun onStart() {
        super.onStart()
        bindService(Intent(this, MusicPlayerService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        startMusicPlayerService()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(broadcastReceiver)
        //handler.removeCallbacks(updateProgressTask)
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }

    override fun onDestroy() {
        stopService(intent)
        super.onDestroy()
    }

    private fun startMusicPlayerService() {
        intent = Intent(this, MusicPlayerService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            stopService(intent)
        }
    }

    private fun goToPlayerPage(){
        val intent = Intent(this, MediaPlayerActivity::class.java)
        startActivity(intent)
    }

}