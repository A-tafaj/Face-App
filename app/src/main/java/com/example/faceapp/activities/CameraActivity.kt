package com.example.faceapp.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.faceapp.databinding.ActivityMainBinding
import com.example.faceapp.viewmodel.CameraViewModel

class CameraActivity : AppCompatActivity() {
    private val TAG = "CameraActivity"
    private lateinit var binding: ActivityMainBinding
    val REQUEST_IMAGE_CAPTURE = 1
    private val cameraViewModel: CameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initClickListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        try {
            cameraViewModel.imageIntent.observe(this, { intent ->
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            })
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "dispatchTakePictureIntent: $e")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            binding.captureImg.setImageBitmap(cameraViewModel.imageOrientation())//getImage())
            cameraViewModel.galleryAddPic()
        }

    }

    fun initClickListeners() {
        binding.captureBtn.setOnClickListener {
            cameraViewModel.dispatchTakePictureIntent()
        }
    }
}