package com.face.demo;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpPrivateUtil {

    private Context mContext;


    public HttpPrivateUtil(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void getLisenceAndConfigPrivate(String url, String data, HttpCallBackListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("biz_token", "default-token");
            json.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        requestForJson(url, json, listener);
    }

    public void verifyPrivate(String url, String livenessType, byte[] delta, byte[] imgRef1, final HttpCallBackListener listener) {
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        Log.e("ttt", "verifyPrivate: liveness_type = " + livenessType );
        requestBody.addFormDataPart("liveness_type", livenessType);

        if (delta != null && delta.length > 0) {
            RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), delta);
            requestBody.addFormDataPart("facelive_data", "facelive_data", fileBody);
        }

        if (imgRef1 != null && imgRef1.length > 0) {
            RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), imgRef1);
            requestBody.addFormDataPart("image_ref1", "image_ref1", fileBody);
        }

        MultipartBody body = requestBody.build();
        requestForPost(url, body, listener);
    }

    public void requestForJson(String url, JSONObject body, final HttpCallBackListener listener) {
        MediaType json = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(json, String.valueOf(body));
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(requestBody);
        
        Request request = builder.build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener != null) {
                    listener.onFailure(-1, e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (listener != null) {
                    if (response.code() == 200) {
                        listener.onSuccess(response.body().string());
                    } else {
                        listener.onFailure(response.code(), response.body().string());
                    }

                }
            }
        });
    }

    public void requestForPost(String url, MultipartBody body, final HttpCallBackListener listener) {

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener != null) {
                    listener.onFailure(-1, e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (listener != null) {
                    if (response.code() == 200) {
                        listener.onSuccess(response.body().string());
                    } else {
                        listener.onFailure(response.code(), response.body().string());
                    }

                }
            }
        });

    }

}
