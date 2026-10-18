/*
 * Copyright (C) 2025 Lucas S Goncalves
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.lucasgoncalves.fastwebview

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
