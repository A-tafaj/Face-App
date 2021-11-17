package com.example.faceapp.viewmodel

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faceapp.utils.FaceApp
import com.example.faceapp.utils.ImageInteractor
import com.microsoft.projectoxford.face.FaceServiceClient.FaceAttributeType.Emotion
import com.microsoft.projectoxford.face.FaceServiceRestClient
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
private const val API_KEY = "a599f5e2faa24c0baed15f54e715d2e3"
private const val API_ENDPOINT = "https://fiek.cognitiveservices.azure.com/face/v1.0"

class CameraViewModel : ViewModel() {
    var passArrayOfEmotions = MutableLiveData<List<String>>()
    var imageIntent = MutableLiveData<Intent>()
    var passDrawnImage = MutableLiveData<Bitmap>()
    private var arrayOfEmotions = arrayListOf<String>()
    private var arrayOfFaces = arrayListOf<String>()
    lateinit var currentPhotoPath: String
    private val imageInteractor = ImageInteractor()

    private val faceServiceClient = FaceServiceRestClient(API_ENDPOINT, API_KEY)

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
            arrayOfEmotions.add("happiness: ${result[i].faceAttributes.emotion.happiness}")
            arrayOfEmotions.add("sadness: ${result[i].faceAttributes.emotion.sadness}")
            arrayOfEmotions.add("surprise: ${result[i].faceAttributes.emotion.surprise}")
            arrayOfEmotions.add("neutral: ${result[i].faceAttributes.emotion.neutral}")
            arrayOfEmotions.add("anger: ${result[i].faceAttributes.emotion.anger}")
            arrayOfEmotions.add("contempt: ${result[i].faceAttributes.emotion.contempt}")
            arrayOfEmotions.add("disgust: ${result[i].faceAttributes.emotion.disgust}")
            arrayOfEmotions.add("fear: ${result[i].faceAttributes.emotion.fear}")

            arrayOfFaces.add("face ${i + 1}: $arrayOfEmotions")
            arrayOfEmotions.clear()
        }
        passArrayOfEmotions.postValue(arrayOfFaces.toList())
        arrayOfFaces.clear()
    }

    fun detectAndFrame(filePath: String) {
        val targetStream: InputStream = FileInputStream(File(filePath))
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val result: Array<Face> = faceServiceClient.detect(
                        targetStream, true, true, arrayOf(
                            Emotion
                        )
                    )
                    fillEmotionsArray(result)
                    val image = imageInteractor.getBitmapFromPath(filePath)
                    val imageWithRectangle = imageInteractor.drawFaceRectanglesOnBitmap(image, result)
                    passDrawnImage.postValue(imageWithRectangle)
                }
            } catch (exception: Exception) {
                Log.e(TAG, "detectAndFrame: exception ->", exception)
            }
        }
    }
}