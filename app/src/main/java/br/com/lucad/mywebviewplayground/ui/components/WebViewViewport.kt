package br.com.lucad.mywebviewplayground.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import br.com.lucad.mywebviewplayground.bridge.NativeBridge
import br.com.lucad.mywebviewplayground.model.BenchmarkMetrics
import br.com.lucad.mywebviewplayground.model.WebViewSettingsState
import br.com.lucad.mywebviewplayground.webview.CustomWebChromeClient
import br.com.lucad.mywebviewplayground.webview.CustomWebViewClient
import br.com.lucad.mywebviewplayground.webview.WebViewPrewarmer
import br.com.lucad.mywebviewplayground.webview.WebViewSettingsManager

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewViewport(
    currentUrl: String,
    settingsState: WebViewSettingsState,
    nativeBridge: NativeBridge,
    isLoading: Boolean,
    loadingProgress: Int,
    onPageStarted: (String?) -> Unit,
    onPageFinished: (String?, BenchmarkMetrics) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onReceivedTitle: (String?) -> Unit,
    onWebViewCreated: (WebView) -> Unit,
    modifier: Modifier = Modifier
) {
    val webViewClient = remember(nativeBridge) {
        CustomWebViewClient(
            nativeBridge = nativeBridge,
            onPageStartedCallback = onPageStarted,
            onPageFinishedCallback = onPageFinished
        )
    }

    val webChromeClient = remember(nativeBridge) {
        CustomWebChromeClient(
            nativeBridge = nativeBridge,
            onProgressChangedCallback = onProgressChanged,
            onReceivedTitleCallback = onReceivedTitle
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                val prewarmed = WebViewPrewarmer.consumePrewarmedWebView()
                val webView = prewarmed ?: WebView(context)

                webView.apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    this.webViewClient = webViewClient
                    this.webChromeClient = webChromeClient

                    WebViewSettingsManager.attachBridge(this, nativeBridge)
                    WebViewSettingsManager.applySettings(this, settingsState)

                    onWebViewCreated(this)
                    if (currentUrl.isNotBlank()) {
                        loadUrl(currentUrl)
                    }
                }
            },
            update = { webView ->
                WebViewSettingsManager.applySettings(webView, settingsState)

                if (currentUrl.isNotBlank() && webView.url != currentUrl) {
                    webView.loadUrl(currentUrl)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading && loadingProgress in 1..99) {
            LinearProgressIndicator(
                progress = { loadingProgress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
