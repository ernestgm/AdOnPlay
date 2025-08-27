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

class StatusActionsChannel(deviceId: String) : CableClient(deviceId) {
    fun connect(onMessage: (Map<String, Any?>?) -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("Cable", "Connected!")

                val subscribeMessage = """
                    {
                      "command": "subscribe",
                      "identifier": "{\"channel\":\"StatusActionsChannel\",\"status\":1}"
                    }
                """.trimIndent()

                webSocket.send(subscribeMessage)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("Cable", "Message: $text")
                onMessage(parseJsonToMap(text))
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("Cable", "Closed: $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Firebase.crashlytics.recordException(t)
                Log.e("Cable", "Error: ${t.message}", t)
                onError(t)
            }
        })
    }
}