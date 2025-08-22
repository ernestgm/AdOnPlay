package com.geniusdevelops.adonplay.app.views.components

import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.geniusdevelops.adonplay.ui.theme.common.Loading

@Composable
fun WebViewWithCookies(
    url: String,
    cookies: Map<String, String>
) {
    var pageLoaded by remember { mutableStateOf(0f) }

    if (pageLoaded == 0F) {
        Loading(
            text = "",
            modifier = Modifier
                .absoluteOffset()
                .fillMaxSize()
        )
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)
            .onGloballyPositioned { layoutCoordinates ->
                Log.d("Height Layout", layoutCoordinates.size.height.toString())
            }
            .alpha(pageLoaded),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true

                WebView.setWebContentsDebuggingEnabled(true)
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(this, true)

                // Función para verificar cookies antes de cargar
                fun loadIfCookiesSet() {
                    val storedCookies = cookieManager.getCookie(url)
                    Log.d("Cookies", "Cookies guardadas: $storedCookies")
                    if (storedCookies != null && cookies.keys.all { storedCookies.contains(it) }) {
                        Log.d("Cookies", "✅ Todas las cookies presentes, cargando URL...")
                        loadUrl(url)
                    } else {
                        Log.w("Cookies", "⏳ Cookies aún no disponibles, reintentando...")
                        postDelayed({ loadIfCookiesSet() }, 100)
                    }
                }

                // Establecer cookies y luego verificar
                cookies.forEach { (name, value) ->
                    val cookieString = "$name=$value; path=/" // no usar domain si no es necesario
                    cookieManager.setCookie(url, cookieString) { success ->
                        Log.d("Cookies", "Seteada: $cookieString -> $success")
                    }
                }
                cookieManager.flush()
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.d("Height View", view?.height.toString())
                        Log.d("Cookies", "✅ URL cargada: $url")
                        pageLoaded = 1F
                    }
                }

                // Iniciar verificación
                loadIfCookiesSet()
            }
        })
}
