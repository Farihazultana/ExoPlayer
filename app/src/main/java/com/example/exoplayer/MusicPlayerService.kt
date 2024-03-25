package com.example.exoplayer

import android.R.attr.path
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
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

    private var isPlaying: Boolean = false

    private var currentPosition : Long = 0L
    private var duration : Long = 0L

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
        MediaPlayerActivity.setOnPlayAction(this)

        startForeground(1, createNotification(isPlaying))

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

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
                "Play" -> {
                    Toast.makeText(this, "Play", Toast.LENGTH_SHORT).show()
                    playMusic()

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
        //startForeground(1, createNotification(isPlaying))
        player.play()
        isPlaying = true
        notifyPlaybackStateChanged()
    }

    override fun pauseMusic() {
        player.pause()
        isPlaying = false
        notifyPlaybackStateChanged()
    }

    override fun previousMusic() {
        player.seekToPreviousMediaItem()
        notifyPlaybackStateChanged()
    }

    override fun nextMusic() {
        player.seekToNextMediaItem()
        notifyPlaybackStateChanged()
    }

    override fun isPlaying() : Boolean {
        return player.isPlaying
    }

    override fun shuffleMusic() {
        player.shuffleModeEnabled = true
    }

    override fun playerCurrentPosition() : Long {
        currentPosition = 0L
        if (player.playbackState == Player.STATE_READY) {
            currentPosition = player.currentPosition
        }
        return currentPosition
    }


    override fun playerDuration() : Long{
        duration = 0L
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

    override fun initializePlayer() {
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

    }

    override fun getPlayer(): ExoPlayer {
        return player
    }

    private fun notifyPlaybackStateChanged() {
        val intent = Intent("PlaybackState")
        intent.putExtra("isPlaying", isPlaying)
        sendBroadcast(intent)
        updateNotification(isPlaying)
    }

    private fun updateNotification(isPlaying: Boolean) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, createNotification(isPlaying))
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
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.bitmap))
            .setContentIntent(pendingIntent)
            .setStyle(mediaStyle)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setProgress(duration.toInt(), currentPosition.toInt(), false)
            .addAction(R.drawable.ic_skip_previous, "Previous", getPendingIntent("Previous"))
            .addAction(playPauseIcon, playPause, getPendingIntent(playPause))
            .addAction(R.drawable.ic_skip_next, "Next", getPendingIntent("Next"))
            .setSound(Uri.EMPTY)

        return notificationBuilder.build()
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, NotificationController::class.java)
        intent.action = action

        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE )
    }

}
