package com.example.exoplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.exoplayer.databinding.CustomExoLayoutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale


class MediaPlayerActvity : AppCompatActivity() {

    val notificationReceiver: NotificationController = NotificationController()
    private lateinit var service: MusicPlayerService
    private var isServiceBound = false

    private lateinit var binding2: CustomExoLayoutBinding
    private var intent = Intent()

    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressTask: Runnable = object : Runnable {
        override fun run() {
            onPlayAction.updateSeekbar()
            handler.postDelayed(this, PROGRESS_UPDATE_INTERVAL)
        }
    }

    private val PROGRESS_UPDATE_INTERVAL = 1000L

    lateinit var url1 : String
    lateinit var url2 : String
    lateinit var url3 : String

    private val serviceConnection = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val musicPlayerBinder = service as MusicPlayerService.MusicPlayerBinder
            this@MediaPlayerActvity.service = musicPlayerBinder.getService()
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false
        }

    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding2 = CustomExoLayoutBinding.inflate(layoutInflater)
        setContentView(binding2.root)

        //startMusicPlayerService()


        // SeekBar
        onPlayAction.updateSeekbar()


        // Set up UI controls
        binding2.ivPlay.setOnClickListener {
            onPlayAction.playMusic()
            binding2.ivPlay.visibility = View.GONE
            binding2.ivPause.visibility = View.VISIBLE
        }

        binding2.ivPause.setOnClickListener {
            onPlayAction.pauseMusic()
            binding2.ivPlay.visibility = View.VISIBLE
            binding2.ivPause.visibility = View.GONE
        }

        binding2.ivPlayNext.setOnClickListener {
            onPlayAction.nextMusic()
        }

        binding2.ivPlayPrev.setOnClickListener {
            onPlayAction.previousMusic()
        }

        binding2.ivShuffle.setOnClickListener {
            onPlayAction.shuffleMusic()
        }

        val filter = IntentFilter().apply {
            addAction("Pause")
            addAction("Next")
            addAction("Previous")
        }

        //LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter)

        // Track selector
        onPlayAction.trackSelector()
    }



    private fun startMusicPlayerService() {
        intent = Intent(this, MusicPlayerService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            stopService(intent)
        }
    }



    override fun onStart() {
        super.onStart()
        handler.post(updateProgressTask)
        bindService(Intent(this, MusicPlayerService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        startMusicPlayerService()
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(updateProgressTask)
        unregisterReceiver(notificationReceiver)
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //unregisterReceiver(broadcastReceiver)
        unregisterReceiver(notificationReceiver)
    }

    companion object{
        lateinit var onPlayAction: PlayAction

        fun onPlayAction(setAction : PlayAction){
            this.onPlayAction = setAction
        }
    }

}