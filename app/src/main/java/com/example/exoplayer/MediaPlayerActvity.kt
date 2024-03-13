package com.example.exoplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerNotificationManager.ACTION_NEXT
import androidx.media3.ui.PlayerNotificationManager.ACTION_PLAY
import androidx.media3.ui.PlayerNotificationManager.ACTION_PREVIOUS
import com.example.exoplayer.databinding.CustomExoLayoutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale


class MediaPlayerActvity : AppCompatActivity() {

    val notificationReceiver: NotificationController = NotificationController()

    private lateinit var binding2: CustomExoLayoutBinding
    private lateinit var player: ExoPlayer
    private var intent = Intent()

    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressTask: Runnable = object : Runnable {
        override fun run() {
            updateProgressBar()
            handler.postDelayed(this, PROGRESS_UPDATE_INTERVAL)
        }
    }
    private val PROGRESS_UPDATE_INTERVAL = 1000L

    /*private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            val message = intent?.getStringExtra("action")

            when (action) {
                "Pause" -> {
                    Toast.makeText(
                        context,
                        "Pause action received with message: $message",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.i("Action", "onReceive: $message")
                }

                else -> {
                    Log.i("Action", "onReceive: $message")
                }

            }
        }
    }*/

    object ExoPlayerSingleton {
        var player: ExoPlayer? = null

        fun setExoPlayerInstance(exoPlayer: ExoPlayer) {
            player = exoPlayer
        }

        fun getExoPlayerInstance(): ExoPlayer? {
            return player
        }
    }
    private fun startMusicPlayerService() {
        intent = Intent(this, MusicPlayerService::class.java)
        //intent.putExtra("exoPlayer", player)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            stopService(intent)
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding2 = CustomExoLayoutBinding.inflate(layoutInflater)
        setContentView(binding2.root)

        // Initialize player after setting content view
        player = ExoPlayer.Builder(this).build()

        // Set player in singleton class
        ExoPlayerSingleton.setExoPlayerInstance(player)

        startMusicPlayerService()

        val url1 = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4"
        val url2 = "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3"
        val url3 = "https://github.com/rafaelreis-hotmart/Audio-Sample-files/raw/master/sample.mp3"

        val firstItem = MediaItem.fromUri(url1!!)
        val secondItem = MediaItem.fromUri(url2!!)
        val thirdItem = MediaItem.fromUri(url3!!)

        // Set the media item to be played.
        ExoPlayerSingleton.getExoPlayerInstance()?.addMediaItem(firstItem)
        ExoPlayerSingleton.getExoPlayerInstance()?.addMediaItem(secondItem)
        ExoPlayerSingleton.getExoPlayerInstance()?.addMediaItem(thirdItem)

        // Prepare the player.
        ExoPlayerSingleton.getExoPlayerInstance()?.prepare()

        // SeekBar
        updateProgressBar()


        // Set up UI controls
        binding2.ivPlay.setOnClickListener {
            ExoPlayerSingleton.getExoPlayerInstance()?.play()
            binding2.ivPlay.visibility = View.GONE
            binding2.ivPause.visibility = View.VISIBLE
        }

        binding2.ivPause.setOnClickListener {
            ExoPlayerSingleton.getExoPlayerInstance()?.pause()
            binding2.ivPlay.visibility = View.VISIBLE
            binding2.ivPause.visibility = View.GONE
        }

        binding2.ivPlayNext.setOnClickListener {
            ExoPlayerSingleton.getExoPlayerInstance()?.seekToNextMediaItem()
        }

        binding2.ivPlayPrev.setOnClickListener {
            ExoPlayerSingleton.getExoPlayerInstance()?.seekToPreviousMediaItem()
        }

        binding2.ivShuffle.setOnClickListener {
            ExoPlayerSingleton.getExoPlayerInstance()?.shuffleModeEnabled = true
        }

        val filter = IntentFilter().apply {
            addAction("Pause")
            addAction("Next")
            addAction("Previous")
        }

        //LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter)

        // Track selector
        trackSelector()
    }


    private fun trackSelector() {
        player.addListener(
            object : Player.Listener {
                override fun onTracksChanged(tracks: Tracks) {
                    val audioList = mutableListOf<String>()
                    var language: String = ""
                    for (trackGroup in tracks.groups) {
                        val trackType = trackGroup.type

                        if (trackType == C.TRACK_TYPE_AUDIO) {
                            for (i in 0 until trackGroup.length) {
                                val trackFormat = trackGroup.getTrackFormat(i)
                                language =
                                    trackFormat.language ?: "und"

                                audioList.add("${audioList.size + 1}. " + Locale(language).displayLanguage)
                            }
                        }
                    }

                    if (audioList.isEmpty()) {
                        // No audio tracks available
                        return
                    }

                    if (audioList[0].contains("null")) {
                        audioList[0] = "1. Default Tracks"
                    }

                    val tempTracks = audioList.toTypedArray()

                    val audioDialog = MaterialAlertDialogBuilder(
                        this@MediaPlayerActvity,
                        R.style.Base_Theme_ExoPlayer
                    )
                        .setTitle("Select Language")
                        .setOnCancelListener { player.play() }
                        .setPositiveButton("Off Audio") { self, _ ->
                            // Handle turning off audio if needed
                            player.trackSelectionParameters =
                                player.trackSelectionParameters
                                    .buildUpon()
                                    .setMaxVideoSizeSd()
                                    .build()
                            self.dismiss()
                        }
                        .setItems(tempTracks) { _, position ->
                            // Handle selecting audio track
                            Toast.makeText(
                                this@MediaPlayerActvity,
                                audioList[position] + " Selected",
                                Toast.LENGTH_SHORT
                            ).show()

                            val selectedLanguage = Locale(language).language
                            player.trackSelectionParameters =
                                player.trackSelectionParameters
                                    .buildUpon()
                                    .setMaxVideoSizeSd()
                                    .setPreferredAudioLanguage(selectedLanguage)
                                    .build()
                        }
                        .create()

                    audioDialog.show()
                    audioDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN)
                    audioDialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))
                }
            }
        )
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
        unregisterReceiver(notificationReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        //unregisterReceiver(broadcastReceiver)
        unregisterReceiver(notificationReceiver)
    }


}