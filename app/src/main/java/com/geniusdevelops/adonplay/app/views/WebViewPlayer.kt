package com.geniusdevelops.adonplay.app.views

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.geniusdevelops.adonplay.BuildConfig
import com.geniusdevelops.adonplay.R
import com.geniusdevelops.adonplay.app.api.responses.DeviceVerifyCode
import com.geniusdevelops.adonplay.app.util.DeviceUtils
import com.geniusdevelops.adonplay.app.viewmodels.AppUiState
import com.geniusdevelops.adonplay.app.viewmodels.AppViewModel
import com.geniusdevelops.adonplay.app.views.components.WebViewWithCookies
import com.geniusdevelops.adonplay.ui.theme.common.ErrorWithButton
import com.geniusdevelops.adonplay.ui.theme.common.Loading
import kotlinx.coroutines.launch

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayerScreen(
    device: DeviceVerifyCode,
    token: String,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
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
