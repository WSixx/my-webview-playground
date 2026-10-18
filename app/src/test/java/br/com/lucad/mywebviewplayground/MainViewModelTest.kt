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
import br.com.lucad.mywebviewplayground.bridge.NativeBridge
import br.com.lucad.mywebviewplayground.model.BenchmarkMetrics
import br.com.lucad.mywebviewplayground.model.WebViewSettingsState
import br.com.lucad.mywebviewplayground.viewmodel.DEFAULT_MOCK_URL
import br.com.lucad.mywebviewplayground.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var nativeBridge: NativeBridge
    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        nativeBridge = NativeBridge()
        viewModel = MainViewModel(nativeBridge = nativeBridge)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialUrlState() {
        assertEquals(DEFAULT_MOCK_URL, viewModel.urlInput.value)
        assertEquals(DEFAULT_MOCK_URL, viewModel.currentUrl.value)
    }

    @Test
    fun testLoadUrlFormatting() {
        viewModel.onUrlInputChanged("example.com")
        viewModel.loadUrl()

        assertEquals("https://example.com", viewModel.currentUrl.value)
        assertEquals("https://example.com", viewModel.urlInput.value)
        assertTrue(viewModel.isLoading.value)
    }

    @Test
    fun testLoadMockAssetUrl() {
        viewModel.onUrlInputChanged("https://google.com")
        viewModel.loadMockAssetUrl()

        assertEquals(DEFAULT_MOCK_URL, viewModel.currentUrl.value)
        assertEquals(DEFAULT_MOCK_URL, viewModel.urlInput.value)
    }

    @Test
    fun testPageLifecycleProgress() {
        viewModel.onPageStarted("https://example.com")
        assertTrue(viewModel.isLoading.value)
        assertEquals(10, viewModel.loadingProgress.value)

        viewModel.onProgressChanged(50)
        assertTrue(viewModel.isLoading.value)
        assertEquals(50, viewModel.loadingProgress.value)

        viewModel.onProgressChanged(100)
        assertFalse(viewModel.isLoading.value)
        assertEquals(100, viewModel.loadingProgress.value)
    }

    @Test
    fun testUpdateSettingsState() {
        val newSettings = WebViewSettingsState(
            javaScriptEnabled = false,
            cacheMode = WebSettings.LOAD_NO_CACHE
        )
        viewModel.updateSettings(newSettings)

        assertEquals(newSettings, viewModel.settingsState.value)
        assertFalse(viewModel.settingsState.value.javaScriptEnabled)
        assertEquals(WebSettings.LOAD_NO_CACHE, viewModel.settingsState.value.cacheMode)
    }

    @Test
    fun testClearLogs() {
        nativeBridge.log("Test log entry")
        assertEquals(1, nativeBridge.logs.value.size)

        viewModel.clearLogs()
        assertTrue(nativeBridge.logs.value.isEmpty())
    }

    @Test
    fun testOpenWebViewClickedRecordsNavigationTimestamp() {
        viewModel.onOpenWebViewClicked()

        val metrics = viewModel.metrics.value
        assertTrue(metrics.navigationClickTimeMs > 0)
        assertFalse(metrics.coldStartRecorded)
    }

    @Test
    fun testColdStartLifecycleRecording() {
        viewModel.onOpenWebViewClicked()

        Thread.sleep(10)
        viewModel.onPageStarted(DEFAULT_MOCK_URL)
        val metricsAfterStart = viewModel.metrics.value
        assertTrue(metricsAfterStart.timeToPageStartedMs >= 0)

        Thread.sleep(10)
        viewModel.onPageFinished(DEFAULT_MOCK_URL, BenchmarkMetrics())
        val metricsAfterFinish = viewModel.metrics.value
        assertTrue(metricsAfterFinish.timeToPageFinishedMs > 0)
        assertTrue(metricsAfterFinish.coldStartRecorded)
    }
}
