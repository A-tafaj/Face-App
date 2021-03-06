package com.example.faceapp.utils

import android.database.Cursor
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import com.microsoft.projectoxford.face.contract.Face

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

    private fun getOrientation(path: String): Int {
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
                val columnIndex: Int = cursor.getColumnIndexOrThrow(proj[0])
                result = cursor.getString(columnIndex)
            }
            cursor.close()
        }
        return result ?: "Not found"
    }

    fun drawFaceRectanglesOnBitmap(originalBitmap: Bitmap, faces: Array<Face>?): Bitmap? {
        val bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        val colors = arrayOf(Color.BLUE, Color.BLACK, Color.GREEN, Color.WHITE, Color.RED, Color.YELLOW, Color.CYAN)

        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f

        if (faces != null) {
            for (indices in faces.indices) {
                paint.color = colors[(indices) % 6]
                val faceRectangle = faces[indices].faceRectangle
                val cX = faceRectangle.left + faceRectangle.width / 2
                val cY = faceRectangle.top + faceRectangle.height

                canvas.drawRect(
                    faceRectangle.left.toFloat(),
                    faceRectangle.top.toFloat(), (
                            faceRectangle.left + faceRectangle.width).toFloat(), (
                            faceRectangle.top + faceRectangle.height).toFloat(),
                    paint
                )
                drawFaceId(canvas, 65, cX, cY + 70, Color.GREEN, indices + 1)
            }
        }
        return bitmap
    }

    private fun drawFaceId(canvas: Canvas, textsize: Int, cX: Int, cY: Int, color: Int, id: Int) {
        val paint = Paint()

        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 12f
        paint.color = color
        paint.textSize = textsize.toFloat()

        canvas.drawText(id.toString(), cX.toFloat(), cY.toFloat(), paint)
    }
}