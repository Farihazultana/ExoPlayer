package com.example.exoplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.example.exoplayer.databinding.ActivityMediaPlayerActvityBinding
import com.example.exoplayer.databinding.CustomExoLayoutBinding

class MediaPlayerActvity : AppCompatActivity() {
    private lateinit var binding: ActivityMediaPlayerActvityBinding
    private lateinit var binding2: CustomExoLayoutBinding
    private lateinit var player: ExoPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaPlayerActvityBinding.inflate(layoutInflater)
        binding2 = CustomExoLayoutBinding.inflate(layoutInflater)
        setContentView(binding2.root)


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

        binding2.ivPlay.setOnClickListener {
            player.play()
            binding2.ivPlay.visibility = View.GONE
            binding2.ivPause.visibility = View.VISIBLE
        }

        binding2.ivPause.setOnClickListener {
            player.pause()
            binding2.ivPlay.visibility = View.VISIBLE
            binding2.ivPause.visibility = View.GONE
        }

        binding2.ivPlayNext.setOnClickListener {
            player.seekForward()
        }
        binding2.ivPlayNext.setOnClickListener {
            player.seekBack()
        }

        binding2.ivShuffle.setOnClickListener {
            player.shuffleModeEnabled = true
        }

    }
}