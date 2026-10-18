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
