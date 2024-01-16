package com.example.exoplayer

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.exoplayer.databinding.ActivityMediaPlayerActvityBinding
import com.example.exoplayer.databinding.CustomExoLayoutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

class MediaPlayerActvity : AppCompatActivity() {

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

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding2 = CustomExoLayoutBinding.inflate(layoutInflater)
        setContentView(binding2.root)

        // Initialize player after setting content view
        player = ExoPlayer.Builder(this).build()
        binding2.playerView.player = player

        intent = Intent(this, MusicPlayerService::class.java)

        val url1 = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4"
        val url2 = "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3"
        val url3 = "https://github.com/rafaelreis-hotmart/Audio-Sample-files/raw/master/sample.mp3"


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
                    val seekTo = (progress)
                    player.seekTo(seekTo.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(updateProgressTask)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                handler.post(updateProgressTask)
            }
        })

        val audioTrack = ArrayList<String>()
        val audioList = ArrayList<String>()


        // Inside onTracksChanged function
        binding2.ivShuffle.setOnClickListener {
            trackSelector()
        }


        binding2.ivPlay.setOnClickListener {
            player.play()
            binding2.ivPlay.visibility = View.GONE
            binding2.ivPause.visibility = View.VISIBLE

            //notification

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                stopService(intent)
            }
        }

        binding2.ivPause.setOnClickListener {
            player.pause()
            binding2.ivPlay.visibility = View.VISIBLE
            binding2.ivPause.visibility = View.GONE

            //stopService(intent)

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
    }


}