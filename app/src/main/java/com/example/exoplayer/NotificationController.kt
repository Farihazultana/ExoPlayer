package com.example.exoplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import javax.security.auth.login.LoginException

class NotificationController : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            "Previous" -> {
                Toast.makeText(context, "Play Previous", Toast.LENGTH_SHORT).show()
                Log.i("Toast", "Play Previous ")
            }

            "Pause" -> {
                Toast.makeText(context, "Pause", Toast.LENGTH_SHORT).show()
                Log.i("Toast", "Pause ")
            }

            "Next" -> {
                Toast.makeText(context, "Play Next", Toast.LENGTH_SHORT).show()
                Log.i("Toast", "Play Next ")
            }
        }
    }
}