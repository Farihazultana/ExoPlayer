package com.example.exoplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.support.v4.media.session.MediaSessionCompat
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import java.io.FileDescriptor

@OptIn(UnstableApi::class)
class MusicPlayerService : Service(), IBinder, PlayAction {
    private val notificationReceiver: NotificationController = NotificationController()
    private val CHANNEL_ID = "Music Service Channel ID"

    private lateinit var mediaSession: MediaSessionCompat

    private val binder = MusicPlayerBinder()

    private lateinit var player: ExoPlayer


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

        initializePlayer()
        MediaPlayerActvity.onPlayAction(this)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        createNotificationChannel()
        val notification = createNotification()
        startForeground(1, notification)

        intent?.getStringExtra("action")?.let { action ->
            when (action) {
                "Previous" -> {
                    Toast.makeText(this, "Play Previous", Toast.LENGTH_SHORT).show()
                    previousMusic()
                }
                "Pause" -> {
                    Toast.makeText(this, "Pause", Toast.LENGTH_SHORT).show()
                    pauseMusic()
                }
                "Next" -> {
                    Toast.makeText(this, "Play Next", Toast.LENGTH_SHORT).show()
                    nextMusic()
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
        releasePlayer()
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

    override fun playerCurrentPosition() : Long {
        var currentPosition = 0L
        if (player.playbackState == Player.STATE_READY) {
            currentPosition = player.currentPosition
        }
        return currentPosition
    }


    override fun playerDuration() : Long{
        var duration = 0L
        if (player.playbackState == Player.STATE_READY) {
            duration = player.duration
        }
        return duration
    }

    override fun releasePlayer() {
        if (this::player.isInitialized) {
            player.release()
        }
    }

    override fun trackSelector() {
        /*player.addListener(
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
                        this@MusicPlayerService,
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
                                this@MusicPlayerService,
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
        )*/
    }

    override fun initializePlayer() : ExoPlayer{
        player = ExoPlayer.Builder(this).build()

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

        return player
    }

}
