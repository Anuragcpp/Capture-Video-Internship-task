package com.example.cameraapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.VideoView
import com.example.cameraapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var recordBtn : Button
    private lateinit var videoView: VideoView
    private val REQUEST_CODE_VIDEO_CAPTURE : Int = 206
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //video view
        videoView = binding.videoView

        //record button
        recordBtn = binding.recordBtn
        recordBtn.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            if(intent.resolveActivity(packageManager) != null){
                startActivityForResult(intent,REQUEST_CODE_VIDEO_CAPTURE);
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_CODE_VIDEO_CAPTURE && resultCode == RESULT_OK){
            val videoUri : Uri? = data?.data
            videoView.setVideoURI(videoUri)
            videoView.start()
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}