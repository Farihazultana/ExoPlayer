package com.example.exoplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.exoplayer.databinding.ActivityMediaPlayerActvityBinding

class MediaPlayerActvity : AppCompatActivity() {
    lateinit var binding: ActivityMediaPlayerActvityBinding
    lateinit var player: ExoPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaPlayerActvityBinding.inflate(layoutInflater)

        setContentView(binding.root)


        val url = intent.getStringExtra("url")

        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player

        // Build the media item.
        val mediaItem = MediaItem.fromUri(url!!)
        // Set the media item to be played.
        player.setMediaItem(mediaItem)
        // Prepare the player.
        player.prepare()
        // Start the playback.
        player.play()

    }
}