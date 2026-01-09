package com.geniusdevelops.adonplay.app.websocket

import CableClient
import android.util.Log
import com.geniusdevelops.adonplay.BuildConfig
import com.geniusdevelops.adonplay.app.util.DeviceInfo
import com.geniusdevelops.adonplay.app.util.parseJsonToMap
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class StatusActionsChannel(deviceId: String) : CableClient(deviceId) {
    fun connect() {
        connect("StatusActionsChannel")
    }

    fun sendData(totalRam: Long, freeRam: Long, cpuUsage: Double, totalDisk: Long, freeDisk: Long) {
        sendMessage(
            "StatusActionsChannel",
            DeviceInfo(totalRam, freeRam, cpuUsage, totalDisk, freeDisk)
        )
    }
}