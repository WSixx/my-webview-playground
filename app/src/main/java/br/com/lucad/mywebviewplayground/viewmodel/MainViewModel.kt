package br.com.lucad.mywebviewplayground.viewmodel

import android.content.Context
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.lucad.mywebviewplayground.bridge.NativeBridge
import br.com.lucad.mywebviewplayground.model.BenchmarkMetrics
import br.com.lucad.mywebviewplayground.model.BridgeDirection
import br.com.lucad.mywebviewplayground.model.BridgeLogMessage
import br.com.lucad.mywebviewplayground.model.WebViewSettingsState
import br.com.lucad.mywebviewplayground.webview.WebViewPrewarmer
import br.com.lucad.mywebviewplayground.webview.postRawMessageToWeb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class LogFilter(val label: String) {
    ALL("All"),
    INCOMING("Incoming"),
    OUTGOING("Outgoing"),
    SYSTEM("System"),
    CONSOLE("Console")
}

const val DEFAULT_MOCK_URL = "file:///android_asset/mock_webcomponent.html"

class MainViewModel(
    val nativeBridge: NativeBridge = NativeBridge()
) : ViewModel() {

    private val _urlInput = MutableStateFlow(DEFAULT_MOCK_URL)
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    private val _currentUrl = MutableStateFlow(DEFAULT_MOCK_URL)
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _pageTitle = MutableStateFlow<String?>("Mock WebComponent")
    val pageTitle: StateFlow<String?> = _pageTitle.asStateFlow()

    private val _loadingProgress = MutableStateFlow(0)
    val loadingProgress: StateFlow<Int> = _loadingProgress.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _settingsState = MutableStateFlow(WebViewSettingsState.Default)
    val settingsState: StateFlow<WebViewSettingsState> = _settingsState.asStateFlow()

    private val _logFilter = MutableStateFlow(LogFilter.ALL)
    val logFilter: StateFlow<LogFilter> = _logFilter.asStateFlow()

    private val _isPrewarmEnabled = MutableStateFlow(false)
    val isPrewarmEnabled: StateFlow<Boolean> = _isPrewarmEnabled.asStateFlow()

    private val _isWebViewPrewarmed = MutableStateFlow(false)
    val isWebViewPrewarmed: StateFlow<Boolean> = _isWebViewPrewarmed.asStateFlow()

    val metrics: StateFlow<BenchmarkMetrics> = nativeBridge.metrics

    val filteredLogs: StateFlow<List<BridgeLogMessage>> = combine(
        nativeBridge.logs,
        _logFilter
    ) { logs, filter ->
        when (filter) {
            LogFilter.ALL -> logs
            LogFilter.INCOMING -> logs.filter { it.direction == BridgeDirection.INCOMING }
            LogFilter.OUTGOING -> logs.filter { it.direction == BridgeDirection.OUTGOING }
            LogFilter.SYSTEM -> logs.filter { it.direction == BridgeDirection.SYSTEM }
            LogFilter.CONSOLE -> logs.filter { it.direction == BridgeDirection.WEB_CONSOLE }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun togglePrewarm(enabled: Boolean, context: Context) {
        _isPrewarmEnabled.value = enabled
        if (enabled) {
            WebViewPrewarmer.prewarm(context, _settingsState.value, nativeBridge)
            _isWebViewPrewarmed.value = WebViewPrewarmer.isPrewarmedAndReady
        } else {
            WebViewPrewarmer.release(nativeBridge)
            _isWebViewPrewarmed.value = false
        }
    }

    fun onOpenWebViewClicked() {
        val clickTime = System.currentTimeMillis()
        val wasPrewarmed = _isPrewarmEnabled.value && WebViewPrewarmer.isPrewarmedAndReady

        nativeBridge.updateMetrics { current ->
            current.copy(
                navigationClickTimeMs = clickTime,
                isPrewarmed = wasPrewarmed,
                webViewCreatedTimeMs = 0L,
                timeToWebViewCreatedMs = 0L,
                timeToPageStartedMs = 0L,
                timeToPageFinishedMs = 0L,
                coldStartRecorded = false
            )
        }
        nativeBridge.logSystemEvent("Navigation to WebView Playground triggered. Pre-warmed: $wasPrewarmed")
    }

    fun onWebViewCreated(webView: WebView) {
        val createdTime = System.currentTimeMillis()
        val clickTime = nativeBridge.metrics.value.navigationClickTimeMs

        if (clickTime > 0 && nativeBridge.metrics.value.webViewCreatedTimeMs == 0L) {
            val duration = createdTime - clickTime
            nativeBridge.updateMetrics { current ->
                current.copy(
                    webViewCreatedTimeMs = createdTime,
                    timeToWebViewCreatedMs = duration
                )
            }
            nativeBridge.logSystemEvent("WebView instance created in ${duration}ms from click")
        }
    }

    fun onUrlInputChanged(newUrl: String) {
        _urlInput.value = newUrl
    }

    fun loadUrl(url: String? = null) {
        val targetUrl = (url ?: _urlInput.value).trim()
        if (targetUrl.isNotEmpty()) {
            val formattedUrl = if (!targetUrl.startsWith("http://") &&
                !targetUrl.startsWith("https://") &&
                !targetUrl.startsWith("file://")
            ) {
                "https://$targetUrl"
            } else {
                targetUrl
            }
            _urlInput.value = formattedUrl
            _currentUrl.value = formattedUrl
            _isLoading.value = true
            _loadingProgress.value = 0
        }
    }

    fun loadMockAssetUrl() {
        loadUrl(DEFAULT_MOCK_URL)
    }

    fun onPageStarted(url: String?) {
        _isLoading.value = true
        _loadingProgress.value = 10
        val startedTime = System.currentTimeMillis()
        val clickTime = nativeBridge.metrics.value.navigationClickTimeMs

        if (!url.isNullOrEmpty()) {
            _currentUrl.value = url
            _urlInput.value = url
        }

        if (clickTime > 0) {
            val duration = startedTime - clickTime
            nativeBridge.updateMetrics { current ->
                current.copy(timeToPageStartedMs = duration)
            }
        }
    }

    fun onPageFinished(url: String?, metrics: BenchmarkMetrics) {
        _isLoading.value = false
        _loadingProgress.value = 100
        val finishedTime = System.currentTimeMillis()
        val clickTime = nativeBridge.metrics.value.navigationClickTimeMs

        if (clickTime > 0) {
            val duration = finishedTime - clickTime
            nativeBridge.updateMetrics { current ->
                current.copy(
                    timeToPageFinishedMs = duration,
                    coldStartRecorded = true
                )
            }
            nativeBridge.logSystemEvent("WebView Cold Start cycle complete. Total Latency: ${duration}ms")
        }
    }

    fun onProgressChanged(progress: Int) {
        _loadingProgress.value = progress
        if (progress >= 100) {
            _isLoading.value = false
        } else if (progress > 0) {
            _isLoading.value = true
        }
    }

    fun onReceivedTitle(title: String?) {
        _pageTitle.value = title
    }

    fun updateSettings(newSettings: WebViewSettingsState) {
        _settingsState.value = newSettings
        nativeBridge.logSystemEvent("WebView Settings updated: $newSettings")
    }

    fun setLogFilter(filter: LogFilter) {
        _logFilter.value = filter
    }

    fun clearLogs() {
        nativeBridge.clearLogs()
    }

    fun sendMessageToWeb(webView: WebView?, jsonOrMessage: String) {
        if (jsonOrMessage.isNotBlank() && webView != null) {
            webView.postRawMessageToWeb(jsonOrMessage, nativeBridge)
        }
    }

    fun clearCache(webView: WebView?) {
        webView?.clearCache(true)
        nativeBridge.logSystemEvent("WebView cache cleared successfully")
    }

    fun resetColdStartAndProcess(context: Context, activeWebView: WebView? = null) {
        activeWebView?.clearCache(true)
        WebViewPrewarmer.release(nativeBridge)
        _isPrewarmEnabled.value = false
        _isWebViewPrewarmed.value = false

        nativeBridge.updateMetrics { BenchmarkMetrics() }
        nativeBridge.clearLogs()
        nativeBridge.logSystemEvent("Process / Cold Start benchmark state reset successfully")

        System.gc()
    }
}
