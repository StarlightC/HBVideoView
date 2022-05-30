package com.starlightc.videoview

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.starlightc.videoview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initVideoView()
    }

    private fun initVideoView() {
        val videoTestUrl = "https://www.w3school.com.cn/example/html5/mov_bbb.mp4"
        binding.videoview.post {
            binding.videoview.addVideoAndSelect("testUri", Uri.parse(videoTestUrl))
            binding.videoview.prepare()
        }
    }
}