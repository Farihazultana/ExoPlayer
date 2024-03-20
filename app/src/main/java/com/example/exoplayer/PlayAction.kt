package com.example.exoplayer

import androidx.media3.exoplayer.ExoPlayer

interface PlayAction {

    fun initializePlayer() : ExoPlayer
    fun playMusic()

    fun pauseMusic()

    fun previousMusic()

    fun nextMusic()

    fun shuffleMusic()

    fun playerCurrentPosition() : Long

    fun playerDuration() : Long

    fun releasePlayer()

    fun trackSelector()
}