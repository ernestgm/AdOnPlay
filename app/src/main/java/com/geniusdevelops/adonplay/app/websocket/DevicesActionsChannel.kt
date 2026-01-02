package com.geniusdevelops.adonplay.app.websocket

import CableClient
import android.util.Log
import com.geniusdevelops.adonplay.BuildConfig
import com.geniusdevelops.adonplay.app.util.parseJsonToMap
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class DevicesActionsChannel(deviceId: String) : CableClient(deviceId) {
    fun connect(onMessage: (Map<String, Any?>?) -> Unit, onError: (Throwable) -> Unit) {
        connect("DevicesActionsChannel", onMessage, onError)
    }
}