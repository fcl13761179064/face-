package com.face.demo;

import android.content.Context;
import android.text.TextUtils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {

    private Context mContext;


    public HttpUtil(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void getBiztoken(String url, String sign, String version, String livenessId, HttpCallBackListener listener) {
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        requestBody.addFormDataPart("sign", sign);
        requestBody.addFormDataPart("sign_version", version);
        if (!TextUtils.isEmpty(livenessId)){
            requestBody.addFormDataPart("liveness_id", livenessId);
        }
        MultipartBody body = requestBody.build();
        requestForPost(url, body, listener);
    }

    public void verify(String url, String sign, String version, String bizToken, String verifyId, String uuId, int comparisonType, String idCardName, String idCardNum, byte[] imgRef1, final HttpCallBackListener listener) {
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        requestBody.addFormDataPart("sign", sign);
        requestBody.addFormDataPart("sign_version", version);
        requestBody.addFormDataPart("comparison_type", comparisonType + "");
        requestBody.addFormDataPart("data_type", 0 + "");
        requestBody.addFormDataPart("verify_id", verifyId);
        requestBody.addFormDataPart("encryption_type", 0 + "");

        requestBody.addFormDataPart("biz_token", bizToken);
        requestBody.addFormDataPart("uuid", uuId);

        if (idCardName != null & idCardNum != null) {
            requestBody.addFormDataPart("idcard_name", idCardName);
            requestBody.addFormDataPart("idcard_number", idCardNum);
        }

        if (imgRef1 != null && imgRef1.length > 0) {
            RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), imgRef1);
            requestBody.addFormDataPart("image_ref1", "image_ref1", fileBody);
        }

        MultipartBody body = requestBody.build();
        requestForPost(url, body, listener);
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
