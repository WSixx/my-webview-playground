package br.com.lucad.mywebviewplayground

import android.webkit.WebSettings
import br.com.lucad.mywebviewplayground.model.BenchmarkMetrics
import br.com.lucad.mywebviewplayground.model.WebViewSettingsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewSettingsStateTest {

    @Test
    fun testDefaultSettingsStateValues() {
        val settings = WebViewSettingsState.Default

        assertTrue(settings.javaScriptEnabled)
        assertTrue(settings.domStorageEnabled)
        assertEquals(WebSettings.LOAD_DEFAULT, settings.cacheMode)
        assertTrue(settings.hardwareAccelerationEnabled)
        assertTrue(settings.allowFileAccess)
        assertTrue(settings.allowContentAccess)
    }

    @Test
    fun testHighPerformanceSettingsStateValues() {
        val settings = WebViewSettingsState.HighPerformance

        assertTrue(settings.javaScriptEnabled)
        assertTrue(settings.domStorageEnabled)
        assertEquals(WebSettings.LOAD_NO_CACHE, settings.cacheMode)
        assertTrue(settings.hardwareAccelerationEnabled)
    }

    @Test
    fun testBenchmarkMetricsDefaults() {
        val metrics = BenchmarkMetrics()

        assertEquals(0L, metrics.loadStartTimeMs)
        assertEquals(0L, metrics.domContentLoadedTimeMs)
        assertEquals(0L, metrics.pageLoadFinishedTimeMs)
        assertEquals(0L, metrics.totalLoadTimeMs)
        assertEquals(0L, metrics.jsBridgeMessageCount)
    }
}
