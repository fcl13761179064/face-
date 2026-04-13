package com.face.demo.facefunc

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.face.lv5.sdk.bean.FaceliveLocalFileInfo
import com.face.lv5.sdk.manager.FaceLiveDetectConfig
import com.face.lv5.sdk.manager.FaceLiveDetectListener
import com.face.lv5.sdk.manager.FaceLiveManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class FaceLivenessCoordinator(
    private val backendApi: FaceBackendApi = FaceBackendApi()
) : ViewModel() {

    companion object {
        private const val TAG = "FaceLivenessCoordinator"
        private const val HOST = "https://api-sgp.yljz.com"
    }

    private val _uiState = MutableStateFlow(FaceLivenessUiState())
    val uiState: StateFlow<FaceLivenessUiState> = _uiState.asStateFlow()

    fun startFlow(activity: Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                setLoading("Step1: 获取后端 BizToken")
                val bizToken = backendApi.fetchBizToken()
                _uiState.value = _uiState.value.copy(bizToken = bizToken, step = "Step2: 初始化 SDK")

                val modelPath = saveAssets(activity, "facelivemodel.bin", "model")
                    ?: error("模型文件写入失败")

                launchSdkDetect(activity, bizToken, modelPath)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "未知错误",
                    step = "失败"
                )
            }
        }
    }

    private fun launchSdkDetect(activity: Activity, bizToken: String, modelPath: String) {
        val config = FaceLiveDetectConfig().apply {
            setBizToken(bizToken)
            host = HOST
            setModelPath(modelPath)
        }

        _uiState.value = _uiState.value.copy(step = "Step3: 调用 SDK startDetect", isLoading = true)

        activity.runOnUiThread {
            FaceLiveManager.getInstance().startDetect(activity, config, object : FaceLiveDetectListener {
                override fun onPreDetectFinish(errorCode: Int, errorMessage: String?) {
                    Log.e(TAG, "onPreDetectFinish: code=$errorCode, msg=$errorMessage")
                    Log.e(TAG, "config===: ${config.modelPath},=======${config.host},=======${config.bizToken},")
                    Log.e(TAG, "SDK初始化完成: code=$errorCode    errosmessage=${errorMessage}")
                    _uiState.value = _uiState.value.copy(step = "SDK初始化完成: code=$errorCode    errosmessage=${errorMessage}")
                }

                override fun onDetectFinish(errorCode: Int, errorMessage: String?, bizToken: String?) {
                    Log.e(TAG, "onDetectFinish: code=$errorCode, msg=$errorMessage")
                    if (errorCode == 1000 && !bizToken.isNullOrBlank()) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            detectCode = 1000,
                            verifyResponse = "活体检测成功，已拿到 SDK 返回 bizToken",
                            step = "Step4: 检测完成"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            detectCode = errorCode,
                            errorMessage = errorMessage ?: "活体失败",
                            step = "活体失败"
                        )
                    }
                }

                override fun onLivenessFileCallback(livenessFilePath: String?) {
                    Log.e(TAG, "onLivenessFileCallback: path=$livenessFilePath")
                }

                override fun onLivenessLocalFileCallBack(faceliveLocalFileInfo: FaceliveLocalFileInfo?) {
                    Log.e(TAG, "onLivenessLocalFileCallBack: info=$faceliveLocalFileInfo")
                }
            })
        }
    }

    private fun setLoading(step: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, step = step, errorMessage = "")
    }

    private fun saveAssets(context: Context, fileName: String, path: String): String? {
        val dir = File(context.getExternalFilesDir("face"), path)
        if (!dir.exists() && !dir.mkdirs()) return null
        val file = File(dir, fileName)

        var fos: FileOutputStream? = null
        var input: InputStream? = null
        return try {
            val buffer = ByteArray(1024)
            fos = FileOutputStream(file)
            input = context.assets.open(fileName)
            while (true) {
                val count = input.read(buffer)
                if (count == -1) break
                fos.write(buffer, 0, count)
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        } finally {
            try {
                fos?.close()
                input?.close()
            } catch (_: Exception) {
            }
        }
    }
}

