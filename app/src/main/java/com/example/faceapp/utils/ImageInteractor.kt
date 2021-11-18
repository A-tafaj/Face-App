package com.example.faceapp.utils

import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore

class ImageInteractor {

    /**
     * Most phone cameras are landscape, meaning if you take the photo in portrait,
     * the resulting photos will be rotated 90 degrees. In this case,
     * the camera software should populate the Exif data with the orientation that the photo should be viewed in.
     *  Note that the below solution depends on the camera software/device manufacturer populating the Exif data,
     *  so it will work in most cases, but it is not a 100% reliable solution.
     */
    fun rotateImage(source: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    fun getOrientatedImage(path: String): Bitmap? {
        val bitmap = getBitmapFromPath(path)
        val rotatedBitmap: Bitmap? = when (getOrientation(path)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
            else -> getBitmapFromPath(path)
        }
        return rotatedBitmap
    }

    fun getOrientation(path: String): Int {
        val exifInterface = ExifInterface(path);
        return exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
    }

    private fun getBitmapFromPath(path: String): Bitmap {
        return BitmapFactory.decodeFile(path)
    }

    fun getPath(uri: Uri?): String {
        var result: String? = null
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = uri?.let { FaceApp.getInstance().applicationContext.contentResolver.query(it, proj, null, null, null) }
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