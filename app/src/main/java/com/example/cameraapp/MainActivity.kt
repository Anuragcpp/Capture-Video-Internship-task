package com.example.cameraapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.cameraapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var recordBtn : Button
    private lateinit var videoView: VideoView
    private var customProgressDialog : Dialog? = null


    private val videoRecorderLouncher : ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
            if(result.resultCode == RESULT_OK && result.data?.data != null){

                //launching  the coroutine
                lifecycleScope.launch {
                    showProgressDialog()
                    saveVideoToInternalStorage( result.data?.data!!)
                }
            }
        }

    private val videoRecorderPermissionLouncher :ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ){
            permission ->

            if( permission ) {
                Toast.makeText(this,"Permission Granted for External storage ", Toast.LENGTH_SHORT).show()
                val videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                videoRecorderLouncher.launch(videoIntent)
            }else{
                Toast.makeText(this,"Permission Denied for External storage ", Toast.LENGTH_SHORT).show()
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //video view
        videoView = binding.videoView

        //record button
        recordBtn = binding.recordBtn
        recordBtn.setOnClickListener {
            requenstStoragePermission()
        }

    }

    private fun requenstStoragePermission () {
        if(ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )){
            showRationalDialog("Camera App", "Camera App Needs  to access your external storage")
        }else{
            //TODO lonch the videoCapture
            videoRecorderPermissionLouncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    // Aleart dialog for custom massage
    private fun showRationalDialog(title : String, message : String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)

        builder.setNeutralButton("Cancle"){dialog , _->
            dialog.dismiss()
        }
        builder.setPositiveButton("Ok"){dialog, _->
            dialog.dismiss()
        }
    }

    // fuction to save the video in the external storage

    private suspend fun saveVideoToInternalStorage(videoUri: Uri) {
        var result : String = ""

        // setting up the coroutine with IO dispatcher
        withContext(Dispatchers.IO){
            try {
                val newfile: File
                val videoAsset = contentResolver.openAssetFileDescriptor(videoUri, "r")
                val `in` = videoAsset!!.createInputStream()
                val filepath = Environment.getExternalStorageDirectory()
                val dir = File(filepath.absolutePath + "/" + "CameraApp video " + "/")
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                newfile = File(dir, "save_" + System.currentTimeMillis() + ".mp4")
                if (newfile.exists()) newfile.delete()
                val out: OutputStream = FileOutputStream(newfile)

                // Copy the bits from instream to outstream
                val buf = ByteArray(1024)
                var len: Int
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
                `in`.close()
                out.close()

                // the absolute path where video is saved
                result = newfile.absolutePath
                runOnUiThread{
                    cancleProgressBarDialog()
                    Log.v("", "Copy file successful.")

                    Toast.makeText(this@MainActivity,"Video Saved successfully on location  : $result ", Toast.LENGTH_SHORT).show()
                    videoView.setVideoURI(videoUri)
                    videoView.start()

                    // setting up the pause play button for the video
                    val mediaController : MediaController = MediaController(this@MainActivity)
                    mediaController.setAnchorView(videoView)
                    videoView.setMediaController(mediaController)

                }

            } catch (e: Exception) {
                result =""
                e.printStackTrace()
            }
        }

    }

    // custom progress dialog to rum while something is going on the in the background (video saving)
    private fun showProgressDialog (){
        customProgressDialog = Dialog(this)
        customProgressDialog?.setContentView(R.layout.custom_process_bar)
        customProgressDialog?.show()

    }
    private fun cancleProgressBarDialog (){
        if (customProgressDialog != null){
            customProgressDialog?.dismiss()
            customProgressDialog = null
        }
    }
}