package com.example.exoplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.exoplayer.databinding.CustomExoLayoutBinding

class MusicPlayerService : Service() {
    private val CHANNEL_ID = "Music Service Channel ID"
    private lateinit var player: ExoPlayer
    private lateinit var binding: CustomExoLayoutBinding
    private lateinit var mediaSession: MediaSessionCompat

    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSessionCompat(this, "MusicPlayerService")

        mediaSession.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )
        mediaSession.setCallback(MediaSessionCallback())
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            player.play()
            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, player.currentPosition, 1.0f)
                    .build()
            )
        }

        override fun onPause() {
            player.pause()
            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PAUSED, player.currentPosition, 1.0f)
                    .build()
            )
        }

        override fun onSkipToNext() {
            player.seekToNext()
        }

        override fun onSkipToPrevious() {
            player.seekToPrevious()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!this::player.isInitialized) {
            initializePlayer(intent)
        }

        when (intent?.action) {
            "Previous" -> player.seekToPrevious()
            "Pause" -> player.playWhenReady = false
            "Next" -> player.seekToNext()
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

        val url1 = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4"
        val url2 = "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3"
        val url3 = "https://github.com/rafaelreis-hotmart/Audio-Sample-files/raw/master/sample.mp3"

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


        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(0, 1, 2) // Index of playback controls (play, pause, stop)


        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText("Playing music")
            .setSmallIcon(R.drawable.ic_music)
            .setContentIntent(pendingIntent)
            .setStyle(mediaStyle)
            .addAction(R.drawable.ic_skip_previous, "Previous", getPendingIntent("Previous"))
            .addAction(R.drawable.ic_pause, "Pause", getPendingIntent("Pause"))
            .addAction(R.drawable.ic_skip_next, "Next", getPendingIntent("Next"))

        return notificationBuilder.build()
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, NotificationController::class.java)
        intent.action = action
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }
}
