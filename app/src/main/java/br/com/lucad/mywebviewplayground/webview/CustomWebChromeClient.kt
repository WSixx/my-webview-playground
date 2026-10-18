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

import android.graphics.Bitmap
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import br.com.lucad.mywebviewplayground.bridge.NativeBridge

/**
 * Custom WebChromeClient for Web UI events, progress updates, and JS console message capturing.
 */
class CustomWebChromeClient(
    private val nativeBridge: NativeBridge? = null,
    var onProgressChangedCallback: ((progress: Int) -> Unit)? = null,
    var onConsoleMessageCallback: ((message: ConsoleMessage) -> Unit)? = null,
    var onReceivedTitleCallback: ((title: String?) -> Unit)? = null,
    var onReceivedIconCallback: ((icon: Bitmap?) -> Unit)? = null,
) : WebChromeClient() {

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        onProgressChangedCallback?.invoke(newProgress)
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        if (consoleMessage != null) {
            val levelName = consoleMessage.messageLevel().name
            val msgText = "${consoleMessage.message()} (${consoleMessage.sourceId()}:${consoleMessage.lineNumber()})"

            nativeBridge?.logConsoleMessage(msgText, levelName)
            onConsoleMessageCallback?.invoke(consoleMessage)
        }
        return super.onConsoleMessage(consoleMessage)
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        onReceivedTitleCallback?.invoke(title)
    }

    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
        super.onReceivedIcon(view, icon)
        onReceivedIconCallback?.invoke(icon)
    }
}
