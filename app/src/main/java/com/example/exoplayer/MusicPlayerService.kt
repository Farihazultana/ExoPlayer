package com.example.exoplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.view.LayoutInflater
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.exoplayer.databinding.CustomExoLayoutBinding

class MusicPlayerService : Service() {
    private val CHANNEL_ID = "Music Service Channel ID"
    private lateinit var player: ExoPlayer
    private lateinit var binding: CustomExoLayoutBinding

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!this::player.isInitialized) {
            initializePlayer(intent)
        }

        createNotificationChannel()
        val notification = createNotification()
        startForeground(1, notification)

        return START_NOT_STICKY
    }



    private fun initializePlayer(intent: Intent?) {
        releasePlayer()
        val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = CustomExoLayoutBinding.inflate(layoutInflater)
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player

        val url1 = intent?.getStringExtra("url1") ?: ""
        val url2 = intent?.getStringExtra("url2") ?: ""
        val url3 = intent?.getStringExtra("url3") ?: ""

        val firstItem = MediaItem.fromUri(url1)
        val secondItem = MediaItem.fromUri(url2)
        val thirdItem = MediaItem.fromUri(url3)

        player.addMediaItem(firstItem)
        player.addMediaItem(secondItem)
        player.addMediaItem(thirdItem)

        player.playWhenReady = true
        player.prepare()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun releasePlayer() {
        if (this::player.isInitialized) {
            player.release()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Music Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MediaPlayerActvity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText("Playing music")
            .setSmallIcon(R.drawable.ic_music)
            .setContentIntent(pendingIntent)

        return notificationBuilder.build()
    }
}
