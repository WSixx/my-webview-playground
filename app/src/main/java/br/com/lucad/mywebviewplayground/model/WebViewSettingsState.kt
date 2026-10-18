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
