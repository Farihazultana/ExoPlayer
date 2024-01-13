package com.example.exoplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.SeekBar
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.exoplayer.databinding.ActivityMediaPlayerActvityBinding
import com.example.exoplayer.databinding.CustomExoLayoutBinding

class MediaPlayerActvity : AppCompatActivity() {
    private lateinit var binding: ActivityMediaPlayerActvityBinding
    private lateinit var binding2: CustomExoLayoutBinding
    private lateinit var player: ExoPlayer

    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressTask: Runnable = object : Runnable {
        override fun run() {
            updateProgressBar()
            handler.postDelayed(this, PROGRESS_UPDATE_INTERVAL)
        }
    }
    private val PROGRESS_UPDATE_INTERVAL = 1000L

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaPlayerActvityBinding.inflate(layoutInflater)
        binding2 = CustomExoLayoutBinding.inflate(layoutInflater)
        setContentView(binding2.root)


        val url1 = intent.getStringExtra("url1")
        val url2 = intent.getStringExtra("url2")
        val url3 = intent.getStringExtra("url3")

        player = ExoPlayer.Builder(this).build()
        binding2.playerView.player = player

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

        // SeekBar
        updateProgressBar()

        binding2.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val seekTo = (progress * 1000L)
                    player.seekTo(seekTo)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(updateProgressTask)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                handler.post(updateProgressTask)
            }
        })


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
            player.seekToNextMediaItem()
        }
        binding2.ivPlayPrev.setOnClickListener {
            player.seekToPreviousMediaItem()
        }

        binding2.ivShuffle.setOnClickListener {
            player.shuffleModeEnabled = true
        }

    }

    private fun updateProgressBar() {
        if (player.playbackState == Player.STATE_READY) {
            val currentPosition = player.currentPosition
            val duration = player.duration

            // Calculate minutes and seconds for current position
            val currentMinutes = currentPosition / 1000 / 60
            val currentSeconds = (currentPosition / 1000) % 60

            // Calculate minutes and seconds for total duration
            val totalMinutes = duration / 1000 / 60
            val totalSeconds = (duration / 1000) % 60

            // Update UI elements
            binding2.seekBar.progress = currentPosition.toInt()
            binding2.seekBar.max = duration.toInt()
            binding2.seekBarStart.text = String.format("%02d:%02d", currentMinutes, currentSeconds)
            binding2.seekbarEnd.text = String.format("%02d:%02d", totalMinutes, totalSeconds)
        }
    }



    override fun onStart() {
        super.onStart()
        handler.post(updateProgressTask)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(updateProgressTask)
    }

}