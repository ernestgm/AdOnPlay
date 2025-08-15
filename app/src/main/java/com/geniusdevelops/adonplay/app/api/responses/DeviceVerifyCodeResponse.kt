package com.geniusdevelops.adonplay.app.api.responses

data class DeviceVerifyCodeResponse(
    val device: DeviceVerifyCode,
    val token: String?
)
