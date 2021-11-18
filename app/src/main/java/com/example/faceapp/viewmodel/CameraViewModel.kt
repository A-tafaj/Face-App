package com.example.faceapp.viewmodel

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.faceapp.utils.FaceApp
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraViewModel : ViewModel() {
    private val TAG = "CameraViewModel"
    var imageIntent = MutableLiveData<Intent>()
    lateinit var currentPhotoPath: String

    private fun createUniqueImageFile(): File? {
        val timeStamp: String = SimpleDateFormat("ddMMyyyy_HHmmss", Locale.GERMANY).format(Date())
        return try {
            val storageDir: File? = FaceApp.getInstance().applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir
            ).apply {
                currentPhotoPath = absolutePath
            }
        } catch (ex: IOException) {
            Log.e(TAG, "createImageFile: ", ex)
            null
        }
    }

    fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(FaceApp.getInstance().applicationContext.packageManager)?.also {
                val photoFile: File? = createUniqueImageFile()
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        FaceApp.getInstance().applicationContext,
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    imageIntent.postValue(takePictureIntent)
                }
            }
        }
    }
}