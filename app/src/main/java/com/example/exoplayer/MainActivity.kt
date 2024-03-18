package com.example.exoplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import com.example.exoplayer.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

class MainActivity : AppCompatActivity() , PlayAction{
    private var isMediaPlayerServiceRunning = false
    private lateinit var binding: ActivityMainBinding
    private lateinit var player: ExoPlayer

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val isAirplaneModeEnabled = intent?.getBooleanExtra("state", false) ?: return

            // checking whether airplane mode is enabled or not
            if (isAirplaneModeEnabled) {
                // showing the toast message if airplane mode is enabled
                Toast.makeText(context, "Airplane Mode Enabled", Toast.LENGTH_LONG).show()
                binding.btnAirplane.text = "Airplane Mode Enabled"
                binding.btnAirplane.setBackgroundColor( resources.getColor(R.color.red))
            } else {
                // showing the toast message if airplane mode is disabled
                Toast.makeText(context, "Airplane Mode Disabled", Toast.LENGTH_LONG).show()
                binding.btnAirplane.text = "Airplane Mode Disabled"
                binding.btnAirplane.setBackgroundColor( resources.getColor(R.color.grey))
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED).also {

            registerReceiver(broadcastReceiver, it)
        }

        binding.btn1.setOnClickListener {
            goToPlayerPage()
        }

        initializePlayer()
        MediaPlayerActvity.onPlayAction(this)
        MusicPlayerService.onPlayAction(this)

    }

    private fun goToPlayerPage(){
        val intent = Intent(this, MediaPlayerActvity::class.java)
        startActivity(intent)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(broadcastReceiver)
    }

    override fun playMusic() {
        player.play()
    }

    override fun pauseMusic() {
        player.pause()
    }

    override fun previousMusic() {
        player.seekToPreviousMediaItem()
    }

    override fun nextMusic() {
        player.seekToNextMediaItem()
    }

    override fun shuffleMusic() {
        player.shuffleModeEnabled = true
    }

    override fun updateSeekbar() {
        updateProgressBar()
    }

    override fun releasePlayer() {
        if (this::player.isInitialized) {
            player.release()
        }
    }

    override fun trackSelector() {
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
                        this@MainActivity,
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
                                this@MainActivity,
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

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build()
        //binding2.playerView.player = player


        val url1 = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4"
        val url2 = "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3"
        val url3 = "https://github.com/rafaelreis-hotmart/Audio-Sample-files/raw/master/sample.mp3"


        val firstItem = MediaItem.fromUri(url1!!)
        val secondItem = MediaItem.fromUri(url2!!)
        val thirdItem = MediaItem.fromUri(url3!!)

        // Set the media item to be played.
        player.addMediaItem(firstItem)
        player.addMediaItem(secondItem)
        player.addMediaItem(thirdItem)

        // Prepare the player.
        player.prepare()
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
            //findViewById<SeekBar>(R.id.seekBar).progress = currentPosition.toInt()
            //findViewById<SeekBar>(R.id.seekBar).max = duration.toInt()
            //findViewById<TextView>(R.id.seekBar_Start).text = String.format("%02d:%02d", currentMinutes, currentSeconds)
            //findViewById<TextView>(R.id.seekbar_End).text = String.format("%02d:%02d", totalMinutes, totalSeconds)
        }
    }

}