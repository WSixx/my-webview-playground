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
