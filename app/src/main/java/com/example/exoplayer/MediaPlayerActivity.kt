package com.example.exoplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import com.example.exoplayer.databinding.CustomExoLayoutBinding


class MediaPlayerActivity : AppCompatActivity() {

    private val notificationReceiver: NotificationController = NotificationController()
    private val CHANNEL_ID = "Music Service Channel ID"
    private lateinit var mediaSession: MediaSessionCompat

    private lateinit var binding: CustomExoLayoutBinding

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

        if (onPlayAction.isPlaying()){
            binding.ivPlay.visibility = View.GONE
            binding.ivPause.visibility = View.VISIBLE
        }else{
            binding.ivPlay.visibility = View.VISIBLE
            binding.ivPause.visibility = View.GONE
        }

        // SeekBar
        updateSeekbar()

        //notification channel
        createNotificationChannel()

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

    private fun updateNotification(isPlaying: Boolean) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, createNotification(isPlaying))
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
    }

    private fun updateSeekbar(){
        val currentPosition = onPlayAction.playerCurrentPosition()
        val duration = onPlayAction.playerDuration()

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


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Music Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(isPlaying: Boolean): Notification {
        val intent = Intent(this, MediaPlayerActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_MUTABLE
        )


        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(0, 1, 2, 3)


        val playPauseIcon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        val playPause = if (isPlaying) "Pause" else "Play"

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText("Playing Music")
            .setSmallIcon(R.drawable.ic_music)
            .setContentIntent(pendingIntent)
            .setStyle(mediaStyle)
            .addAction(R.drawable.ic_skip_previous, "Previous", getPendingIntent("Previous"))
            .addAction(playPauseIcon, playPause, getPendingIntent(playPause))
            .addAction(R.drawable.ic_skip_next, "Next", getPendingIntent("Next"))

        return notificationBuilder.build()
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, NotificationController::class.java)
        intent.action = action

        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE )
    }

    companion object {
        lateinit var onPlayAction: PlayAction

        fun onPlayAction(setAction : PlayAction){
            this.onPlayAction = setAction
        }
    }

}