package me.grishka.houseclub.api

import android.net.Uri
import android.provider.OpenableColumns
import me.grishka.houseclub.App
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.Okio
import java.io.IOException

class ContentUriRequestBody(private val uri: Uri) : RequestBody() {
    private var size: Long = 0
    var fileName: String? = null
    @Throws(IOException::class)
    override fun contentLength(): Long {
        return size
    }

    override fun contentType(): MediaType? {
        return MediaType.get(App.applicationContext!!.contentResolver.getType(uri))
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        Okio.source(App.applicationContext!!.contentResolver.openInputStream(uri))
            .use { source -> sink.writeAll(source) }
    }

    init {
        App.applicationContext!!.contentResolver.query(uri, null, null, null, null).use { cursor ->
            cursor!!.moveToFirst()
            size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE))
            fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        }
    }
}