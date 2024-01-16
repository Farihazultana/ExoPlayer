package com.example.exoplayer

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.media3.exoplayer.ExoPlayer
import com.example.exoplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var isMediaPlayerServiceRunning = false
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btn1.setOnClickListener {
            goToPlayerPage()
        }

    }

    private fun goToPlayerPage(){
        val intent = Intent(this, MediaPlayerActvity::class.java)
        startActivity(intent)
    }
}