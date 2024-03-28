package com.example.exoplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat

object NotificationUtils {
    private val CHANNEL_ID = "Music Service Channel ID"
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             CHANNEL_ID
            val channelName = "Music Service Channel"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
                description = "Notification Channel for Music Service"
                enableLights(false)
                enableVibration(false)
            }
            val notificationManager =
                context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createNotification(
        context: Context,
        isPlaying: Boolean,
        currentPosition: Long,
        duration: Long
    ): Notification {
        val intent = Intent(context, MediaPlayerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_MUTABLE
        )

        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setShowActionsInCompactView(0, 1, 2, 3)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val playbackSpeed = if (isPlaying) 1F else 0F
            val mediaSession = MediaSessionCompat(context, "MusicPlayerService")
            mediaSession.setMetadata(
                MediaMetadataCompat.Builder()
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                    .build()
            )
            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setState(
                        if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                        currentPosition,
                        playbackSpeed
                    )
                    .setActions(
                        PlaybackStateCompat.ACTION_SEEK_TO or
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                                if (isPlaying) PlaybackStateCompat.ACTION_PAUSE else PlaybackStateCompat.ACTION_PLAY or
                                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    )
                    .build()
            )
        }

        val playPauseIcon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        val playPause = if (isPlaying) "Pause" else "Play"

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText("Playing Music")
            .setSmallIcon(R.drawable.ic_music)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.bitmap))
            .setContentIntent(pendingIntent)
            .setStyle(mediaStyle)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setProgress(duration.toInt(), currentPosition.toInt(), false)
            .addAction(R.drawable.ic_skip_previous, "Previous", getPendingIntent(context, "Previous"))
            .addAction(playPauseIcon, playPause, getPendingIntent(context, playPause))
            .addAction(R.drawable.ic_skip_next, "Next", getPendingIntent(context, "Next"))
            .setSound(Uri.EMPTY)



        return notificationBuilder.build()
    }

    private fun getPendingIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, NotificationController::class.java)
        intent.action = action
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE)
    }
}
