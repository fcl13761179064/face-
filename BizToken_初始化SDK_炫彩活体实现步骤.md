# BizToken、SDK 初始化、炫彩活体实现步骤（仅 MainActivity）

这份文档只针对 `MainActivity`（公有云）链路，按“你可以直接照着做”的方式整理。

---

## 1) 先理解三件事

- `biz_token`：活体检测一次流程的“临时凭证”，服务端接口返回。
- SDK 初始化：用 `FaceLiveDetectConfig` 把 `biz_token + host + modelPath` 传给 SDK。
- 炫彩活体：由 `LIVENESS_ID` 对应的控制台场景配置决定，不是在客户端写一个 `flash=true`。

---

## 2) 代码入口（从哪里开始）

主入口是 `MainActivity` 的按钮点击：

- 点击 `bt_start`
- 调 `startLivenessCheckFlow()`
- 再进入 `requestCameraPerm()`

这一步只是“总流程开关”，真正业务从拿 `biz_token` 开始。

---

## 3) 如何获取 biztoken（按步骤）

### Step A：生成签名

在 `getBizToken()` 里先调用：

- `generateSign()`

内部使用：

- `GenerateSign.appSign(API_KEY, SECRET, currtTime, expireTime)`

得到参数 `sign`，用于服务端鉴权。

### Step B：请求 biztoken 接口

调用：

- `mHttpUtil.getBiztoken(GET_BIZTOKEN_URL, mSign, SIGN_VERSION, LIVENESS_ID, callback)`

请求体关键字段：

- `sign`
- `sign_version`
- `liveness_id`

这些字段在 `HttpUtil.getBiztoken()` 中组装并通过 `multipart/form-data` 发出。

### Step C：解析响应

在回调 `onSuccess(String responseBody)`：

- 解析 JSON 字段 `biz_token`
- 保存到 `mBiztoken`
- 立刻调用 `startDetect(token, modelPath)`

---

## 4) 如何初始化 SDK 并开始检测

在 `startDetect(String token, String modelPath)` 里：

1. 创建配置对象：`FaceLiveDetectConfig config = new FaceLiveDetectConfig();`
2. 设置 `config.setBizToken(token);`
3. 设置 `config.setHost(HOST);`
4. 设置 `config.setModelPath(modelPath);`
5. 可选设置录屏：`config.setMediaProjection(...)`
6. 调用：
   - `FaceLiveManager.getInstance().startDetect(MainActivity.this, config, listener)`

回调关键点：

- `onPreDetectFinish(...)`：SDK 初始化结果（是否能进入活体页）。
- `onDetectFinish(...)`：活体采集完成结果。
  - `errorCode == 1000` 才调用 `verify()`。

---

## 5) 如何“开启炫彩活体拍照”

### 结论先说

你要开的不是客户端参数，而是 **控制台场景**：

- 在平台创建/配置“炫彩活体”场景
- 得到该场景的 `LIVENESS_ID`
- App 端请求 biztoken 时传这个 `LIVENESS_ID`

SDK 根据该 `biz_token` 进入对应活体流程（动作/炫彩）。

### 你要做的最小改动

1. 把 `MainActivity` 的 `LIVENESS_ID` 改成“炫彩场景”的 ID。
2. 保证 `GET_BIZTOKEN_URL` 对应环境能识别该场景。
3. 正常走 `getBizToken() -> startDetect()`，无需新增本地“炫彩开关”。

---

## 6) 最小可跑配置清单（必须）

在 `MainActivity` 补齐：

- `SECRET`
- `LIVENESS_ID`（炫彩场景）
- `VERIFY_ID`
- `GET_BIZTOKEN_URL`
- `VERIFY_URL`
- `HOST`

只要这些正确，主链路就是：

1. 权限通过
2. 拿 biztoken
3. 初始化 SDK
4. 拉起炫彩活体
5. 活体成功后 verify

---

## 7) 建议你按这个顺序联调

1. 先只测到 `getBizToken` 成功（日志里看到 `biz_token`）。
2. 再测 `onPreDetectFinish` 是否 `1000`。
3. 再测 `onDetectFinish` 是否 `1000`。
4. 最后看 `verify` 返回体里的业务字段（不是只看 HTTP 200）。

---

## 8) 对应代码文件索引

- 主流程：`app/src/main/java/com/face/demo/MainActivity.java`
- 获取 token / verify 请求：`app/src/main/java/com/face/demo/HttpUtil.java`
- 签名：`app/src/main/java/com/face/demo/GenerateSign.java`
- 回调接口：`app/src/main/java/com/face/demo/HttpCallBackListener.java`

