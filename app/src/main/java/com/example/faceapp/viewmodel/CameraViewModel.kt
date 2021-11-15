package com.example.faceapp.viewmodel

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faceapp.utils.FaceApp
import com.microsoft.projectoxford.face.FaceServiceClient.FaceAttributeType
import com.microsoft.projectoxford.face.FaceServiceClient.FaceAttributeType.Emotion
import com.microsoft.projectoxford.face.FaceServiceClient.FaceAttributeType.Gender
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

class CameraViewModel : ViewModel() {
    var jsonObject: JSONObject = JSONObject()
    var jsonObject2: JSONObject = JSONObject()
    var imageIntent = MutableLiveData<Intent>()
    var passJson = MutableLiveData<String>()
    lateinit var currentPhotoPath: String

    private val faceServiceClient = FaceServiceRestClient("https://fiek.cognitiveservices.azure.com/face/v1.0", "a599f5e2faa24c0baed15f54e715d2e3")

    fun getImage(): Bitmap {
        return BitmapFactory.decodeFile(currentPhotoPath)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("ddMMyyyy_HHmmss", Locale.GERMANY).format(Date())
        val storageDir: File? = FaceApp.getInstance().applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(FaceApp.getInstance().applicationContext.packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Log.e(TAG, "dispatchTakePictureIntent: $ex")
                    null
                }
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
            Emotion,
            Gender
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
                    Log.d(TAG, "detectAndFrame: json- > ${passJson.postValue(jsonObject2.toString())}")
                }
            } catch (exception: Exception) {
                Log.e(TAG, "detectAndFrame: exception ->", exception)
            }
        }
    }

    fun getOrientation(): Int {
        val exifInterface = ExifInterface(currentPhotoPath);
        return exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
    }

    fun getOrientatedImage(): Bitmap? {
        val rotatedBitmap: Bitmap? = when (getOrientation()) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(getImage(), 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(getImage(), 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(getImage(), 270f)
            else -> getImage()
        }
        return rotatedBitmap
    }

    fun rotateImage(source: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    fun getPath(context: Context, uri: Uri?): String {
        var result: String? = null
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = uri?.let { context.getContentResolver().query(it, proj, null, null, null) }
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val column_index: Int = cursor.getColumnIndexOrThrow(proj[0])
                result = cursor.getString(column_index)
            }
            cursor.close()
        }
        return result ?: "Not found"
    }
}