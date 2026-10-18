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
