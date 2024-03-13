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
import androidx.media3.common.Player
import javax.security.auth.login.LoginException

class NotificationController : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        /*val player = MediaPlayerActvity.ExoPlayerSingleton.getExoPlayerInstance()
        when (intent?.action) {
            "Previous" -> {
                Toast.makeText(context, "Play Previous", Toast.LENGTH_SHORT).show()
                Log.i("Toast", "Play Previous ")
                if (player != null) {
                    if (player.playbackState == Player.STATE_READY) {
                        player?.seekToPrevious()
                    }
                }

            }

            "Pause" -> {
                Toast.makeText(context, "Pause", Toast.LENGTH_SHORT).show()
                Log.i("Toast", "Pause ")
                if (player != null) {
                    if (player.playbackState == Player.STATE_READY) {
                        player?.pause()
                    }
                }

            }

            "Next" -> {
                Toast.makeText(context, "Play Next", Toast.LENGTH_SHORT).show()
                Log.i("Toast", "Play Next ")
                if (player != null) {
                    if (player.playbackState == Player.STATE_READY) {
                        player?.seekToNext()
                    }
                }

            }
        }*/

        val serviceIntent = Intent(context, MusicPlayerService::class.java)
        intent?.action?.let { action ->
            serviceIntent.putExtra("action", action)
            context?.startService(serviceIntent)
        }
    }
}