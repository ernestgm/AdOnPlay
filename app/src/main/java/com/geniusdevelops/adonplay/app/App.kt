package com.geniusdevelops.adonplay.app

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
import com.geniusdevelops.adonplay.R
import com.geniusdevelops.adonplay.app.api.responses.DeviceVerifyCode
import com.geniusdevelops.adonplay.app.util.DeviceUtils
import com.geniusdevelops.adonplay.app.viewmodels.AppUiState
import com.geniusdevelops.adonplay.app.viewmodels.AppViewModel
import com.geniusdevelops.adonplay.app.views.PlayerScreen
import com.geniusdevelops.adonplay.ui.theme.common.ErrorWithButton
import com.geniusdevelops.adonplay.ui.theme.common.Loading
import kotlinx.coroutines.launch

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun App(
    appViewModel: AppViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by appViewModel.uiState.collectAsStateWithLifecycle()
    val deviceUtils = DeviceUtils(context)
    var msg by remember { mutableStateOf("") }
    var deviceVerifyCode by remember { mutableStateOf<DeviceVerifyCode?>(null) }
    var deviceToken by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        // Effect is triggered when HomeScreen is displayed
        coroutineScope.launch {
            appViewModel.verifyDevice(deviceUtils.getDeviceId())
        }
        onDispose {
            // Effect is triggered when HomeScreen is no longer displayed
        }
    }

    when (val s = uiState) {
        is AppUiState.Ready -> {
            s.deviceVerify?.let {
                deviceVerifyCode = it
            }
            if (s.token != null) {
                deviceToken = s.token
                deviceVerifyCode?.let {
                    PlayerScreen(
                        device = it,
                        token = deviceToken
                    )
                }
            } else {
                msg = "Your device is not registered. Contact the administrator."
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(all = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            modifier = Modifier
                                .width(300.dp)
                                .padding(all = 20.dp),
                            painter = painterResource(id = R.drawable.ic_error),
                            contentDescription = ""
                        )

                        Text(
                            text = "Device ID: ${deviceVerifyCode?.device_id}",
                            modifier = Modifier.padding(bottom = 8.dp),
                            style = MaterialTheme.typography.displaySmall
                        )
                        Text(
                            text = "Device Code: ${deviceVerifyCode?.code}",
                            modifier = Modifier.padding(bottom = 8.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                        )
                        ErrorWithButton(text = msg, modifier = Modifier.fillMaxSize()) {
                            coroutineScope.launch {
                                appViewModel.verifyDevice(deviceUtils.getDeviceId())
                            }
                        }
                    }
                }
            }

        }

        is AppUiState.Loading -> {
            Loading(
                text = "", modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )
        }

        is AppUiState.Error -> {
            ErrorWithButton(text = s.msg, modifier = Modifier.fillMaxSize()) {
                coroutineScope.launch {
                    appViewModel.verifyDevice(deviceUtils.getDeviceId())
                }
            }
        }

        else -> {}
            }
}
