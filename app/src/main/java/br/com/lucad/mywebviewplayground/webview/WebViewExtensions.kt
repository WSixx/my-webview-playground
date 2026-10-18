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

import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import br.com.lucad.mywebviewplayground.bridge.NativeBridge
import org.json.JSONObject

/**
 * Extension functions for posting messages and evaluating JavaScript safely.
 */

fun WebView.postMessageToWeb(
    action: String,
    payload: Any? = null,
    nativeBridge: NativeBridge? = null,
    callback: ((String?) -> Unit)? = null,
) {
    val jsonMsg = JSONObject().apply {
        put("action", action)
        payload?.let { put("payload", it) }
        put("timestamp", System.currentTimeMillis())
    }.toString()

    postRawMessageToWeb(jsonMsg, nativeBridge, callback)
}

fun WebView.postRawMessageToWeb(
    jsonOrMessage: String,
    nativeBridge: NativeBridge? = null,
    callback: ((String?) -> Unit)? = null,
) {
    val escapedMsg = JSONObject.quote(jsonOrMessage)
    val script = """
        (function() {
            if (typeof window.postMessageToWeb === 'function') {
                window.postMessageToWeb($escapedMsg);
            } else if (window.NativeBridge && typeof window.NativeBridge.onMessageFromNative === 'function') {
                window.NativeBridge.onMessageFromNative($escapedMsg);
            } else if (window.AndroidBridge && typeof window.AndroidBridge.onMessageFromNative === 'function') {
                window.AndroidBridge.onMessageFromNative($escapedMsg);
            } else {
                console.log("No recipient target for native message:", $escapedMsg);
            }
        })();
    """.trimIndent()

    nativeBridge?.logOutgoingMessage(jsonOrMessage, "POST_TO_WEB")
    evaluateJavascriptSafe(script, callback)
}

fun WebView.evaluateJavascriptSafe(
    script: String,
    callback: ((String?) -> Unit)? = null,
) {
    val runEvaluation = {
        this.evaluateJavascript(script) { result ->
            callback?.invoke(result)
        }
    }

    if (Looper.myLooper() == Looper.getMainLooper()) {
        runEvaluation()
    } else {
        Handler(Looper.getMainLooper()).post {
            runEvaluation()
        }
    }
}
