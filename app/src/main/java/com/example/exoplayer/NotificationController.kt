package com.example.exoplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi

class NotificationController : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        val serviceIntent = Intent(context, MusicPlayerService::class.java)
        intent?.action?.let { action ->
            serviceIntent.putExtra("action", action)
            context?.startService(serviceIntent)
        }
    }
}