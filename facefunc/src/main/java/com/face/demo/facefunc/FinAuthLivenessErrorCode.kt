package com.face.demo.facefunc

/**
 * FinAuth Liveness Detection SDK V5.8.15 错误码说明（可持续补充）。
 * 目前先覆盖项目已出现和高频需要排查的错误码。
 */
object FinAuthLivenessErrorCode {

    private val codeReasonMap: Map<Int, String> = mapOf(
        1000 to "LIVENESS_FINISH / GET_CONFIG_SUCCESS：活体完成，且成功获取配置",
        1001 to "token过期了",
        1002 to "ILLEGAL_PARAMETER：参数非法，具体原因见 sdkMessage 的 {} 内容",
        1003 to "AUTHENTICATION_FAIL：鉴权失败，具体原因见 sdkMessage 的 {} 内容",
        1004 to "MOBILE_PHONE_NOT_SUPPORT：设备不支持",
        1005 to "具体异常信息（exceptionType_class_line），建议联系技术支持",
        1006 to "REQUEST_FREQUENTLY：同设备并发调用过于频繁",
        1007 to "NETWORK_TIME_OUT：网络请求超时",
        1008 to "INTERNAL_ERROR：网络配置错误，请重试；持续失败请联系支持",
        1009 to "INVALID_BUNDLE_ID：信息校验失败，请重启程序或设备后重试",
        1010 to "NETWORK_ERROR：无法连接网络，请检查网络后重试",
        1011 to "USER_CANCEL：用户取消",
        1012 to "NO_CAMERA_PERMISSION：无相机权限",
        1013 to "DEVICE_NOT_SUPPORT：无法启动相机，请确认相机状态",
        1014 to "FACE_INIT_FAIL：人脸初始化失败，具体原因见 sdkMessage 的 {} 内容",
        1015 to "错误码 1015：官方说明未提供（待补充）",
        1016 to "LIVENESS_FAILURE：活体检测失败",
        1017 to "GO_TO_BACKGROUND：应用切后台导致活体失败",
        1018 to "LIVENESS_TIME_OUT：长时间无操作导致超时",
        1019 to "DATA_UPLOAD_FAIL：数据上传失败",
        1020 to "错误码 1020：官方说明未提供（待补充）",
        1021 to "错误码 1021：官方说明未提供（待补充）",
        1022 to "MOBILE_PHONE_NOT_SUPPORT_SCRN：设备不支持录屏",
        1023 to "SCRN_AUTHORIZATION_FAIL：录屏授权失败",
        1024 to "SCRN_RECORD_FAIL：录屏失败",
        1025 to "VIDEO_SAVE_FAIL：视频保存失败",
        1026 to "NO_AUDIO_RECORD_PERMISSION：无录音权限",
        4205 to "LOAD_LIB_FAILED：动态库加载失败"
    )

    fun reasonOf(code: Int): String {
        return codeReasonMap[code] ?: "未知错误码，请结合 SDK errorMessage 和服务端日志排查"
    }

    fun format(code: Int, sdkMessage: String?): String {
        val reason = reasonOf(code)
        val msg = sdkMessage ?: ""
        return "code=$code, reason=$reason, sdkMessage=$msg"
    }
}

