package com.example.exoplayer

interface PlayAction {
    fun playMusic()

    fun pauseMusic()

    fun previousMusic()

    fun nextMusic()

    fun shuffleMusic()

    fun updateSeekbar()

    fun releasePlayer()

    fun trackSelector()
}