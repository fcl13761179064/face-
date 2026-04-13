# FinAuth Liveness Detection SDK V5.8.15 Code 说明

本文档放在 `facefunc` 模块下，用于记录 SDK 回调 `errorCode` 对应原因，并指导排查。

## 已落地到代码中的映射（官方清单版）

| code | 原因 |
|---|---|
| 1000 | LIVENESS_FINISH / GET_CONFIG_SUCCESS |
| 1001 | BIZ_TOKEN_DENIED |
| 1002 | ILLEGAL_PARAMETER（具体见 `{}`） |
| 1003 | AUTHENTICATION_FAIL（具体见 `{}`） |
| 1004 | MOBILE_PHONE_NOT_SUPPORT |
| 1005 | 具体异常格式：`Exception_Class_Line` |
| 1006 | REQUEST_FREQUENTLY |
| 1007 | NETWORK_TIME_OUT |
| 1008 | INTERNAL_ERROR |
| 1009 | INVALID_BUNDLE_ID |
| 1010 | NETWORK_ERROR |
| 1011 | USER_CANCEL |
| 1012 | NO_CAMERA_PERMISSION |
| 1013 | DEVICE_NOT_SUPPORT |
| 1014 | FACE_INIT_FAIL（具体见 `{}`） |
| 1015 | 官方说明未提供（待补充） |
| 1016 | LIVENESS_FAILURE |
| 1017 | GO_TO_BACKGROUND |
| 1018 | LIVENESS_TIME_OUT |
| 1019 | DATA_UPLOAD_FAIL |
| 1020 | 官方说明未提供（待补充） |
| 1021 | 官方说明未提供（待补充） |
| 1022 | MOBILE_PHONE_NOT_SUPPORT_SCRN |
| 1023 | SCRN_AUTHORIZATION_FAIL |
| 1024 | SCRN_RECORD_FAIL |
| 1025 | VIDEO_SAVE_FAIL |
| 1026 | NO_AUDIO_RECORD_PERMISSION |
| 4205 | LOAD_LIB_FAILED |

## 代码位置

- 映射工具：`facefunc/src/main/java/com/face/demo/facefunc/FinAuthLivenessErrorCode.kt`
- 打印位置：`facefunc/src/main/java/com/face/demo/facefunc/FaceLivenessCoordinator.kt`
  - `onPreDetectFinish(...)`
  - `onDetectFinish(...)`

## 打印格式

日志统一为：

`code=<code>, reason=<映射原因>, sdkMessage=<SDK原始errorMessage>`

示例：

`code=1005, reason=初始化失败：配置或鉴权信息异常..., sdkMessage=(JSONException_org.json.JSON_112)`

## 后续扩展

当前 `codeReasonMap` 已覆盖 `1000`、`1001-1026`、`4205`。
其中 `1015/1020/1021` 暂无你提供的官方定义，先标记为“待补充”。

