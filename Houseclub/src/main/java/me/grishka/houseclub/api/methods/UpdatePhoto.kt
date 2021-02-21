package me.grishka.houseclub.api.methods

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import me.grishka.houseclub.App
import me.grishka.houseclub.api.ClubhouseAPIRequest
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class UpdatePhoto(private val uri: Uri) :
    ClubhouseAPIRequest<Bitmap?>("POST", "update_photo", Bitmap::class.java) {
    private lateinit var resizedBitmap: Bitmap

    @Throws(IOException::class)
    override fun prepare() {
        var orig: Bitmap
        App.applicationContext.contentResolver.openInputStream(uri).use {
            orig = BitmapFactory.decodeStream(it)
        }
        val size = Math.min(512, Math.min(orig.width, orig.height))
        resizedBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val srcRect: Rect
        srcRect = if (orig.width > orig.height) {
            val x = (orig.width - orig.height) / 2
            Rect(x, 0, x + orig.height, orig.height)
        } else {
            val y = (orig.height - orig.width) / 2
            Rect(0, y, orig.width, y + orig.width)
        }
        Canvas(resizedBitmap).drawBitmap(orig, srcRect, Rect(0, 0, size, size), Paint(Paint.FILTER_BITMAP_FLAG))
        val tmp = File(App.applicationContext.cacheDir, "ava_tmp.jpg")
        FileOutputStream(tmp).use { out -> resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out) }
        fileToUpload = tmp
        fileFieldName = "file"
        fileMimeType = "image/jpeg"
    }

    @Throws(Exception::class)
    override fun parse(resp: String?): Bitmap {
        return resizedBitmap
    }
}