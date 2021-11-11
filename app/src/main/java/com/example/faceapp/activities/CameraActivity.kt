package com.example.faceapp.activities

import android.Manifest
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.faceapp.databinding.ActivityMainBinding
import com.example.faceapp.viewmodel.CameraViewModel
import androidx.core.app.ActivityCompat

import android.content.pm.PackageManager

import androidx.core.content.ContextCompat

import android.os.Build
import androidx.annotation.NonNull
import android.content.DialogInterface
import android.app.Activity
import android.net.Uri
import android.provider.Settings
import android.R
import android.R.attr

import android.R.attr.data
import android.view.View


class CameraActivity : AppCompatActivity() {
    private val TAG = "CameraActivity"
    private lateinit var binding: ActivityMainBinding
    val REQUEST_IMAGE_CAPTURE = 1
    private var permissionGranted = true
    private val REQUEST_OPEN_GALLERY = 2
    private val SELECT_PICTURE_CODE = 3
    private val cameraViewModel: CameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initClickListeners()
        observeViewModel()
        handlePermission()
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
            binding.captureImg.setImageBitmap(cameraViewModel.imageOrientation())
            cameraViewModel.galleryAddPic()
        }
        if (resultCode === RESULT_OK && requestCode === REQUEST_OPEN_GALLERY) {
                // Get the url from data
                val selectedImageUri: Uri? = data?.data
                if (null != selectedImageUri) {
                    binding.galleryImg.setImageURI(selectedImageUri)
            }
        }

    }

    /* Choose an image from Gallery */
    private fun openGalleryChooser() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), REQUEST_OPEN_GALLERY)
    }

    fun initClickListeners() {
        binding.captureBtn.setOnClickListener {
            cameraViewModel.dispatchTakePictureIntent()
        }
        binding.galleryBtn.setOnClickListener{
            if (permissionGranted){
                openGalleryChooser()
            }
        }
    }

    private fun handlePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //ask for permission
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                SELECT_PICTURE_CODE
            )
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        when (requestCode) {
            SELECT_PICTURE_CODE -> {
                var i = 0
                while (i < permissions.size) {
                    val permission = permissions[i]
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        permissionGranted = false
                        val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission!!)
                        if (showRationale) {
                            Log.d(TAG, "onRequestPermissionsResult: access to media is crucial to this app")
                        } else {
                            Log.d(TAG, "onRequestPermissionsResult: access to media is crucial to this app")
                            //showSettingsAlert()
                        }
                    }
                    i++
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}