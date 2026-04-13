package com.face.demo;

/**
 * Created by mafuxin on 2018/6/14.
 */

public interface HttpCallBackListener {

    void onSuccess(String responseBody);

    void onFailure(int statusCode, String responseBody);
}
