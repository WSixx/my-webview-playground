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

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.JavaScriptReplyProxy
import androidx.webkit.WebMessageCompat
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature

/**
 * High-performance FastWebView subclass utilizing Chromium Mojo IPC via [WebViewCompat.addWebMessageListener]
 * and [JavaScriptReplyProxy] for zero-eval bidirectional native <-> JS communication.
 */
class FastWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    private var replyProxy: JavaScriptReplyProxy? = null

    var isHandshakeReady: Boolean = false
        private set

    private val pendingMessageQueue = ArrayDeque<String>()
    private val pendingBinaryQueue = ArrayDeque<ByteArray>()

    var onNativeMessageReceived: ((String) -> Unit)? = null
    var onBinaryMessageReceived: ((ByteArray) -> Unit)? = null
    var onHandshakeCompleted: (() -> Unit)? = null

    init {
        configureSettings()
        setupAssetLoaderAndClient()
        setupMojoIpcBridge()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureSettings() {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
        }
    }

    private fun setupAssetLoaderAndClient() {
        val assetLoader = WebViewAssetLoader.Builder()
            .setDomain("appassets.androidplatform.net")
            .addPathHandler("/assets/", RamCachedAssetHandler(context))
            .build()

        webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                return request?.url?.let { assetLoader.shouldInterceptRequest(it) }
                    ?: super.shouldInterceptRequest(view, request)
            }
        }
    }

    private fun setupMojoIpcBridge() {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            WebViewCompat.addWebMessageListener(
                this,
                FAST_BRIDGE_NAME,
                setOf("*")
            ) { _, message, _, _, proxy ->
                this.replyProxy = proxy
                when (message.type) {
                    WebMessageCompat.TYPE_STRING -> {
                        val strData = message.data ?: ""
                        if (strData.contains("__HANDSHAKE_READY__")) {
                            isHandshakeReady = true
                            flushPendingQueues()
                            onHandshakeCompleted?.invoke()
                        }
                        onNativeMessageReceived?.invoke(strData)
                    }
                    WebMessageCompat.TYPE_ARRAY_BUFFER -> {
                        val byteData = message.arrayBuffer
                        onBinaryMessageReceived?.invoke(byteData)
                    }
                }
            }
        }
    }

    /**
     * Posts a message directly over Chromium Mojo IPC using [JavaScriptReplyProxy].
     * Bypasses evaluateJavascript entirely for zero-eval execution.
     */
    fun sendDirectMessage(message: String) {
        val proxy = replyProxy
        if (proxy != null && isHandshakeReady && WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            proxy.postMessage(message)
        } else {
            pendingMessageQueue.addLast(message)
        }
    }

    /**
     * Posts raw binary bytes over Mojo IPC directly to JavaScript as ArrayBuffer.
     */
    fun sendDirectBinary(data: ByteArray) {
        val proxy = replyProxy
        if (proxy != null && isHandshakeReady && WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_ARRAY_BUFFER)) {
            proxy.postMessage(data)
        } else {
            pendingBinaryQueue.addLast(data)
        }
    }

    private fun flushPendingQueues() {
        val proxy = replyProxy ?: return
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) return

        while (pendingMessageQueue.isNotEmpty()) {
            val msg = pendingMessageQueue.removeFirst()
            proxy.postMessage(msg)
        }

        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_ARRAY_BUFFER)) {
            while (pendingBinaryQueue.isNotEmpty()) {
                val bin = pendingBinaryQueue.removeFirst()
                proxy.postMessage(bin)
            }
        }
    }

    /**
     * Mounts a WebComponent inside the shell container with initial props.
     */
    fun mountComponent(tag: String, propsJson: String) {
        sendDirectMessage("""{"type":"__MOUNT_COMPONENT__","tag":"$tag","props":$propsJson}""")
    }

    /**
     * Updates props on the currently mounted WebComponent.
     */
    fun updateProps(propsJson: String) {
        sendDirectMessage("""{"type":"__UPDATE_PROPS__","props":$propsJson}""")
    }

    /**
     * Resets active mounted components and clears callbacks to prepare instance for pool reuse.
     */
    fun resetForPool() {
        if (isHandshakeReady) {
            sendDirectMessage("""{"type":"__RESET_COMPONENT__"}""")
        }
        onNativeMessageReceived = null
        onBinaryMessageReceived = null
        onHandshakeCompleted = null
        pendingMessageQueue.clear()
        pendingBinaryQueue.clear()
    }

    companion object {
        const val FAST_BRIDGE_NAME = "NativeFastBridge"
        const val SHELL_URL = "https://appassets.androidplatform.net/assets/shell.html"
    }
}
