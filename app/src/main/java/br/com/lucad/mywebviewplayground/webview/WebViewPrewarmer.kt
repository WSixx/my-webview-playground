package br.com.lucad.mywebviewplayground.webview

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView
import br.com.lucad.mywebviewplayground.bridge.NativeBridge
import br.com.lucad.mywebviewplayground.model.WebViewSettingsState

/**
 * Singleton/Manager helper for pre-warming WebView engine in the background
 * to eliminate Cold Start overhead when transitioning to WebView views.
 */
object WebViewPrewarmer {

    private var prewarmedWebView: WebView? = null
    var isPrewarmedAndReady: Boolean = false
        private set

    @SuppressLint("SetJavaScriptEnabled")
    fun prewarm(
        context: Context,
        settingsState: WebViewSettingsState = WebViewSettingsState.Default,
        nativeBridge: NativeBridge? = null
    ) {
        if (prewarmedWebView != null) return

        try {
            val appContext = context.applicationContext
            val webView = WebView(appContext).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                if (nativeBridge != null) {
                    WebViewSettingsManager.attachBridge(this, nativeBridge)
                }
                WebViewSettingsManager.applySettings(this, settingsState)
                // Load empty page to prime V8 JS engine & Chromium C++ render pipeline
                loadUrl("about:blank")
            }
            prewarmedWebView = webView
            isPrewarmedAndReady = true
            nativeBridge?.logSystemEvent("WebView context pre-warmed successfully in background")
        } catch (e: Exception) {
            isPrewarmedAndReady = false
            nativeBridge?.logSystemEvent("WebView pre-warming failed: ${e.localizedMessage}")
        }
    }

    fun consumePrewarmedWebView(): WebView? {
        val instance = prewarmedWebView
        prewarmedWebView = null
        isPrewarmedAndReady = false
        return instance
    }

    fun release(nativeBridge: NativeBridge? = null) {
        prewarmedWebView?.let {
            try {
                it.stopLoading()
                it.clearHistory()
                it.removeAllViews()
                it.destroy()
            } catch (_: Exception) {}
        }
        prewarmedWebView = null
        isPrewarmedAndReady = false
        nativeBridge?.logSystemEvent("Pre-warmed WebView released and context destroyed")
    }
}
