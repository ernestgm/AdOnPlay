package com.geniusdevelops.adonplay.app.api.services

import com.geniusdevelops.adonplay.app.api.requests.DeviceVerifyCodeRequest
import com.geniusdevelops.adonplay.app.api.responses.DeviceVerifyCodeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface DeviceServices {
    @POST("devices_verify_codes")
    suspend fun setDeviceVerifyCode(
        @Body body: DeviceVerifyCodeRequest
    ): Response<DeviceVerifyCodeResponse>
}