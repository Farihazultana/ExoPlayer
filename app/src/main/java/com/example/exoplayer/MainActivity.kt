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

        val intent = Intent(this, MusicPlayerService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            stopService(intent)
        }

        binding.btn1.setOnClickListener {
            /*if (!isMediaPlayerServiceRunning){
                val url =
                    "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4"

                val intent = Intent(this, MediaPlayerService::class.java)
                intent.putExtra("url", url)

                startActivity(intent)
                isMediaPlayerServiceRunning = true
            }*/

            val url1 = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4"
            val url2 = "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3"
            //val url2 = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4"
            val url3 = "https://github.com/rafaelreis-hotmart/Audio-Sample-files/raw/master/sample.mp3"

            goToPlayerPage(url1, url2, url3)

        }

        binding.btn2.setOnClickListener {
            val url = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4"

            //goToPlayerPage(url)
        }

        binding.btn3.setOnClickListener {
            val url = "https://github.com/rafaelreis-hotmart/Audio-Sample-files/raw/master/sample.mp3"

            //goToPlayerPage(url)
        }

    }

    private fun goToPlayerPage(url1:String, url2: String, url3: String){
        val intent = Intent(this, MediaPlayerActvity::class.java)
        intent.putExtra("url1", url1)
        intent.putExtra("url2", url2)
        intent.putExtra("url3", url3)

        startActivity(intent)
    }
}