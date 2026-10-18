package br.com.lucad.mywebviewplayground.fastwebview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Jetpack Compose wrapper for [FastWebView] that acquires an instance from [WebViewWarmupPool],
 * binds callbacks, mounts components, and handles lifecycle cleanup on dispose.
 */
@Composable
fun FastWebViewComposable(
    modifier: Modifier = Modifier,
    componentTag: String = "checkout-widget",
    propsJson: String = "{}",
    onMessageReceived: ((String) -> Unit)? = null,
    onBinaryReceived: ((ByteArray) -> Unit)? = null,
    onFastWebViewCreated: ((FastWebView) -> Unit)? = null
) {
    var fastWebViewInstance by remember { mutableStateOf<FastWebView?>(null) }

    AndroidView(
        factory = { ctx ->
            val fastWebView = WebViewWarmupPool.acquireFastWebView(ctx)
            fastWebView.onNativeMessageReceived = onMessageReceived
            fastWebView.onBinaryMessageReceived = onBinaryReceived
            onFastWebViewCreated?.invoke(fastWebView)
            fastWebViewInstance = fastWebView
            fastWebView
        },
        update = { webView ->
            webView.onNativeMessageReceived = onMessageReceived
            webView.onBinaryMessageReceived = onBinaryReceived
        },
        modifier = modifier
    )

    DisposableEffect(Unit) {
        onDispose {
            fastWebViewInstance?.let { webView ->
                WebViewWarmupPool.releaseFastWebView(webView)
            }
            fastWebViewInstance = null
        }
    }

    LaunchedEffect(componentTag, propsJson, fastWebViewInstance) {
        val webView = fastWebViewInstance ?: return@LaunchedEffect
        webView.mountComponent(componentTag, propsJson)
    }
}
