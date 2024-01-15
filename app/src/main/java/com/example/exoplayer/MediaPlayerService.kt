package com.example.exoplayer

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector


class MediaPlayerService : Service(){
    private var player: ExoPlayer? = null
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)

        val mediaUrl = intent?.getStringExtra("url")

        if(mediaUrl != null){
            playMedia(mediaUrl)
        }

        return START_STICKY
    }

    private fun playMedia(mediaUrl: String){
        releasePlayer()
        player = ExoPlayer.Builder(this)
            .setTrackSelector(DefaultTrackSelector(this))
            .build()

        val mediaItem = MediaItem.fromUri(mediaUrl)
        val mediaSource = ProgressiveMediaSource.Factory(
            DefaultDataSource.Factory(this)
        ).createMediaSource(mediaItem)

        player?.setMediaSource(mediaSource)
        player!!.prepare()

        player!!.playWhenReady = true
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }
}
