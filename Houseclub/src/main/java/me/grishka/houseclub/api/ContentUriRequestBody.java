package me.grishka.houseclub.api;

import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.IOException;

import me.grishka.houseclub.App;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class ContentUriRequestBody extends RequestBody {

    private Uri uri;
    private long size;
    private String name;

    public ContentUriRequestBody(Uri uri) {
        this.uri = uri;
        try (Cursor cursor = App.applicationContext.getContentResolver().query(uri, null, null, null, null)) {
            cursor.moveToFirst();
            size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
            name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        }
    }

    @Override
    public long contentLength() throws IOException {
        return size;
    }

    @Override
    public MediaType contentType() {
        return MediaType.get(App.applicationContext.getContentResolver().getType(uri));
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        try (Source source = Okio.source(App.applicationContext.getContentResolver().openInputStream(uri))) {
            sink.writeAll(source);
        }
    }

    public String getFileName() {
        return name;
    }
}
