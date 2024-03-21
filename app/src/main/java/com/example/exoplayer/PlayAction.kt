package com.example.exoplayer

import androidx.media3.exoplayer.ExoPlayer

interface PlayAction {

    fun getPlayer() : ExoPlayer
    fun initializePlayer()
    fun playMusic()

    fun pauseMusic()

    fun previousMusic()

    fun nextMusic()

    fun isPlaying() : Boolean

    fun shuffleMusic()

    fun playerCurrentPosition() : Long

    fun playerDuration() : Long

    fun releasePlayer()

    fun trackSelector()
}