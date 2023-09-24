package com.example.mucsicplayerexercise4.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.mucsicplayerexercise4.ConstModel
import com.example.mucsicplayerexercise4.ConstModel.currentSongIndex
import com.example.mucsicplayerexercise4.ConstModel.songs
import com.example.mucsicplayerexercise4.R

class MusicService : Service() {
    private val binder = MusicBinder()
    private lateinit var mediaPlayer: MediaPlayer
//    private val songs = listOf(R.raw.aloi, R.raw.mien_man, R.raw.neu_luc_do)

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        mediaPlayer = MediaPlayer.create(this, ConstModel.songs[currentSongIndex].second)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "play" -> playMusic()
            "next" -> nextSong()
            "prev" -> prevSong()
        }
        return START_STICKY
    }

    private fun broadcastMusicStatus() {
        val intent = Intent("com.example.musicstatus")
            .apply { putExtra("isPlaying", mediaPlayer.isPlaying) }
        sendBroadcast(intent)
    }


    private fun playMusic() {
        val isPlaying = mediaPlayer.isPlaying
        if (isPlaying) {
            mediaPlayer.pause()
        } else {
            mediaPlayer.start()
        }
        handleMusicStatus(isPlaying = !isPlaying)
    }

    private fun nextSong() {
        changeSongIndex(increment = true)
        resetAndPlayMusic()
    }

    private fun prevSong() {
        changeSongIndex(increment = false)
        resetAndPlayMusic()
    }

    private fun changeSongIndex(increment: Boolean) {
        currentSongIndex = if (increment) {
            (currentSongIndex + 1) % songs.size
        } else {
            (currentSongIndex - 1 + songs.size) % songs.size
        }
    }

    private fun resetAndPlayMusic() {
        mediaPlayer.reset()
        mediaPlayer = MediaPlayer.create(this, songs[currentSongIndex].second)
        mediaPlayer.start()
        handleMusicStatus(isPlaying = true)
    }

    private fun handleMusicStatus(isPlaying: Boolean) {
        showNotification(isPlaying = isPlaying)
        broadcastMusicStatus()
    }

    private fun showNotification(message: String = songs[currentSongIndex].first,isPlaying : Boolean = true) {
        val playIntent = PendingIntent.getService(
            this, 0, Intent(this, MusicService::class.java)
                .apply { action = "play" }, PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = PendingIntent.getService(
            this, 0, Intent(this, MusicService::class.java)
                .apply { action = "next" }, PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = PendingIntent.getService(
            this, 0, Intent(this, MusicService::class.java)
                .apply { action = "prev" }, PendingIntent.FLAG_IMMUTABLE
        )

        val imageResource =
            if (isPlaying) R.drawable.ic_pause_black else R.drawable.ic_play_black

        val notification = NotificationCompat.Builder(this, "notification_channel_id")
            .setSmallIcon(R.drawable.baseline_music)
            .setContentTitle("Music Player")
            .setContentText(message)
            .addAction(R.drawable.ic_skip_previous_black, "previous", prevIntent)
            .addAction(imageResource, "play", playIntent)
            .addAction(R.drawable.ic_skip_next_black, "next", nextIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(1 /* #1: pause button \*/)
            )
            .setSilent(true)
            .build()

        startForeground(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Music Player"
            val descriptionText = "Music player notification channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("notification_channel_id", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService {
            return this@MusicService
        }
    }
}