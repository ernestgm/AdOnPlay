package com.geniusdevelops.adonplay.app.api.responses

data class DeviceVerifyCode(
    val id: Int,
    val device_id: String,
    val code: String,
    val registered: Boolean,
)
