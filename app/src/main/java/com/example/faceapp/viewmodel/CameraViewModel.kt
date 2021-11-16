package com.example.faceapp.viewmodel

import android.content.ContentValues
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
import com.microsoft.projectoxford.face.FaceServiceClient.FaceAttributeType
import com.microsoft.projectoxford.face.FaceServiceClient.FaceAttributeType.Emotion
import com.microsoft.projectoxford.face.FaceServiceRestClient
import com.microsoft.projectoxford.face.contract.Face
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
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
    var jsonObject: JSONObject = JSONObject()
    var jsonObject2: JSONObject = JSONObject()
    var imageIntent = MutableLiveData<Intent>()
    var passJson = MutableLiveData<String>()
    var passDrawnImage = MutableLiveData<Bitmap>()
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

    fun faceAttributes(): Array<FaceAttributeType?> {
        return arrayOf(
            Emotion
        )
    }

    fun fillJSON(result: Array<Face>) {
        for (i in result.indices) {
            jsonObject.put("happiness", result[i].faceAttributes.emotion.happiness)
            jsonObject.put("sadness", result[i].faceAttributes.emotion.sadness)
            jsonObject.put("surprise", result[i].faceAttributes.emotion.surprise)
            jsonObject.put("neutral", result[i].faceAttributes.emotion.neutral)
            jsonObject.put("anger", result[i].faceAttributes.emotion.anger)
            jsonObject.put("contempt", result[i].faceAttributes.emotion.contempt)
            jsonObject.put("disgust", result[i].faceAttributes.emotion.disgust)
            jsonObject.put("fear", result[i].faceAttributes.emotion.fear)
            Log.e(ContentValues.TAG, "doInBackground: $jsonObject")
            jsonObject2.put(i.toString(), jsonObject)
        }
    }

    fun detectAndFrame(filePath: String) {
        val targetStream: InputStream = FileInputStream(File(filePath))
        viewModelScope.launch {
            try {
                Log.d(TAG, "detectAndFrame: init")
                withContext(Dispatchers.IO) {
                    val result: Array<Face> = faceServiceClient.detect(
                        targetStream, true, true, faceAttributes()
                    )
                    fillJSON(result)
                    passJson.postValue(jsonObject2.toString())
                    val image = imageInteractor.getBitmapFromPath(filePath)
                    val imageWithRectangle = imageInteractor.drawFaceRectanglesOnBitmap(image, result)
                    passDrawnImage.postValue(imageWithRectangle)
                    Log.d(TAG, "detectAndFrame: json- > ${passJson.postValue(jsonObject2.toString())}")

                }
            } catch (exception: Exception) {
                Log.e(TAG, "detectAndFrame: exception ->", exception)
            }
        }
    }
}