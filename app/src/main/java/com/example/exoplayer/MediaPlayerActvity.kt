package com.example.exoplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.example.exoplayer.databinding.ActivityMediaPlayerActvityBinding

class MediaPlayerActvity : AppCompatActivity() {
    lateinit var binding: ActivityMediaPlayerActvityBinding
    lateinit var player: ExoPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaPlayerActvityBinding.inflate(layoutInflater)

        setContentView(binding.root)


        val url1 = intent.getStringExtra("url1")
        val url2 = intent.getStringExtra("url2")
        val url3 = intent.getStringExtra("url3")

        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player

        /*val mediaItem =
            MediaItem.Builder().setUri(url).setMimeType(MimeTypes.APPLICATION_MP4).build()

        val mediaSystem = ProgressiveMediaSource.Factory(DefaultDataSource.Factory(this))
            .createMediaSource(mediaItem)

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()*/

        // Build the media item.
        //val mediaItem = MediaItem.fromUri(url!!)
        val firstItem = MediaItem.fromUri(url1!!)
        val secondItem = MediaItem.fromUri(url2!!)
        val thirdItem = MediaItem.fromUri(url3!!)
        // Set the media item to be played.
        //player.setMediaItem(mediaItem)
        player.addMediaItem(firstItem)
        player.addMediaItem(secondItem)
        player.addMediaItem(thirdItem)
        // Prepare the player.
        player.prepare()
        // Start the playback.
        player.play()

    }
}