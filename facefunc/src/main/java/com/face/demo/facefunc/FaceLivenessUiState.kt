package com.face.demo.facefunc

data class FaceLivenessUiState(
    val title: String = "炫彩活体检测",
    val bizToken: String = "",
    val isLoading: Boolean = false,
    val step: String = "等待开始",
    val detectCode: Int? = null,
    val verifyResponse: String = "",
    val errorMessage: String = ""
)

