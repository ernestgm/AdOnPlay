import android.os.Handler
import android.os.Looper
import android.util.JsonReader
import android.util.Log
import com.geniusdevelops.adonplay.BuildConfig
import com.geniusdevelops.adonplay.app.util.DeviceInfo
import com.geniusdevelops.adonplay.app.util.parseJsonToMap
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import okhttp3.*
import java.util.concurrent.TimeUnit

open class CableClient(
    val deviceId: String
) {
    protected val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // Importante para WebSockets
        .build()
    protected var webSocket: WebSocket? = null
    protected var request: Request
    private val handler = Handler(Looper.getMainLooper())
    protected var isManuallyClosed = false

    init {
        val url = "${BuildConfig.WS_BASE_URL}?device_id=${deviceId}"
        request = Request.Builder().url(url).build()
    }

    fun connect(
        channel: String,
        onMessage: (Map<String, Any?>?) -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        isManuallyClosed = false
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("Cable", "Connected!")

                val subscribeMessage = """
                    {
                      "command": "subscribe",
                      "identifier": "{\"channel\":\"${channel}\"}"
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
                if (!isManuallyClosed) {
                    attemptReconnect(channel, onMessage, onError)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Firebase.crashlytics.recordException(t)
                Log.e("Cable", "Error: ${t.message}", t)
                attemptReconnect(channel, onMessage, onError)
                onError(t)
            }
        })
    }

    fun sendMessage(channel: String, data: DeviceInfo) {
        val message = """
                    {
                      "command": "message",
                      "identifier": "{\"channel\":\"${channel}\"}",
                      "data": "{\"device_id\":\"${deviceId}\",\"total_ram\":${data.totalRam},\"free_ram\":${data.freeRam},\"cpu_usage\":${data.cpuUsage},\"total_disk\":${data.totalDisk},\"free_disk\":${data.freeDisk}}"
                    }
                """.trimIndent()
        Log.d("Cable Status", "Data-> ${message}")
        webSocket?.send(message)
    }


    private fun attemptReconnect(
        channel: String,
        onMessage: (Map<String, Any?>?) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (isManuallyClosed) return

        Log.d("CableClient", "🔄 Reintentando conexión en 5 segundos...")
        handler.postDelayed({
            connect(channel, onMessage, onError)
        }, 5000) // Puedes implementar un delay exponencial aquí
    }

    fun disconnect() {
        isManuallyClosed = true
        webSocket?.close(1000, "Bye")
    }
}
