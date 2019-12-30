package com.azheng.scaleimage

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix

import android.os.Environment
import android.text.TextUtils
import androidx.exifinterface.media.ExifInterface

import java.io.*


/**
 *
 *   //通知系统相册刷新
this.sendBroadcast(
Intent(
Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
Uri.fromFile(File(path))
)
)
 */

object ImageUtil {

    fun getImageBitmap(srcPath: String, maxWidth: Float, maxHeight: Float): Bitmap? {
        val newOpts = BitmapFactory.Options()
        newOpts.inJustDecodeBounds = true
        var bitmap: Bitmap? = BitmapFactory.decodeFile(srcPath, newOpts)

        newOpts.inJustDecodeBounds = false
        val originalWidth = newOpts.outWidth
        val originalHeight = newOpts.outHeight

        var be = 1f
        if (originalWidth > originalHeight && originalWidth > maxWidth) {
            be = originalWidth / maxWidth
        } else if (originalWidth < originalHeight && originalHeight > maxHeight) {
            be = newOpts.outHeight / maxHeight
        }
        if (be <= 0) {
            be = 1f
        }

        newOpts.inSampleSize = be.toInt()
        newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888
        newOpts.inDither = false
        newOpts.inPurgeable = true
        newOpts.inInputShareable = true

        if (bitmap != null && !bitmap.isRecycled) {
            bitmap.recycle()
        }

        try {
            bitmap = BitmapFactory.decodeFile(srcPath, newOpts)
        } catch (e: OutOfMemoryError) {
            if (bitmap != null && !bitmap.isRecycled) {
                bitmap.recycle()
            }
            Runtime.getRuntime().gc()
        } catch (e: Exception) {
            Runtime.getRuntime().gc()
        }

        if (bitmap != null) {
            bitmap = rotateBitmapByDegree(bitmap, getBitmapDegree(srcPath))
        }
        return bitmap
    }

    fun getBitmapDegree(path: String): Int {
        var degree = 0
        try {
            val exifInterface = ExifInterface(path)
            val orientation =
                exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
                else -> degree = 0
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return degree
    }

    fun rotateBitmapByDegree(bm: Bitmap, degree: Int): Bitmap {
        var returnBm: Bitmap? = null
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        try {
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height, matrix, true)
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        }

        if (returnBm == null) {
            returnBm = bm
        }
        if (bm != returnBm) {
            bm.recycle()
        }
        return returnBm
    }

    fun getWidthHeight(imagePath: String): IntArray {
        if (TextUtils.isEmpty(imagePath)) {
            return intArrayOf(0, 0)
        }
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        try {
            val originBitmap = BitmapFactory.decodeFile(imagePath, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 使用第一种方式获取原始图片的宽高
        var srcWidth = options.outWidth
        var srcHeight = options.outHeight

        // 使用第二种方式获取原始图片的宽高
        if (srcHeight <= 0 || srcWidth <= 0) {
            try {
                val exifInterface = ExifInterface(imagePath)
                srcHeight =
                    exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, ExifInterface.ORIENTATION_NORMAL)
                srcWidth =
                    exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, ExifInterface.ORIENTATION_NORMAL)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        // 使用第三种方式获取原始图片的宽高
        if (srcWidth <= 0 || srcHeight <= 0) {
            val bitmap2 = BitmapFactory.decodeFile(imagePath)
            if (bitmap2 != null) {
                srcWidth = bitmap2.width
                srcHeight = bitmap2.height
                try {
                    if (!bitmap2.isRecycled) {
                        bitmap2.recycle()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
        return intArrayOf(srcWidth, srcHeight)
    }

    fun getImageRatio(imagePath: String): Float {
        val wh = getWidthHeight(imagePath)
        return if (wh[0] > 0 && wh[1] > 0) {
            Math.max(wh[0], wh[1]).toFloat() / Math.min(wh[0], wh[1]).toFloat()
        } else 1f
    }

    fun resizeImage(origin: Bitmap?, newWidth: Int, newHeight: Int): Bitmap? {
        if (origin == null) {
            return null
        }
        val height = origin.height
        val width = origin.width
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        val newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false)
        if (!origin.isRecycled) {
            origin.recycle()
        }
        return newBM
    }

    @Throws(IOException::class)
    fun saveBitmapBackPath(bm: Bitmap): String {
        val path = Environment.getExternalStorageDirectory().path + "/share/image/"
        val targetDir = File(path)
        if (!targetDir.exists()) {
            try {
                targetDir.mkdirs()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        val fileName = (System.currentTimeMillis() +
                (Math.random() * 1000).toInt()).toString() + ".jpeg"

        val savedFile = File(path, fileName)
        val bos = FileOutputStream(savedFile)
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        bos.flush()
        bos.close()
        return savedFile.absolutePath
    }

    /**
     *将字节流转化为bitmap
     */
    fun transLateStreamToBitMap(input: InputStream): Bitmap {
        var outPutStream = ByteArrayOutputStream()
        try {
            var read: Int = -1
            input.use { input ->
                outPutStream.use {
                    while (input.read().also { read = it } != -1) {
                        it.write(read)
                    }
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        return BitmapFactory.decodeByteArray(outPutStream.toByteArray(), 0, outPutStream.toByteArray().size)
    }

    /**
     * 保存bitmap到本地
     *
     * @param bitmap
     * @return
     */
    fun saveBitmap(bitmap: Bitmap): String {
        var savePath = ""
        val filePic: File
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            savePath = Environment.getExternalStorageDirectory().path + "/share/image/"
        } else {
            return savePath
        }
        try {
            val fileName = (System.currentTimeMillis() +
                    (Math.random() * 1000).toInt()).toString() + ".jpeg"
            savePath = "${savePath}+${fileName}"
            filePic = File(savePath)
            if (!filePic.exists()) {
                filePic.parentFile.mkdirs()
                filePic.createNewFile()
            }
            val fos = FileOutputStream(filePic)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return savePath
        }
        return savePath
    }

    /**
     * 计算出图片初次显示需要放大倍数
     * @param imagePath 图片的绝对路径
     */
    fun getImageScale(context: Context, imagePath: String): Float {
        if (TextUtils.isEmpty(imagePath)) {
            return 2.0f
        }

        var bitmap: Bitmap? = null

        try {
            bitmap = BitmapFactory.decodeFile(imagePath)
        } catch (error: OutOfMemoryError) {
            error.printStackTrace()
        }

        if (bitmap == null) {
            return 2.0f
        }

        // 拿到图片的宽和高
        val dw = bitmap.width
        val dh = bitmap.height

        val wm = (context as Activity).windowManager
        val width = wm.defaultDisplay.width
        val height = wm.defaultDisplay.height

        var scale = 1.0f
        //图片宽度大于屏幕，但高度小于屏幕，则缩小图片至填满屏幕宽
        if (dw > width && dh <= height) {
            scale = width * 1.0f / dw
        }
        //图片宽度小于屏幕，但高度大于屏幕，则放大图片至填满屏幕宽
        if (dw <= width && dh > height) {
            scale = width * 1.0f / dw
        }
        //图片高度和宽度都小于屏幕，则放大图片至填满屏幕宽
        if (dw < width && dh < height) {
            scale = width * 1.0f / dw
        }
        //图片高度和宽度都大于屏幕，则缩小图片至填满屏幕宽
        if (dw > width && dh > height) {
            scale = width * 1.0f / dw
        }
        bitmap.recycle()
        return scale
    }

    fun isGif(url: String): Boolean {
        var mimeType = url.substring(url.lastIndexOf("."))
        mimeType = mimeType.replace(".", "")
        if (mimeType == "gif") {
            return true
        } else {
            return false
        }

    }
}