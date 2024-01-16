package com.example.exoplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi

class NotificationController : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            "Previous", "Pause", "Next" -> {
                // Handle the actions accordingly
                val serviceIntent = Intent(context, MusicPlayerService::class.java)
                serviceIntent.action = intent.action
                context?.startForegroundService(serviceIntent)
            }
        }
    }
}