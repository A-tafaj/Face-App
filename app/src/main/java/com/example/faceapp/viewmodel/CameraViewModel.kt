package com.example.faceapp.viewmodel

import android.content.Intent
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
import com.example.faceapp.FaceApp
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "CameraViewModel"

class CameraViewModel : ViewModel() {
    var imageIntent = MutableLiveData<Intent>()
    lateinit var currentPhotoPath: String

    fun getImage(): Bitmap {
        return BitmapFactory.decodeFile(currentPhotoPath)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
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
                    Log.e(TAG, "dispatchTakePictureIntent: $ex", )
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
    fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            FaceApp.getInstance().applicationContext.sendBroadcast(mediaScanIntent)
        }
    }

    /**
     * Most phone cameras are landscape, meaning if you take the photo in portrait,
     * the resulting photos will be rotated 90 degrees. In this case,
     * the camera software should populate the Exif data with the orientation that the photo should be viewed in.
     *  Note that the below solution depends on the camera software/device manufacturer populating the Exif data,
     *  so it will work in most cases, but it is not a 100% reliable solution.
     *  */
    fun getOrientation(): Int {
        var exifInterface =  ExifInterface(currentPhotoPath);
        return exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED);
    }

    fun imageOrientation(): Bitmap? {
        var  rotatedBitmap: Bitmap? = when(getOrientation()) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(getImage(), 90f)
            ExifInterface.ORIENTATION_ROTATE_180 ->rotateImage(getImage(), 180f)
            ExifInterface.ORIENTATION_ROTATE_270 ->rotateImage(getImage(), 270f)
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
}