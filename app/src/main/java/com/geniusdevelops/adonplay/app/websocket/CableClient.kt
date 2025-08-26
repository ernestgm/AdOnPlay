import android.util.Log
import com.geniusdevelops.adonplay.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import okhttp3.*

class CableClient(
    private val deviceId: String = ""
) {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    fun connect() {
        val url = "${BuildConfig.WS_BASE_URL}?device_id=${deviceId}"
        val request = Request.Builder().url(url).build()
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
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("Cable", "Closed: $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Firebase.crashlytics.recordException(t)
                Log.e("Cable", "Error: ${t.message}", t)
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Bye")
    }
}
