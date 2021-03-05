package me.grishka.houseclub.api;

import android.net.Uri;
import android.os.LocaleList;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Field;
import java.util.Map;

import me.grishka.appkit.utils.WorkerThread;
import me.grishka.houseclub.App;
import me.grishka.houseclub.BuildConfig;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ClubhouseAPIController {
    private static ClubhouseAPIController instance;
    private static final String TAG = "ClubhouseAPI";
    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final Uri API_URL = Uri.parse("https://www.clubhouseapi.com/api");
    //	private static final Uri API_URL=Uri.parse("http://192.168.0.51:8080/");
    private static final String API_BUILD_ID = "304";
    private static final String API_BUILD_VERSION = "0.1.28";
    private static final String API_UA = "clubhouse/" + API_BUILD_ID + " (iPhone; iOS 13.5.1; Scale/3.00)";

    public static final String PUBNUB_PUB_KEY = "pub-c-6878d382-5ae6-4494-9099-f930f938868b";
    public static final String PUBNUB_SUB_KEY = "sub-c-a4abea84-9ca3-11ea-8e71-f2b83ac9263d";

    public static final String TWITTER_ID = "NyJhARWVYU1X3qJZtC2154xSI";
    public static final String TWITTER_SECRET = "ylFImLBFaOE362uwr4jut8S8gXGWh93S1TUKbkfh7jDIPse02o";

    public static final String AGORA_KEY = "938de3e8055e42b281bb8c6f69c21f78";
    public static final String SENTRY_KEY = "5374a416cd2d4009a781b49d1bd9ef44@o325556.ingest.sentry.io/5245095";
    public static final String INSTABUG_KEY = "4e53155da9b00728caa5249f2e35d6b3";
    public static final String AMPLITUDE_KEY = "9098a21a950e7cb0933fb5b30affe5be";

    private WorkerThread apiThread;
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").disableHtmlEscaping().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    private OkHttpClient httpClient = new OkHttpClient.Builder().build();

    private ClubhouseAPIController() {
        apiThread = new WorkerThread("ApiThread");
        apiThread.start();
    }

    public static ClubhouseAPIController getInstance() {
        if (instance == null) {
            instance = new ClubhouseAPIController();
        }
        return instance;
    }

    public <T> void execRequest(ClubhouseAPIRequest<T> req) {
        apiThread.postRunnable(new RequestRunnable<>(req), 0);
    }

    public Gson getGson() {
        return gson;
    }

    private class RequestRunnable<T> implements Runnable {
        private ClubhouseAPIRequest<T> req;

        public RequestRunnable(ClubhouseAPIRequest<T> req) {
            this.req = req;
        }

        @Override
        public void run() {
            try {
                if (req.canceled)
                    return;
                Uri.Builder uri = API_URL.buildUpon().appendPath(req.path);
                if (req.queryParams != null) {
                    for (Map.Entry<String, String> e : req.queryParams.entrySet()) {
                        uri.appendQueryParameter(e.getKey(), e.getValue());
                    }
                }
                req.prepare();
                RequestBody reqBody = null;
                if (req.requestBody != null) {
                    reqBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), gson.toJson(req.requestBody));
                } else if (req.fileToUpload != null && req.fileFieldName != null) {
                    reqBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart(req.fileFieldName, req.fileToUpload.getName(), RequestBody.create(MediaType.get(req.fileMimeType), req.fileToUpload))
                            .build();
                } else if (req.contentUriToUpload != null && req.fileFieldName != null) {
                    ContentUriRequestBody part = new ContentUriRequestBody(req.contentUriToUpload);
                    reqBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart(req.fileFieldName, part.getFileName(), part)
                            .build();
                }
                if (DEBUG)
                    Log.i(TAG, "Sending: " + req.method + " " + uri);
                Request.Builder bldr = new Request.Builder()
                        .url(uri.build().toString());
                if ("POST".equals(req.method) && reqBody == null) {
                    Field fld = bldr.getClass().getDeclaredField("method");
                    fld.setAccessible(true);
                    fld.set(bldr, req.method);
                } else {
                    bldr.method(req.method, reqBody);
                }
                LocaleList locales = App.applicationContext.getResources().getConfiguration().getLocales();
                bldr.header("CH-Languages", locales.toLanguageTags())
                        .header("CH-Locale", locales.get(0).toLanguageTag().replace('-', '_'))
                        .header("Accept", "application/json")
                        .header("CH-AppBuild", API_BUILD_ID)
                        .header("CH-AppVersion", API_BUILD_VERSION)
                        .header("User-Agent", API_UA)
                        .header("CH-DeviceId", ClubhouseSession.deviceID);

                if (ClubhouseSession.isLoggedIn()) {
                    bldr.header("Authorization", "Token " + ClubhouseSession.userToken)
                            .header("CH-UserID", ClubhouseSession.userID);
                }
                Call call = httpClient.newCall(bldr.build());
                if (DEBUG)
                    Log.i(TAG, call.request().headers().toString());
                req.currentRequest = call;
                try (Response resp = call.execute()) {
                    ResponseBody body = resp.body();
                    if (DEBUG)
                        Log.i(TAG, "Code: " + resp.code());
                    if (resp.code() == 200) {
                        String respStr = body.string();
                        if (DEBUG)
                            Log.i(TAG, "Raw response: " + respStr);
//						T robj=gson.fromJson(body.charStream(), req.responseClass);
                        T robj = req.parse(respStr);
                        if (DEBUG)
                            Log.i(TAG, "Parsed response: " + robj);
                        req.onSuccess(robj);
                    } else {
                        String respStr = body.string();
                        if (DEBUG)
                            Log.i(TAG, "Raw response: " + respStr);
                        BaseResponse br = gson.fromJson(respStr, BaseResponse.class);
                        req.onError(new ClubhouseErrorResponse(br));
                    }
                }
            } catch (Exception x) {
                Log.w(TAG, x);
                String msg = BuildConfig.DEBUG ? x.getLocalizedMessage() : null;
                req.onError(new ClubhouseErrorResponse(-1, msg));
            }
        }
    }
}
