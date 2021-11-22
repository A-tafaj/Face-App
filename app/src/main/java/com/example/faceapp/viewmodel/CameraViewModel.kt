package com.example.faceapp.viewmodel

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.Camera
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faceapp.SingleLiveEvent
import com.example.faceapp.services.FaceServiceRestClient.getFaceServiceRestClient
import com.example.faceapp.utils.FaceApp
import com.example.faceapp.utils.GenericMethods.decimalFormat
import com.example.faceapp.utils.ImageInteractor
import com.microsoft.projectoxford.face.FaceServiceClient.FaceAttributeType.Emotion
import com.microsoft.projectoxford.face.contract.Face
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "CameraViewModel"

class CameraViewModel : ViewModel() {
    var progressVisibility = MutableLiveData<Boolean>()
    var passArrayOfEmotions = MutableLiveData<List<String>>()
    var imageIntent = SingleLiveEvent<Intent>()
    var passDrawnImage = MutableLiveData<Bitmap>()
    private var arrayOfEmotions = arrayListOf<String>()
    private var arrayOfFaces = arrayListOf<String>()
    lateinit var currentPhotoPath: String
    private val faceServiceRestClient = getFaceServiceRestClient()
    private val imageInteractor = ImageInteractor()

    /**
     * Creates a file where we store the taken image - this is used in order to avoid image quality loss
     */
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

    fun fillEmotionsArray(result: Array<Face>) {
        for (i in result.indices) {
            val emotion = result[i].faceAttributes.emotion
            arrayOfEmotions.add("happiness: ${decimalFormat(emotion.happiness)}")
            arrayOfEmotions.add("sadness: ${decimalFormat(emotion.sadness)}")
            arrayOfEmotions.add("surprise: ${decimalFormat(emotion.surprise)}")
            arrayOfEmotions.add("neutral: ${decimalFormat(emotion.neutral)}")
            arrayOfEmotions.add("anger: ${decimalFormat(emotion.anger)}")
            arrayOfEmotions.add("contempt: ${decimalFormat(emotion.contempt)}")
            arrayOfEmotions.add("disgust: ${decimalFormat(emotion.disgust)}")
            arrayOfEmotions.add("fear: ${decimalFormat(emotion.fear)}")

            arrayOfFaces.add("face ${i + 1}: $arrayOfEmotions")
            arrayOfEmotions.clear()
        }
        passArrayOfEmotions.postValue(arrayOfFaces.toList())
        arrayOfFaces.clear()
    }

    fun detectAndFrame(filePath: String) {
        val targetStream: InputStream = FileInputStream(File(filePath))
        progressVisibility.postValue(true)
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val result: Array<Face> = faceServiceRestClient.detect(
                        targetStream, true, true, arrayOf(
                            Emotion
                        )
                    )
                    fillEmotionsArray(result)
                    val image = imageInteractor.getBitmapFromPath(filePath)
                    val imageWithRectangle = imageInteractor.drawFaceRectanglesOnBitmap(image, result)
                    passDrawnImage.postValue(imageWithRectangle)
                    progressVisibility.postValue(false)
                }
            } catch (exception: Exception) {
                Log.e(TAG, "detectAndFrame: exception ->", exception)
            }
        }
    }

    /***/
    /** Check if this device has a camera */
    private fun checkCameraHardware(context: Context): Boolean {
        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            // this device has a camera
            return true
        }
        // no camera on this device
        return false
    }

    /** A safe way to get an instance of the Camera object. */
    fun getCameraInstance(): Boolean {
        return try {
            Camera.open()
            true
            // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            Log.d(TAG, "getCameraInstance: Camera is not available (in use or does not exist)")
            false // returns false if camera is unavailable
        }
    }

    fun checkIfCameraIsAvailable(context: Context): Boolean {
        if (checkCameraHardware(context)) {
            if (getCameraInstance()) {
                return true
            }
            return false
        }
        return false
    }
}