package me.grishka.houseclub.api;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;

import me.grishka.appkit.api.APIRequest;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.houseclub.R;
import okhttp3.Call;

public abstract class ClubhouseAPIRequest<T> extends APIRequest<T> {

    public String path;
    public String method;
    public HashMap<String, String> queryParams;
    public Object requestBody;
    public Type responseClass;
    public File fileToUpload;
    public Uri contentUriToUpload;
    public String fileFieldName, fileMimeType;
    private ProgressDialog progress;

    boolean canceled;
    Call currentRequest;

    public ClubhouseAPIRequest(String method, String path, Type responseClass) {
        this.path = path;
        this.method = method;
        this.responseClass = responseClass;
    }

    @Override
    public void cancel() {
        canceled = true;
        if (currentRequest != null)
            currentRequest.cancel();
    }

    @Override
    public APIRequest<T> exec() {
        ClubhouseAPIController.getInstance().execRequest(this);
        if (progress != null)
            progress.show();
        return this;
    }

    public ClubhouseAPIRequest<T> upload(String fieldName, String mimeType, File file) {
        fileFieldName = fieldName;
        fileToUpload = file;
        fileMimeType = mimeType;
        return this;
    }

    public ClubhouseAPIRequest<T> upload(String fieldName, Uri uri) {
        fileFieldName = fieldName;
        contentUriToUpload = uri;
        return this;
    }

    public ClubhouseAPIRequest<T> wrapProgress(Context context) {
        progress = new ProgressDialog(context);
        progress.setMessage(context.getString(R.string.loading));
        progress.setCancelable(false);
        return this;
    }

    private void dismissProgressDialog() {
        progress.dismiss();
        progress = null;
    }

    public void prepare() throws Exception {

    }

    public T parse(String resp) throws Exception {
        return ClubhouseAPIController.getInstance().getGson().fromJson(resp, responseClass);
    }

    void onSuccess(T result) {
        if (progress != null)
            uiThreadHandler.post(this::dismissProgressDialog);
        invokeSuccessCallback(result);
    }

    void onError(ErrorResponse result) {
        if (progress != null)
            uiThreadHandler.post(this::dismissProgressDialog);
        invokeErrorCallback(result);
    }
}
