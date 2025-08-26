package com.geniusdevelops.adonplay.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geniusdevelops.adonplay.app.api.requests.DeviceVerifyCodeRequest
import com.geniusdevelops.adonplay.app.api.responses.DeviceVerifyCode
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.nio.channels.UnresolvedAddressException

class AppViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<AppUiState?>(null)
    val uiState: StateFlow<AppUiState?> = _uiState

    fun verifyDevice(deviceId: String) {
        _uiState.value = AppUiState.Loading
        viewModelScope.launch {
            try {
                val response =
                    APIManager.devices.setDeviceVerifyCode(DeviceVerifyCodeRequest(device_id = deviceId))
                if (response.isSuccessful) {
                    val verifyCode = response.body()
                    _uiState.value =
                        AppUiState.Ready(deviceVerify = verifyCode?.device, verifyCode?.token)
                }
            } catch (e: IOException) {
                // Maneja errores de red (ej. sin conexión a internet)
                showException(e)
                println("Error de red: ${e.message}")
            } catch (e: Exception) {
                // Maneja otras excepciones
                showException(e)
                println("Error inesperado: ${e.message}")
            }
        }
    }

    private fun showException(e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
        when (e) {
            is UnresolvedAddressException -> {
                _uiState.value = AppUiState.Error("Network Error: Check your internet connection.")
            }

            else -> {
                _uiState.value = AppUiState.Error("Error: " + e.message.toString())
            }
        }
    }
}

sealed interface AppUiState {
    data object Loading : AppUiState
    data class Error(val msg: String = "") : AppUiState
    data class Ready(
        val deviceVerify: DeviceVerifyCode?,
        val token: String?
    ) : AppUiState
}