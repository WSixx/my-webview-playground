package br.com.lucad.mywebviewplayground.model

import android.webkit.WebSettings

/**
 * Configuration model representing all tunable WebView settings.
 */
data class WebViewSettingsState(
    val javaScriptEnabled: Boolean = true,
    val domStorageEnabled: Boolean = true,
    val cacheMode: Int = WebSettings.LOAD_DEFAULT,
    val hardwareAccelerationEnabled: Boolean = true,
    val allowFileAccess: Boolean = true,
    val allowContentAccess: Boolean = true,
    val databaseEnabled: Boolean = true,
    val useWideViewPort: Boolean = true,
    val loadWithOverviewMode: Boolean = true,
    val supportZoom: Boolean = true,
    val builtInZoomControls: Boolean = false,
    val displayZoomControls: Boolean = false,
    val userAgent: String? = null,
    val mediaPlaybackRequiresUserGesture: Boolean = false
) {
    companion object {
        val Default = WebViewSettingsState()

        val HighPerformance = WebViewSettingsState(
            javaScriptEnabled = true,
            domStorageEnabled = true,
            cacheMode = WebSettings.LOAD_NO_CACHE,
            hardwareAccelerationEnabled = true,
            allowFileAccess = true,
            allowContentAccess = true,
            databaseEnabled = true
        )
    }
}
