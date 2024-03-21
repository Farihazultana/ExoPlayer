package com.example.exoplayer

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import com.example.exoplayer.databinding.CustomExoLayoutBinding


class MediaPlayerActivity : AppCompatActivity() {

    private val notificationReceiver: NotificationController = NotificationController()

    private lateinit var binding: CustomExoLayoutBinding

    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressTask: Runnable = object : Runnable {
        override fun run() {
            updateSeekbar()
            handler.postDelayed(this, PROGRESS_UPDATE_INTERVAL)
        }
    }

    private val PROGRESS_UPDATE_INTERVAL = 1000L

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CustomExoLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        // Set up UI controls
        binding.ivPlay.setOnClickListener {
            onPlayAction.playMusic()
            binding.ivPlay.visibility = View.GONE
            binding.ivPause.visibility = View.VISIBLE

        }

        binding.ivPause.setOnClickListener {
            onPlayAction.pauseMusic()
            binding.ivPlay.visibility = View.VISIBLE
            binding.ivPause.visibility = View.GONE
        }

        binding.ivPlayNext.setOnClickListener {
            onPlayAction.nextMusic()
        }

        binding.ivPlayPrev.setOnClickListener {
            onPlayAction.previousMusic()
        }

        binding.ivShuffle.setOnClickListener {
            onPlayAction.shuffleMusic()
        }

        // Track selector
        onPlayAction.trackSelector()
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

    companion object {
        lateinit var onPlayAction: PlayAction

        fun onPlayAction(setAction : PlayAction){
            this.onPlayAction = setAction
        }
    }

}