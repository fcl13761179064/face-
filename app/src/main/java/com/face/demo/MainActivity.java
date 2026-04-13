package com.face.demo;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.face.lv5.sdk.bean.FaceliveLocalFileInfo;
import com.face.lv5.sdk.manager.FaceLiveDetectConfig;
import com.face.lv5.sdk.manager.FaceLiveDetectListener;
import com.face.lv5.sdk.manager.FaceLiveManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

import static android.os.Build.VERSION_CODES.M;

/**
 * 本Demo是接入示例，原则上不建议支持直接使用，需要您根据业务场景进行配置再接入开发。
 * 接入步骤简易说明：
 * 步骤一： 获取biztoken
 * 步骤二： 开始检测，调用FaceLiveManager.getInstance().startDetect
 * 步骤三： 对活体检测结果进行处理，调用verify接口
 */
public class MainActivity extends Activity {

    private static final String GET_BIZTOKEN_URL = "https://api.yljz.com/finauth/v5/sdk/get_biz_token";
    private static final String VERIFY_URL = "https://api.yljz.com/finauth/v5/sdk/verify";
    private static final String HOST = "https://api.yljz.com";

    private static final String API_KEY = "V4H53ywc_g3XXMifN5Yk3a4tB8nK6dTD";
    private static final String SECRET = "";
    /**
     * LIVENESS_ID为活体场景ID，开发中需要根据业务场景从后台获取不同的活体场景ID，展示不同的活体类型(分为动作或炫彩)，同样一种活体类型，不同的配置也会有细节上的差别
	 * LIVENESS_ID（即活体场景ID）配置方式为登录https://yljz.com,点控制台，左侧的‘应用配置’->‘场景配置’->'SDK'->'活体采集场景'，可通过点击‘新增活体场景’按钮来创建场景，
 创建成功后的场景ID即为LIVENESS_ID
     * 重点：是否“炫彩活体”，不是在客户端硬编码切换，而是由该场景ID在控制台的场景配置决定。
     */
    private static final String LIVENESS_ID = "";
    /**
     * VERIFY_ID为做verify的时候需要的比对场景ID，
	 * VERIFY_ID（即比对场景ID）配置方法为登录https://yljz.com,点控制台->左侧的‘应用配置’->场景配置->SDK->verify场景->新增verify场景，创建成功后的场景ID即为verifyID
     */
    private static final String VERIFY_ID = "";
    private static final String SIGN_VERSION = "hmac_sha1";
    private String modelPath = "";// 模型本地存放路径

    private HttpUtil mHttpUtil;
    private String mSign;
    private String mBiztoken;
    /**
     * 调用verify接口时，mComparisonType值为0代表人脸比对，值为1代表人身核验。如果进行人脸比对，需要在verify时传入一张图片，进而与活体数据进行比对；如果进行人身核验，则需要身份证名字和号码，然后据此调用verify接口进行比对。
     */
    private int mComparisonType = 0;


    /**
     * 屏幕录制
     */
    private static final int RECORD_REQUEST_CODE = 4;
    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private RecordService mRecordService = null;
    private boolean mIsBinded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btStart = findViewById(R.id.bt_start);
        Button btRequest = findViewById(R.id.bt_request);
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("公有云");

        mHttpUtil = new HttpUtil(this);
        modelPath = saveAssets("facelivemodel.bin","model");

        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLivenessCheckFlow();
            }
        });

        btRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestScreenPermission();
            }
        });
    }

    /**
     * 活体检测总入口（MainActivity 公有云链路）
     * 步骤：
     * 1) 先过相机权限
     * 2) 获取bizToken
     * 3) 用bizToken初始化SDK并拉起活体检测
     */
    private void startLivenessCheckFlow() {
        requestCameraPerm();
    }

    /**
     * 此处是录屏权限的申请
     *
     * 说明：
     * 1、录屏权限的申请有两种方式，一种是App层申请，通过FaceLiveDetectConfig.setMediaProjection()方法设置进来；另一种是App不申请，由SDK层申请。建议使用第二种方式。
     * 2.另外，需要说明如果App申请录屏权限，则App要负责释放，否则会出现不可控的问题。
     *
     * 3.如果App的targetSdkVersion高于28，则需要在App清单文件中加入
     * <service
     *             android:name="com.face.lv5.sdk.screen.service.MediaProjectionService"
     *             android:foregroundServiceType="mediaProjection"
     *             />
     *  ，低于29则不用增加。
     *
     */
    private void requestScreenPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            Intent captureIntent = projectionManager.createScreenCaptureIntent();
            startActivityForResult(captureIntent, RECORD_REQUEST_CODE);
        }
    }



    protected ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RecordService.RecordBinder binder = (RecordService.RecordBinder) service;
            mRecordService = binder.getRecordService();
            mIsBinded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsBinded = false;
        }
    };

    /**
     * 生成签名
     */
    private void generateSign() {
        long currtTime = System.currentTimeMillis() / 1000;
        long expireTime = (System.currentTimeMillis() + 60 * 60 * 100) / 1000;
        mSign = GenerateSign.appSign(API_KEY, SECRET, currtTime, expireTime).replaceAll("[\\s*\t\n\r]", "");
    }

    /**
     * 为了保证业务灵活性，建议该请求放在云端。
     * 此接口是为了获取检测唯一凭证biztoken，biztoken用于初始化活体检测SDK。
     * LIVENESS_ID： 活体场景ID。分为动作活体和炫彩活体，不同的ID对应着一系列的配置项，需要在finauth控制台配置。
     */
    private void getBizToken() {
        // Step 1: 生成签名，服务端据此校验请求合法性
        generateSign();
        // Step 2: 以 sign + liveness_id 请求一次性凭证 biz_token
        mHttpUtil.getBiztoken(GET_BIZTOKEN_URL, mSign, SIGN_VERSION, LIVENESS_ID, new HttpCallBackListener() {
            @Override
            public void onSuccess(String responseBody) {
                try {
                    JSONObject json = new JSONObject(responseBody);
                    String token = json.getString("biz_token");
                    mBiztoken = token;
                    // Step 3: 使用拿到的 biz_token 初始化并启动 SDK
                    startDetect(token, modelPath);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onFailure(int statusCode, String responseBody) {
                Log.e("finauthv5", "onFailure: statusCode = " + statusCode);
                Log.e("finauthv5", "onFailure: responseBody = " + responseBody);
            }
        });
    }

    /**
     * 为了保证业务灵活性，建议该请求放在云端。
     * verify是根据活体检测获取的活体数据进行人脸比对或者人身核验（也即KYC验证），mComparisonType等于0代表人脸比对，mComparisonType等于1代表人身核验（也即KYC验证）。
	 * 如果是人脸比对，需要传入一张照片（image_ref1），跟活体数据进行比对；如果是人身核验需要传入身份证名字和身份证号。
     * 具体可以参考FinAuthID增强版的‘获取比对结果’接口文档（https://yljz.com/document/finauth-guide-docs/v5_get_result），这里演示的是基础配置。
     */
    private void verify() {
        generateSign();
        String uuid = UUID.randomUUID().toString();
        String idCardName = "张三";//用户姓名
        String idCardNum = "330721197403162417";//用户身份证号码
        byte [] imgRef1 = null;
         mHttpUtil.verify(VERIFY_URL, mSign, SIGN_VERSION, mBiztoken, VERIFY_ID, uuid, mComparisonType, idCardName, idCardNum, imgRef1, new HttpCallBackListener() {
             @Override
             public void onSuccess(String responseBody) {
                 Log.e("finauthv5", "verify onSuccess : responseBody=" + responseBody);
				/*
				    注意，此处的verify接口调用成功并不一定意味着人脸比对通过，人脸比对是否通过要看responseBody中的result_message节点值，具体请参考yljz官网‘获取比对结果’接口文档
				*/
             }

             @Override
             public void onFailure(int statusCode, String responseBody) {
                 /**
                  * 具体错误码请查看yljz官网的FinAuthID增强版的‘获取比对结果’接口文档
                  */
                 Log.e("finauthv5", "verify onFailure : errorCode=" + statusCode + ",responseBody=" + responseBody);
             }
         });
    }

    /**
     * 开始检测
     * @param token
     * @param modelPath
     */
    private void startDetect(String token, String modelPath) {
        FaceLiveDetectConfig config = new FaceLiveDetectConfig();
        // SDK 初始化关键参数：bizToken（必填）
        config.setBizToken(token);
        // SDK 网关地址（需与申请 token 的环境一致）
        config.setHost(HOST);
        /**
         * 模型传入支持两种方式： 1 通过ModelPath进行设置；2 可以将facelivemodel.bin 文件放入app的raw目录下供SDK内部读取
         * ps:两者皆提供SDK内部优先使用ModelPath下的model文件
         */
        config.setModelPath(modelPath);
        config.setMediaProjection(mRecordService == null ? null : mRecordService.getMediaProjection());
        // 说明：炫彩/动作活体类型由 bizToken 对应的 LIVENESS_ID 场景决定，此处无需额外设置“炫彩开关”。

        /**
         * 此处即将调用检测接口，建议此处展示Loading，在回调时取消。
         */

        FaceLiveManager.getInstance().startDetect(MainActivity.this, config, new FaceLiveDetectListener() {
            @Override
            public void onPreDetectFinish(int errorCode, String errorMessage) {
                Log.e("finauthv5", "onPreDetectFinish : errorCode=" + errorCode + ",errorMessage=" + errorMessage);
                /**
                 * 即将进入活体检测界面，errorCode为1000代表活体检测SDK初始化成功；否则代表错误，可以根据相应的errorMessage进行业务处理。此处可以取消Loading。
                 */
            }

            @Override
            public void onDetectFinish(int errorCode, String errorMessage, String bizToken) {
                Log.e("finauthv5", "onDetectFinish : errorCode=" + errorCode + ",errorMessage=" + errorMessage);
                /**
                 * 此处是活体检测的结果回调，返回码1000代表成功，可以调用verify接口。其他都是错误。只有成功时才可以进行verify，否则没有意义，甚至会出现一些错误，望注意！
                 */
                if (errorCode == 1000) {
                    // 活体成功后再调用 verify，获取最终比对/核验结果
                    verify();
                } else {
                    //失败的业务处理
                }

                if (mRecordService != null && mRecordService.getMediaProjection() != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mRecordService.getMediaProjection().stop();
                    }
                    mRecordService.setMediaProjection(null);
                }
                if (mIsBinded) {
                    unbindService(connection);
                    mIsBinded = false;
                }
                if (projectionManager != null) {
                    projectionManager = null;
                }
            }

            @Override
            public void onLivenessFileCallback(String livenessFilePath) {

                Log.e("finauthv5", "onLivenessFileCallback : livenessFilePath=" + livenessFilePath);
				/*
				该回调方法可以获取到留存到本地的活体数据的路径。
				*/
            }

            @Override
            public void onLivenessLocalFileCallBack(FaceliveLocalFileInfo faceliveLocalFileInfo) {

            }
        });
    }

    /**
     * 申请相机权限
     */
    private void requestCameraPerm() {
        if (android.os.Build.VERSION.SDK_INT >= M) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                //进行权限请求
                requestPermissions(
                        new String[]{Manifest.permission.CAMERA},
                        2);
            } else {
                getBizToken();
            }
        } else {
            getBizToken();
        }
    }

    /**
     * 申请相机权限的回调
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 2) {
            if (grantResults.length < 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                /**
                 * 用户未授权时的业务处理，比如弹窗展示此权限的重要性
                 */
            } else {
                getBizToken();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
            Intent intentRecord = new Intent(this, RecordService.class);
            intentRecord.putExtra("code", resultCode);
            intentRecord.putExtra("data", data);
            bindService(intentRecord, connection, BIND_AUTO_CREATE);
        }
    }

    /**
     * 把SDK提供的模型数据保存到手机路径下
     * @param fileName Asseets文件夹下模型数据名字
     * @param path  将要保存的目标路径
     * @return
     */
    private String saveAssets(String fileName, String path) {
        File dir = new File(this.getExternalFilesDir("face"), path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return null;
            }
        }
        File file = new File(dir, fileName);

        FileOutputStream fos = null;
        InputStream is = null;
        String ret = null;
        try {
            int count;
            byte[] buffer = new byte[1024];
            fos = new FileOutputStream(file);
            is = this.getAssets().open(fileName);
            while ((count = is.read(buffer)) != -1) {
                fos.write(buffer, 0, count);
            }

            ret = file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fos != null) fos.close();
                if (is != null) is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
}