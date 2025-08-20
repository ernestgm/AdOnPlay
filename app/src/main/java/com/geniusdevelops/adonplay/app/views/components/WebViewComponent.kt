package com.geniusdevelops.adonplay.app.views.components

import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun WebViewWithCookies(url: String, cookies: Map<String, String>) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
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
                settings.userAgentString =
                    "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 Chrome/95.0.4638.74 Mobile Safari/537.36"

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
                        Log.d("Cookies", "✅ URL cargada: $url")
                    }
                }

                // Iniciar verificación
                loadIfCookiesSet()
            }
        })
}
