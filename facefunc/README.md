# facefunc 源码说明（Compose 版）

你要的源码已按步骤文档拆好，目录下文件含义如下：

- `FaceLivenessComposeActivity.kt`：Compose 页面宿主，处理相机权限并触发流程。
- `FaceLivenessComposeScreen.kt`：Compose UI，展示当前步骤、token、检测码、核验结果。
- `FaceLivenessCoordinator.kt`：核心流程控制（获取 bizToken -> 初始化 SDK -> startDetect -> verify）。
- `FaceBackendApi.kt`：后端 API 占位实现（目前使用模拟 token）。
- `FaceLivenessUiState.kt`：UI 状态模型。

## 关键流程（与文档一致）

1. 点击“开始炫彩活体检测”
2. 申请相机权限
3. `fetchBizToken(livenessId)` 从后端拿 `bizToken`（目前模拟）
4. 组装 `FaceLiveDetectConfig` 并调用 `FaceLiveManager.startDetect`
5. `onDetectFinish == 1000` 后调用 `verify`

## 炫彩活体说明

炫彩活体不是客户端本地开关，核心是 **后端签发的 bizToken 对应炫彩场景 LIVENESS_ID**。
代码里使用 `LIVENESS_ID = "flash_scene_demo_id"` 作为示例，你替换成真实场景即可。

## 你需要替换的内容

- `FaceLivenessCoordinator.kt` 中的 `HOST/LIVENESS_ID/VERIFY_ID`
- `FaceBackendApi.kt` 改成真实后端请求，删掉模拟实现

