package com.example.exoplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.example.exoplayer.databinding.CustomExoLayoutBinding


class MediaPlayerActivity : AppCompatActivity() {

    private val notificationReceiver: NotificationController = NotificationController()
    private val CHANNEL_ID = "Music Service Channel ID"
    private lateinit var mediaSession: MediaSessionCompat


    private lateinit var binding: CustomExoLayoutBinding

    private var currentPosition : Long = 0L
    private var duration : Long = 0L

    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressTask: Runnable = object : Runnable {
        override fun run() {
            updateSeekbar()
            handler.postDelayed(this, PROGRESS_UPDATE_INTERVAL)
        }
    }

    private val PROGRESS_UPDATE_INTERVAL = 1000L

    private val playbackStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (it.action == "PlaybackState") {
                    val isPlaying = it.getBooleanExtra("isPlaying", false)
                    updatePlayPauseButton(isPlaying)
                    updateNotification(isPlaying)
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CustomExoLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //notification channel
        NotificationUtils.createNotificationChannel(this)

        val filter = IntentFilter().apply {
            addAction("Play")
            addAction("Pause")
            addAction("Next")
            addAction("Previous")
        }
        registerReceiver(notificationReceiver, filter)
        registerReceiver(playbackStateReceiver, IntentFilter("PlaybackState"))
        mediaSession = MediaSessionCompat(this, "MusicPlayerService")



        binding.playerView.player = onPlayAction.getPlayer()


        onPlayAction.getPlayer().addListener(object : Player.Listener {
            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                super.onMediaMetadataChanged(mediaMetadata)
                Log.d("metadata", mediaMetadata.title.toString())

                binding.tvSong.text = mediaMetadata.title.toString()
                binding.tvSinger.text = mediaMetadata.albumArtist.toString()
            }
        })

        // SeekBar
        updateSeekbar()



        // Set up UI controls
        binding.ivPlay.setOnClickListener {
            onPlayAction.playMusic()
            binding.ivPlay.visibility = View.GONE
            binding.ivPause.visibility = View.VISIBLE


            updateNotification(onPlayAction.isPlaying())


        }

        binding.ivPause.setOnClickListener {
            onPlayAction.pauseMusic()
            binding.ivPlay.visibility = View.VISIBLE
            binding.ivPause.visibility = View.GONE

            updateNotification(onPlayAction.isPlaying())
        }

        binding.ivPlayNext.setOnClickListener {
            onPlayAction.nextMusic()

            updateNotification(onPlayAction.isPlaying())
        }

        binding.ivPlayPrev.setOnClickListener {
            onPlayAction.previousMusic()

            updateNotification(onPlayAction.isPlaying())
        }

        binding.ivShuffle.setOnClickListener {
            onPlayAction.shuffleMusic()

            updateNotification(onPlayAction.isPlaying())
        }

        // Track selector
        onPlayAction.trackSelector()
    }

    override fun onResume() {
        super.onResume()
        updateNotification(onPlayAction.isPlaying())
        updatePlayPauseButton(onPlayAction.isPlaying())
    }


    override fun onStart() {
        super.onStart()
        handler.post(updateProgressTask)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(updateProgressTask)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(notificationReceiver)
        unregisterReceiver(playbackStateReceiver)
        onPlayAction.releasePlayer()
    }

    private fun updateSeekbar(){
        currentPosition = onPlayAction.playerCurrentPosition()
        duration = onPlayAction.playerDuration()

        // Calculate minutes and seconds for current position
        val currentMinutes = currentPosition / 1000 / 60
        val currentSeconds = (currentPosition / 1000) % 60

        // Calculate minutes and seconds for total duration
        val totalMinutes = duration / 1000 / 60
        val totalSeconds = (duration / 1000) % 60

        Log.d("Player", "Current time: $currentMinutes:$currentSeconds, Total time: $totalMinutes:$totalSeconds")

        // Update UI elements
        binding.seekBar.progress = currentPosition.toInt()
        binding.seekBar.max = duration.toInt()
        binding.seekBarStart.text = String.format("%02d:%02d", currentMinutes, currentSeconds)
        binding.seekbarEnd.text = String.format("%02d:%02d", totalMinutes, totalSeconds)



    }


    private fun updatePlayPauseButton(isPlaying: Boolean) {
        if (isPlaying) {
            binding.ivPlay.visibility = View.GONE
            binding.ivPause.visibility = View.VISIBLE
        } else {
            binding.ivPlay.visibility = View.VISIBLE
            binding.ivPause.visibility = View.GONE
        }
    }



    private fun updateNotification(isPlaying: Boolean) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, NotificationUtils.createNotification(this,mediaSession, isPlaying, currentPosition, duration))
    }

    companion object {
        lateinit var onPlayAction: PlayAction
            private set

        fun setOnPlayAction(action: PlayAction) {
            onPlayAction = action
        }
    }

}