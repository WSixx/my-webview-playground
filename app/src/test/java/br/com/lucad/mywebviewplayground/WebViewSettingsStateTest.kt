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
