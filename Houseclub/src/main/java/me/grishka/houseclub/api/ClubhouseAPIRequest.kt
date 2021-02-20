package me.grishka.houseclub.api

import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import me.grishka.appkit.api.APIRequest
import me.grishka.appkit.api.ErrorResponse
import me.grishka.houseclub.R
import okhttp3.Call
import java.io.File
import java.lang.reflect.Type
import java.util.HashMap

abstract class ClubhouseAPIRequest<T>(var method: String, var path: String, var responseClass: Type) : APIRequest<T>() {
    var queryParams: HashMap<String, String>? = null
    var requestBody: Any? = null
    var fileToUpload: File? = null
    var contentUriToUpload: Uri? = null
    var fileFieldName: String? = null
    var fileMimeType: String? = null
    private var progress: ProgressDialog? = null
    var canceled = false
    var currentRequest: Call? = null
    override fun cancel() {
        canceled = true
        if (currentRequest != null) currentRequest!!.cancel()
    }

    override fun exec(): APIRequest<T> {
        ClubhouseAPIController.instance!!.execRequest(this)
        if (progress != null) progress!!.show()
        return this
    }

    fun upload(fieldName: String?, mimeType: String?, file: File?): ClubhouseAPIRequest<T> {
        fileFieldName = fieldName
        fileToUpload = file
        fileMimeType = mimeType
        return this
    }

    fun upload(fieldName: String?, uri: Uri?): ClubhouseAPIRequest<T> {
        fileFieldName = fieldName
        contentUriToUpload = uri
        return this
    }

    fun wrapProgress(context: Context): ClubhouseAPIRequest<T> {
        progress = ProgressDialog(context)
        progress!!.setMessage(context.getString(R.string.loading))
        progress!!.setCancelable(false)
        return this
    }

    private fun dismissProgressDialog() {
        progress!!.dismiss()
        progress = null
    }

    fun onSuccess(result: T) {
        if (progress != null) uiThreadHandler.post { dismissProgressDialog() }
        invokeSuccessCallback(result)
    }

    fun onError(result: ErrorResponse?) {
        if (progress != null) uiThreadHandler.post { dismissProgressDialog() }
        invokeErrorCallback(result)
    }
}