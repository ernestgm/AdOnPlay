import android.util.JsonReader
import android.util.Log
import com.geniusdevelops.adonplay.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import okhttp3.*

open class CableClient(
    val deviceId: String
) {
    protected val client = OkHttpClient()
    protected var webSocket: WebSocket? = null
    protected var request: Request

    init {
        val url = "${BuildConfig.WS_BASE_URL}?device_id=${deviceId}"
        request = Request.Builder().url(url).build()
    }

    fun disconnect() {
        webSocket?.close(1000, "Bye")
    }
}
