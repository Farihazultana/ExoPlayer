package com.example.exoplayer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.media3.exoplayer.ExoPlayer
import com.example.exoplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val player = ExoPlayer.Builder(this).build()

        binding.btn1.setOnClickListener {
            val url =
                "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4"

            goToPlayerPage(url)
        }

        binding.btn2.setOnClickListener {
            val url = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4"

            goToPlayerPage(url)
        }

        binding.btn3.setOnClickListener {
            val url = "https://github.com/rafaelreis-hotmart/Audio-Sample-files/raw/master/sample.mp3"

            goToPlayerPage(url)
        }

    }

    private fun goToPlayerPage(url:String){
        val intent = Intent(this, MediaPlayerActvity::class.java)
        intent.putExtra("url", url)

        startActivity(intent)
    }
}