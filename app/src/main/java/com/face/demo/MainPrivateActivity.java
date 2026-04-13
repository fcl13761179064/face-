package com.face.demo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.face.lv5.sdk.bean.FaceliveLocalFileInfo;
import com.face.lv5.sdk.listener.FaceliveGetConfigCallback;
import com.face.lv5.sdk.listener.FaceliveRequestFinishCallback;
import com.face.lv5.sdk.manager.FaceLiveLivenessTypeE;
import com.face.lv5.sdk.manager.FaceLiveDetectPrivateConfig;
import com.face.lv5.sdk.manager.FaceLiveDetectPrivateListener;
import com.face.lv5.sdk.manager.FaceLivePrivateManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static android.os.Build.VERSION_CODES.M;

public class MainPrivateActivity extends Activity{
    private static final String TAG = "MainPrivateActivity";
    private static final String GET_LICENSE_AND_CONFIG_URL = "";
    private static final String VERIFY_URL = "";


    private String modelPath = "";// 模型本地存放路径

    private HttpPrivateUtil mHttpUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btStart = findViewById(R.id.bt_start);
        Button btRequest = findViewById(R.id.bt_request);
        btRequest.setVisibility(View.GONE);
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("私有云");

        mHttpUtil = new HttpPrivateUtil(this);
        modelPath = saveAssets("facelivemodel.bin","model");

        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestCameraPerm();
            }
        });
    }

    /**
     * 为了保证业务灵活性，建议该请求放在云端。
     * verify是根据活体检测获取的活体数据进行人脸比对或者人身核验（也即KYC验证）。
     * 如果是人脸比对，需要传入一张照片（image_ref1），跟活体数据进行比对；如果是人身核验需要传入身份证名字和身份证号。
     * 具体可以参考FinAuthID的接口文档，这里演示的是基础配置。
     */
    private void verify(String livenessType, byte[] delta) {
        byte [] imageRef1 = null;
        mHttpUtil.verifyPrivate(VERIFY_URL, livenessType, delta, imageRef1, new HttpCallBackListener() {
            @Override
            public void onSuccess(String responseBody) {
                Log.e("finauthv5", "verify onSuccess : responseBody=" + responseBody);
                /**
                 注意，此处的verify接口调用成功并不一定意味着人脸比对通过，人脸比对是否通过要看responseBody中的result_message节点值，具体请参考yljz官网‘获取比对结果’接口文档
                 */
            }

            @Override
            public void onFailure(int statusCode, String responseBody) {
                /**
                 * 具体错误码请查看yljz官网的FinAuthID的‘获取比对结果’接口文档
                 */
                Log.e("finauthv5", "verify onFailure : errorCode=" + statusCode + ",responseBody=" + responseBody);
            }
        });
    }


    /**
     * 开始检测
     */
    private void startDetect() {
        FaceLiveDetectPrivateConfig config = new FaceLiveDetectPrivateConfig();
        config.setUrl(GET_LICENSE_AND_CONFIG_URL);
        String modelPath = saveAssets("facelivemodel.bin", "model");
        config.setModelPath(modelPath);
        FaceLivePrivateManager.getInstance().startDetect(getApplicationContext(), config, new FaceliveGetConfigCallback() {
            @Override
            public void onGetConfig(String data, FaceliveRequestFinishCallback callback) {
                mHttpUtil.getLisenceAndConfigPrivate(GET_LICENSE_AND_CONFIG_URL, data, new HttpCallBackListener() {
                    @Override
                    public void onSuccess(String responseBody) {
                        Log.e(TAG, "onSuccess: responseBody = " +responseBody );
                        callback.onFinish(responseBody);
                    }

                    @Override
                    public void onFailure(int statusCode, String responseBody) {
                        Log.e(TAG, "onFailure: statusCode = " + statusCode );
                        Log.e(TAG, "onFailure: responseBody = " + responseBody );
                    }
                });
            }
        }, new FaceLiveDetectPrivateListener() {
            @Override
            public void onPreDetectFinish(int errorCode, String errorMessage) {
                Log.e("finauthv5", "onPreDetectFinish : errorCode=" + errorCode + ",errorMessage=" + errorMessage);
                /**
                 * 即将进入活体检测界面，errorCode为1000代表活体检测SDK初始化成功；否则代表错误，可以根据相应的errorMessage进行业务处理。此处可以取消Loading。
                 */
            }

            @Override
            public void onDetectFinish(int errorCode, String errorMessage, String bizToken, byte[] delta) {
                Log.e("finauthv5", "onDetectFinish : errorCode=" + errorCode + ",errorMessage=" + errorMessage + ",   delta len = " + delta.length);
                /**
                 * 此处是活体检测的结果回调，返回码1000代表成功，可以调用verify接口。其他都是错误。只有成功时才可以进行verify，否则没有意义，甚至会出现一些错误，望注意！
                 */
                String livenessType = "active";
                if (config.getLivenessType() == FaceLiveLivenessTypeE.Flash) {
                    livenessType = "flash";
                } else if (config.getLivenessType() == FaceLiveLivenessTypeE.Initiative_Flash) {
                    livenessType = "initiative_flash";
                }
                verify(livenessType, delta);
            }

            @Override
            public void onLivenessFileCallback(String livenessFilePath) {
                Log.e("finauthv5", "onLivenessFileCallback : livenessFilePath=" + livenessFilePath);
                /**
                 该回调方法可以获取到留存到本地的活体数据的路径。
                 */
            }

            @Override
            public void onLivenessLocalFileCallBack(FaceliveLocalFileInfo faceliveLocalFileInfo) {
                /**
                 该回调方法可以获取到留存到本地的活体数据的路径，当录屏功能开启时生效
                 */
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
                startDetect();
            }
        } else {
            startDetect();
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
                startDetect();
            }
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
