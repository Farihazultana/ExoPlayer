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
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.exoplayer.databinding.CustomExoLayoutBinding
import java.io.FileDescriptor

@OptIn(UnstableApi::class)
class MusicPlayerService : Service(), IBinder {
    val notificationReceiver: NotificationController = NotificationController()
    private val CHANNEL_ID = "Music Service Channel ID"

    private lateinit var binding: CustomExoLayoutBinding
    private lateinit var mediaSession: MediaSessionCompat

    private val binder = MusicPlayerBinder()
    inner class MusicPlayerBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter().apply {
            addAction("Pause")
            addAction("Next")
            addAction("Previous")
        }
        registerReceiver(notificationReceiver, filter)
        mediaSession = MediaSessionCompat(this, "MusicPlayerService")

        //initializePlayer()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        /*if (!this::player.isInitialized) {
            initializePlayer()
        }*/

        createNotificationChannel()
        val notification = createNotification()
        startForeground(1, notification)

        intent?.getStringExtra("action")?.let { action ->
            when (action) {
                "Previous" -> {
                    Toast.makeText(this, "Play Previous", Toast.LENGTH_SHORT).show()
                    onPlayAction.previousMusic()
                    //player.play()
                }
                "Pause" -> {
                    Toast.makeText(this, "Pause", Toast.LENGTH_SHORT).show()
                    onPlayAction.pauseMusic()
                }
                "Next" -> {
                    Toast.makeText(this, "Play Next", Toast.LENGTH_SHORT).show()
                    onPlayAction.nextMusic()
                    //player.play()
                }

                else -> {}
            }
        }

        return START_NOT_STICKY
    }


    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(notificationReceiver)
        onPlayAction.releasePlayer()
    }

    /*private fun releasePlayer() {
        if (this::player.isInitialized) {
            player.release()
        }
    }*/

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

    private fun createNotification(): Notification {
        val intent = Intent(this, MediaPlayerActvity::class.java)
        intent.putExtra("action", "Pause")
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_MUTABLE
        )


        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(0, 1, 2) // Index of playback controls (play, pause, stop)


        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText("Playing Music")
            .setSmallIcon(R.drawable.ic_music)
            .setContentIntent(pendingIntent)
            .setStyle(mediaStyle)
            .addAction(R.drawable.ic_skip_previous, "Previous", getPendingIntent("Previous"))
            .addAction(R.drawable.ic_pause, "Pause", getPendingIntent("Pause"))
            .addAction(R.drawable.ic_skip_next, "Next", getPendingIntent("Next"))

        return notificationBuilder.build()
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, NotificationController::class.java)
        intent.action = action

        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE )
    }

    override fun getInterfaceDescriptor(): String? {
        return null
    }

    override fun pingBinder(): Boolean {
        return false
    }

    override fun isBinderAlive(): Boolean {
        return false
    }

    override fun queryLocalInterface(descriptor: String): IInterface? {
        return null
    }

    override fun dump(fd: FileDescriptor, args: Array<out String>?) {

    }

    override fun dumpAsync(fd: FileDescriptor, args: Array<out String>?) {

    }

    override fun transact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        return false
    }

    override fun linkToDeath(recipient: IBinder.DeathRecipient, flags: Int) {

    }

    override fun unlinkToDeath(recipient: IBinder.DeathRecipient, flags: Int): Boolean {
        return false
    }

    companion object{
        lateinit var onPlayAction: PlayAction

        fun onPlayAction(setAction : PlayAction){
            this.onPlayAction = setAction
        }
    }


}
