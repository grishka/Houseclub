package me.grishka.houseclub.api

import android.net.Uri
import android.util.Log
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import me.grishka.appkit.utils.WorkerThread
import me.grishka.houseclub.App
import me.grishka.houseclub.BuildConfig
import me.grishka.houseclub.api.ClubhouseSession.deviceID
import me.grishka.houseclub.api.ClubhouseSession.isLoggedIn
import me.grishka.houseclub.api.ClubhouseSession.userID
import me.grishka.houseclub.api.ClubhouseSession.userToken
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

class ClubhouseAPIController private constructor() {
    private val apiThread: WorkerThread
    val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").disableHtmlEscaping()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
    private val httpClient = OkHttpClient.Builder().build()
    fun <T> execRequest(req: ClubhouseAPIRequest<T>) {
        apiThread.postRunnable(RequestRunnable(req), 0)
    }

    private inner class RequestRunnable<T>(private val req: ClubhouseAPIRequest<T>) : Runnable {
        override fun run() {
            try {
                if (req.canceled) return
                val uri = API_URL.buildUpon().appendPath(
                    req.path
                )
                if (req.queryParams != null) {
                    for ((key, value) in req.queryParams!!) {
                        uri.appendQueryParameter(key, value)
                    }
                }
                var reqBody: RequestBody? = null
                if (req.requestBody != null) {
                    reqBody = RequestBody.create(
                        MediaType.get("application/json; charset=utf-8"), gson.toJson(
                        req.requestBody
                    )
                    )
                } else if (req.fileToUpload != null && req.fileFieldName != null) {
                    reqBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(
                            req.fileFieldName, req.fileToUpload!!.name, RequestBody.create(
                            MediaType.get(
                                req.fileMimeType
                            ), req.fileToUpload
                        )
                        )
                        .build()
                } else if (req.contentUriToUpload != null && req.fileFieldName != null) {
                    val part = ContentUriRequestBody(req.contentUriToUpload!!)
                    reqBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(req.fileFieldName, part.fileName, part)
                        .build()
                }
                if (DEBUG) Log.i(TAG, "Sending: " + req.method + " " + uri)
                val bldr = Request.Builder()
                    .url(uri.build().toString())
                if ("POST" == req.method && reqBody == null) {
                    val fld = bldr.javaClass.getDeclaredField("method")
                    fld.isAccessible = true
                    fld[bldr] = req.method
                } else {
                    bldr.method(req.method, reqBody)
                }
                val locales = App.applicationContext!!.resources.configuration.locales
                bldr.header("CH-Languages", locales.toLanguageTags())
                    .header("CH-Locale", locales[0].toLanguageTag().replace('-', '_'))
                    .header("Accept", "application/json")
                    .header("CH-AppBuild", API_BUILD_ID)
                    .header("CH-AppVersion", API_BUILD_VERSION)
                    .header("User-Agent", API_UA)
                    .header("CH-DeviceId", deviceID)
                if (isLoggedIn) {
                    bldr.header("Authorization", "Token " + userToken)
                        .header("CH-UserID", userID)
                }
                val call = httpClient.newCall(bldr.build())
                if (DEBUG) Log.i(TAG, call.request().headers().toString())
                req.currentRequest = call
                call.execute().use { resp ->
                    val body = resp.body()
                    if (DEBUG) Log.i(TAG, "Code: " + resp.code())
                    if (resp.code() == 200) {
                        val respStr = body!!.string()
                        if (DEBUG) Log.i(TAG, "Raw response: $respStr")
                        //						T robj=gson.fromJson(body.charStream(), req.responseClass);
                        val robj: T = gson.fromJson(respStr, req.responseClass)
                        if (DEBUG) Log.i(TAG, "Parsed response: $robj")
                        req.onSuccess(robj)
                    } else {
                        val respStr = body!!.string()
                        if (DEBUG) Log.i(TAG, "Raw response: $respStr")
                        val br = gson.fromJson(respStr, BaseResponse::class.java)
                        req.onError(ClubhouseErrorResponse(br))
                    }
                }
            } catch (x: Exception) {
                Log.w(TAG, x)
                val msg = if (BuildConfig.DEBUG) x.localizedMessage else null
                req.onError(ClubhouseErrorResponse(-1, msg!!))
            }
        }
    }

    companion object {
        var instance: ClubhouseAPIController? = null
            get() {
                if (field == null) {
                    field = ClubhouseAPIController()
                }
                return field
            }
            private set
        private const val TAG = "ClubhouseAPI"
        private val DEBUG = BuildConfig.DEBUG
        private val API_URL = Uri.parse("https://www.clubhouseapi.com/api")

        //	private static final Uri API_URL=Uri.parse("http://192.168.0.51:8080/");
        private const val API_BUILD_ID = "304"
        private const val API_BUILD_VERSION = "0.1.28"
        private const val API_UA = "clubhouse/" + API_BUILD_ID + " (iPhone; iOS 13.5.1; Scale/3.00)"
        const val PUBNUB_PUB_KEY = "pub-c-6878d382-5ae6-4494-9099-f930f938868b"
        const val PUBNUB_SUB_KEY = "sub-c-a4abea84-9ca3-11ea-8e71-f2b83ac9263d"
        const val TWITTER_ID = "NyJhARWVYU1X3qJZtC2154xSI"
        const val TWITTER_SECRET = "ylFImLBFaOE362uwr4jut8S8gXGWh93S1TUKbkfh7jDIPse02o"
        const val AGORA_KEY = "938de3e8055e42b281bb8c6f69c21f78"
        const val SENTRY_KEY = "5374a416cd2d4009a781b49d1bd9ef44@o325556.ingest.sentry.io/5245095"
        const val INSTABUG_KEY = "4e53155da9b00728caa5249f2e35d6b3"
        const val AMPLITUDE_KEY = "9098a21a950e7cb0933fb5b30affe5be"
    }

    init {
        apiThread = WorkerThread("ApiThread")
        apiThread.start()
    }
}