package com.example.faceapp.utils

import android.database.Cursor
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import com.microsoft.projectoxford.face.contract.Face

class ImageInteractor {

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

    fun getBitmapFromPath(path: String): Bitmap {
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

    fun drawFaceRectanglesOnBitmap(originalBitmap: Bitmap, faces: Array<Face>?): Bitmap? {
        val bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        val colors = arrayOf(Color.BLUE, Color.BLACK, Color.GREEN, Color.WHITE, Color.RED, Color.YELLOW, Color.CYAN)
        paint.strokeWidth = 5f
        if (faces != null) {
            for (indice in faces.indices) {
                paint.color = colors[(indice) % 6]
                val faceRectangle = faces[indice].faceRectangle
                canvas.drawRect(
                    faceRectangle.left.toFloat(),
                    faceRectangle.top.toFloat(), (
                            faceRectangle.left + faceRectangle.width).toFloat(), (
                            faceRectangle.top + faceRectangle.height).toFloat(),
                    paint
                )

            }
        }
        return bitmap
    }
}