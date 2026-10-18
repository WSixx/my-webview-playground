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
import android.view.View
import android.webkit.WebView
import br.com.lucad.mywebviewplayground.bridge.NativeBridge
import br.com.lucad.mywebviewplayground.model.WebViewSettingsState

/**
 * Manager responsible for applying [WebViewSettingsState] configurations and binding JS interfaces to WebView instances.
 */
object WebViewSettingsManager {

    @Suppress("DEPRECATION")
    @SuppressLint("SetJavaScriptEnabled")
    fun applySettings(
        webView: WebView,
        settingsState: WebViewSettingsState,
    ) {
        webView.settings.apply {
            javaScriptEnabled = settingsState.javaScriptEnabled
            domStorageEnabled = settingsState.domStorageEnabled
            cacheMode = settingsState.cacheMode
            allowFileAccess = settingsState.allowFileAccess
            allowContentAccess = settingsState.allowContentAccess
            databaseEnabled = settingsState.databaseEnabled
            useWideViewPort = settingsState.useWideViewPort
            loadWithOverviewMode = settingsState.loadWithOverviewMode
            setSupportZoom(settingsState.supportZoom)
            builtInZoomControls = settingsState.builtInZoomControls
            displayZoomControls = settingsState.displayZoomControls
            mediaPlaybackRequiresUserGesture = settingsState.mediaPlaybackRequiresUserGesture
            settingsState.userAgent?.let { userAgentString = it }
        }

        if (settingsState.hardwareAccelerationEnabled) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
    }

    /**
     * Binds NativeBridge instance under "NativeBridge" and "AndroidBridge" for web compatibility.
     */
    @SuppressLint("JavascriptInterface")
    fun attachBridge(
        webView: WebView,
        nativeBridge: NativeBridge,
    ) {
        webView.addJavascriptInterface(nativeBridge, NativeBridge.BRIDGE_NAME)
        webView.addJavascriptInterface(nativeBridge, NativeBridge.LEGACY_BRIDGE_NAME)
    }
}
