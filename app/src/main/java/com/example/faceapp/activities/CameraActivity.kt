package com.example.faceapp.activities

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.faceapp.databinding.ActivityMainBinding
import com.example.faceapp.utils.ImageInteractor
import com.example.faceapp.viewmodel.CameraViewModel

class CameraActivity : AppCompatActivity() {
    private val TAG = "CameraActivity"
    private lateinit var binding: ActivityMainBinding
    val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_OPEN_GALLERY = 2
    private val SELECT_PICTURE_CODE = 3
    private val imageInteractor = ImageInteractor()
    private val cameraViewModel: CameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initClickListeners()
        observeViewModel()

    }

    /**observe when user takes photo and observe when we get the emotions*/
    private fun observeViewModel() {
        try {
            cameraViewModel.imageIntent.observe(this, { intent ->
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            })
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "dispatchTakePictureIntent: $e")
        }
        cameraViewModel.passJson.observe(this, { json ->
            binding.apiData.text = json
            Log.d(TAG, "observeViewModel: detectAndFrame =  $json")
        })
        cameraViewModel.passDrawnImage.observe(this, { drawnImage ->
            binding.captureImg.setImageBitmap(drawnImage)// this
            Log.d(TAG, "observeViewModel: -> drawnImage $drawnImage")
        })
    }

    /**check the reqest granted and set img from camera or gallery (detect emotions on both), draw rectangle on faces*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            binding.captureImg.setImageBitmap(imageInteractor.getOrientatedImage(cameraViewModel.currentPhotoPath))
            cameraViewModel.detectAndFrame(cameraViewModel.currentPhotoPath)
        }
        if (resultCode == RESULT_OK && requestCode == REQUEST_OPEN_GALLERY) {
            // Get the url from data
            data?.data?.run {
                val path = imageInteractor.getPath(this)
                binding.captureImg.setImageBitmap(imageInteractor.getOrientatedImage(path))
                cameraViewModel.detectAndFrame(path)
            }
        }
    }

    /**open gallery*/
    private fun chooseImageFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), REQUEST_OPEN_GALLERY)
    }

    fun initClickListeners() {
        binding.captureBtn.setOnClickListener {
            cameraViewModel.dispatchTakePictureIntent()
        }
        binding.galleryBtn.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                SELECT_PICTURE_CODE
            )
        } else {
            chooseImageFromGallery()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        when (requestCode) {
            SELECT_PICTURE_CODE -> {
                val permission = permissions[0]
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
                    val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission!!)
                    if (showRationale) {
                        Log.d(TAG, "onRequestPermissionsResult: access to media is crucial to this app")
                    }
                } else {
                    chooseImageFromGallery()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}