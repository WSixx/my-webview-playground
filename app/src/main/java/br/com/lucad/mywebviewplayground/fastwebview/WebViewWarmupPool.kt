package br.com.lucad.mywebviewplayground.fastwebview

import android.content.Context
import android.content.MutableContextWrapper
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import androidx.webkit.WebViewOutcomeReceiver
import androidx.webkit.WebViewStartUpConfig
import androidx.webkit.WebViewStartUpResult
import androidx.webkit.WebViewStartupException
import java.util.concurrent.Executors

/**
 * Singleton pool manager pre-allocating and warming up [FastWebView] instances.
 * Leverages [MutableContextWrapper] to allow context re-parenting without memory leaks
 * and [WebViewStartUpConfig] via [WebViewCompat.startUpWebView] for background engine initialization.
 */
object WebViewWarmupPool {

    private var prewarmedFastWebView: FastWebView? = null
    private var wrapperContext: MutableContextWrapper? = null

    var isEngineWarm: Boolean = false
        private set

    /**
     * Pre-warms Chromium rendering engine and pre-allocates a [FastWebView] instance.
     * Loads `shell.html` into memory and primes Mojo IPC ahead of UI rendering.
     */
    @OptIn(WebViewCompat.ExperimentalAsyncStartUp::class)
    fun warmUpEngineAndPool(context: Context) {
        if (prewarmedFastWebView != null) return

        val appContext = context.applicationContext

        // Preload assets in RAM
        RamCachedAssetHandler.preloadAssets(appContext)

        // Asynchronously initialize WebView engine if startup feature is supported
        try {
            if (WebViewFeature.isStartupFeatureSupported(appContext, WebViewFeature.STARTUP_FEATURE_SET_DATA_DIRECTORY_SUFFIX)) {
                val executor = Executors.newSingleThreadExecutor()
                val config = WebViewStartUpConfig.Builder(executor).build()
                WebViewCompat.startUpWebView(
                    appContext,
                    config,
                    object : WebViewOutcomeReceiver<WebViewStartUpResult, WebViewStartupException> {
                        override fun onResult(result: WebViewStartUpResult) {
                            isEngineWarm = true
                        }

                        override fun onError(error: WebViewStartupException) {
                            isEngineWarm = false
                        }
                    }
                )
            }
        } catch (_: Exception) {
            // Optional async startup API fallback
        }

        // Pre-instantiate FastWebView with MutableContextWrapper
        try {
            val wrapper = MutableContextWrapper(appContext)
            wrapperContext = wrapper
            val webView = FastWebView(wrapper)
            webView.loadUrl(FastWebView.SHELL_URL)
            prewarmedFastWebView = webView
            isEngineWarm = true
        } catch (_: Exception) {
            isEngineWarm = false
        }
    }

    /**
     * Acquires a pre-warmed [FastWebView] from the pool, updating its [MutableContextWrapper]
     * to the Activity context.
     */
    fun acquireFastWebView(activityContext: Context): FastWebView {
        val existing = prewarmedFastWebView
        val wrapper = wrapperContext

        if (existing != null && wrapper != null) {
            prewarmedFastWebView = null
            wrapperContext = null
            wrapper.baseContext = activityContext
            return existing
        }

        // Fallback: create fresh FastWebView if pool is empty
        val newWrapper = MutableContextWrapper(activityContext)
        return FastWebView(newWrapper).apply {
            loadUrl(FastWebView.SHELL_URL)
        }
    }

    /**
     * Resets a [FastWebView] instance and returns it to the pool for future reuse.
     */
    fun releaseFastWebView(webView: FastWebView) {
        webView.resetForPool()

        if (prewarmedFastWebView == null) {
            val wrapper = webView.context as? MutableContextWrapper
            wrapper?.baseContext = webView.context.applicationContext
            prewarmedFastWebView = webView
            wrapperContext = wrapper
        } else {
            try {
                webView.stopLoading()
                webView.removeAllViews()
                webView.destroy()
            } catch (_: Exception) {}
        }
    }

    /**
     * Clears all pre-allocated instances in the pool.
     */
    fun clearPool() {
        prewarmedFastWebView?.let {
            try {
                it.stopLoading()
                it.removeAllViews()
                it.destroy()
            } catch (_: Exception) {}
        }
        prewarmedFastWebView = null
        wrapperContext = null
        isEngineWarm = false
    }
}
