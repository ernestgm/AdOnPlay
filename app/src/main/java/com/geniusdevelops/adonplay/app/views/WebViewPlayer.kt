package com.geniusdevelops.adonplay.app.views

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.geniusdevelops.adonplay.BuildConfig
import com.geniusdevelops.adonplay.app.api.responses.DeviceVerifyCode
import com.geniusdevelops.adonplay.app.util.DeviceUtils
import com.geniusdevelops.adonplay.app.views.components.WebViewWithCookies
import com.geniusdevelops.adonplay.app.websocket.DevicesActionsChannel
import kotlinx.coroutines.launch


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayerScreen(
    device: DeviceVerifyCode,
    token: String,
    portrait: Boolean = false
) {
    val context = LocalContext.current
    val deviceUtils = DeviceUtils(context)
    var isPortrait by remember { mutableStateOf(portrait) }
    val configuration = LocalConfiguration.current
    val screenWidthPx = configuration.screenWidthDp.dp
    val screenHeightPx = configuration.screenHeightDp.dp
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        // Effect is triggered when HomeScreen is displayed
        val deviceActionsChannel = DevicesActionsChannel(deviceUtils.getDeviceId())
        coroutineScope.launch {
            deviceActionsChannel.connect(onMessage = { data ->
                data?.let {
                    val message = data["message"] as? Map<*, *>
                    val messageType = message?.get("type")
                    if (messageType == "ejecute_portrait_change") {
                        val payloadMap = message.get("payload") as? Map<*, *>
                        isPortrait = payloadMap?.get("portrait") as Boolean
                    }
                }
            }, onError = {
                Log.e("PlayerScreen", "Error: ${it.message}", it)
            })
        }

        onDispose {
            deviceActionsChannel.disconnect()
        }
    }

    val modifier = if (isPortrait) {
        Modifier
            .requiredWidth(screenHeightPx)
            .requiredHeight(screenWidthPx)
            .graphicsLayer {
                rotationZ = 90f
            }
    } else {
        Modifier
            .fillMaxSize()
    }

    Box(
        modifier = modifier
            .padding(all = 0.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WebViewWithCookies(
                url = BuildConfig.PLAYER_BASE_URL,
                cookies = mapOf(
                    "device_code" to device.code,
                    "device_token" to token,
                    "device_id" to device.device_id
                )
            )
        }
    }
}
