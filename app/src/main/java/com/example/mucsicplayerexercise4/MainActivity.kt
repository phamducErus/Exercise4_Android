package com.example.mucsicplayerexercise4

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mucsicplayerexercise4.databinding.ActivityMainBinding
import com.example.mucsicplayerexercise4.service.MusicService

class MainActivity : AppCompatActivity() {
    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.play.setOnClickListener {
            val intent = Intent(this, MusicService::class.java)
                .apply { action = "play" }
            startService(intent)
        }

        binding.skipNext.setOnClickListener {
            val intent = Intent(this, MusicService::class.java)
                .apply { action = "next" }
            startService(intent)
        }

        binding.skipPrevious.setOnClickListener {
            val intent = Intent(this, MusicService::class.java)
                .apply { action = "prev" }
            startService(intent)
        }

        binding.trackName.text = ConstModel.songs[ConstModel.currentSongIndex].first
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(musicStatusReceiver, IntentFilter("com.example.musicstatus"))
        registerReceiver(networkReceiver, IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(musicStatusReceiver)
        unregisterReceiver(networkReceiver)
    }

    private val musicStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val isPlaying = intent?.getBooleanExtra("isPlaying", false) ?: false
            val imageResource =
                if (isPlaying) R.drawable.ic_pause_black else R.drawable.ic_play_black
            binding.play.setImageResource(imageResource)
            binding.trackName.text = ConstModel.songs[ConstModel.currentSongIndex].first
        }

    }

    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val isConnected =
                (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
                    getNetworkCapabilities(activeNetwork)?.run {
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                                || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                                || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    } ?: false
                }
            if (isConnected) {
                Toast.makeText(this@MainActivity, "Connected", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Disconnected", Toast.LENGTH_SHORT).show()
            }
        }
    }
}