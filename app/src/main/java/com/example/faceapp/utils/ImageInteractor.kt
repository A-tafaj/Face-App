package com.example.faceapp.utils

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.File

class ImageInteractor {
    fun getImageFromGallery(data: Intent?){
        val selectedImage = data?.data
        val imageFile = File(getPathFromUri(selectedImage))
    }

    fun getImage(): Bitmap {
        return BitmapFactory.decodeFile("currentPhotoPath")
    }

    fun getOrientation(path: String): Int {
        val exifInterface = ExifInterface(path);
        return exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
    }

    fun getOrientatedImage(): Bitmap? {
        val rotatedBitmap: Bitmap? = when (getOrientation("path")) {
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
}