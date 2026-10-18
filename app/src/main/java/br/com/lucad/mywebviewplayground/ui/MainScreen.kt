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

package br.com.lucad.mywebviewplayground.ui

import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.lucad.mywebviewplayground.bridge.NativeBridge
import br.com.lucad.mywebviewplayground.model.BenchmarkMetrics
import br.com.lucad.mywebviewplayground.model.BridgeDirection
import br.com.lucad.mywebviewplayground.model.BridgeLogMessage
import br.com.lucad.mywebviewplayground.model.WebViewSettingsState
import br.com.lucad.mywebviewplayground.ui.components.BenchmarkOverlayCard
import br.com.lucad.mywebviewplayground.ui.components.JsBridgeConsolePanel
import br.com.lucad.mywebviewplayground.ui.components.UrlLauncherBar
import br.com.lucad.mywebviewplayground.ui.components.WebViewConfigPanel
import br.com.lucad.mywebviewplayground.ui.components.WebViewViewport
import br.com.lucad.mywebviewplayground.ui.theme.MyWebviewPlaygroundTheme
import br.com.lucad.mywebviewplayground.viewmodel.LogFilter
import br.com.lucad.mywebviewplayground.viewmodel.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
    onBackClick: (() -> Unit)? = null
) {
    val urlInput by viewModel.urlInput.collectAsStateWithLifecycle()
    val currentUrl by viewModel.currentUrl.collectAsStateWithLifecycle()
    val pageTitle by viewModel.pageTitle.collectAsStateWithLifecycle()
    val loadingProgress by viewModel.loadingProgress.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()
    val metrics by viewModel.metrics.collectAsStateWithLifecycle()
    val logs by viewModel.filteredLogs.collectAsStateWithLifecycle()
    val logFilter by viewModel.logFilter.collectAsStateWithLifecycle()

    var activeWebView by remember { mutableStateOf<WebView?>(null) }

    MainScreenContent(
        urlInput = urlInput,
        currentUrl = currentUrl,
        pageTitle = pageTitle,
        loadingProgress = loadingProgress,
        isLoading = isLoading,
        settingsState = settingsState,
        metrics = metrics,
        logs = logs,
        logFilter = logFilter,
        nativeBridge = viewModel.nativeBridge,
        onUrlInputChange = viewModel::onUrlInputChanged,
        onGoClick = { viewModel.loadUrl() },
        onLoadMockClick = { viewModel.loadMockAssetUrl() },
        onReloadClick = { activeWebView?.reload() },
        onClearCacheClick = { viewModel.clearCache(activeWebView) },
        onSettingsChange = viewModel::updateSettings,
        onFilterChange = viewModel::setLogFilter,
        onSendMessage = { msg -> viewModel.sendMessageToWeb(activeWebView, msg) },
        onClearLogs = viewModel::clearLogs,
        onPageStarted = viewModel::onPageStarted,
        onPageFinished = viewModel::onPageFinished,
        onProgressChanged = viewModel::onProgressChanged,
        onReceivedTitle = viewModel::onReceivedTitle,
        onWebViewCreated = { webView ->
            activeWebView = webView
            viewModel.onWebViewCreated(webView)
        },
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    urlInput: String,
    currentUrl: String,
    pageTitle: String?,
    loadingProgress: Int,
    isLoading: Boolean,
    settingsState: WebViewSettingsState,
    metrics: BenchmarkMetrics,
    logs: List<BridgeLogMessage>,
    logFilter: LogFilter,
    nativeBridge: NativeBridge,
    onUrlInputChange: (String) -> Unit,
    onGoClick: () -> Unit,
    onLoadMockClick: () -> Unit,
    onReloadClick: () -> Unit,
    onClearCacheClick: () -> Unit,
    onSettingsChange: (WebViewSettingsState) -> Unit,
    onFilterChange: (LogFilter) -> Unit,
    onSendMessage: (String) -> Unit,
    onClearLogs: () -> Unit,
    onPageStarted: (String?) -> Unit,
    onPageFinished: (String?, BenchmarkMetrics) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onReceivedTitle: (String?) -> Unit,
    onWebViewCreated: (WebView) -> Unit,
    onBackClick: (() -> Unit)? = null
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var bottomPanelExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            UrlLauncherBar(
                urlInput = urlInput,
                pageTitle = pageTitle,
                onUrlInputChange = onUrlInputChange,
                onGoClick = onGoClick,
                onLoadMockClick = onLoadMockClick,
                onReloadClick = onReloadClick,
                onClearCacheClick = onClearCacheClick,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main WebView Viewport
            WebViewViewport(
                currentUrl = currentUrl,
                settingsState = settingsState,
                nativeBridge = nativeBridge,
                isLoading = isLoading,
                loadingProgress = loadingProgress,
                onPageStarted = onPageStarted,
                onPageFinished = onPageFinished,
                onProgressChanged = onProgressChanged,
                onReceivedTitle = onReceivedTitle,
                onWebViewCreated = onWebViewCreated,
                modifier = Modifier.fillMaxSize()
            )

            // Real-Time Benchmark Overlay Card at top
            BenchmarkOverlayCard(
                metrics = metrics,
                loadingProgress = loadingProgress,
                isLoading = isLoading,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                initialExpanded = true
            )

            // Bottom Tools Drawer / Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header Bar with PrimaryTabRow and Expand/Collapse Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PrimaryTabRow(
                            selectedTabIndex = selectedTab,
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = {
                                    selectedTab = 0
                                    bottomPanelExpanded = true
                                },
                                text = { Text("JS Console", fontSize = 12.sp) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Terminal,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )

                            Tab(
                                selected = selectedTab == 1,
                                onClick = {
                                    selectedTab = 1
                                    bottomPanelExpanded = true
                                },
                                text = { Text("Settings", fontSize = 12.sp) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }

                        IconButton(
                            onClick = { bottomPanelExpanded = !bottomPanelExpanded },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (bottomPanelExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                contentDescription = if (bottomPanelExpanded) "Collapse Panel" else "Expand Panel"
                            )
                        }
                    }

                    // Panel Content (Expandable)
                    AnimatedVisibility(
                        visible = bottomPanelExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                        ) {
                            when (selectedTab) {
                                0 -> JsBridgeConsolePanel(
                                    logs = logs,
                                    activeFilter = logFilter,
                                    onFilterChange = onFilterChange,
                                    onSendMessage = onSendMessage,
                                    onClearLogs = onClearLogs
                                )
                                1 -> WebViewConfigPanel(
                                    settingsState = settingsState,
                                    onSettingsChange = onSettingsChange
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MyWebviewPlaygroundTheme {
        MainScreenContent(
            urlInput = "file:///android_asset/mock_webcomponent.html",
            currentUrl = "file:///android_asset/mock_webcomponent.html",
            pageTitle = "Mock WebComponent Playground",
            loadingProgress = 100,
            isLoading = false,
            settingsState = WebViewSettingsState.Default,
            metrics = BenchmarkMetrics(
                totalLoadTimeMs = 280,
                domContentLoadedTimeMs = 95,
                usedMemoryMb = 38.2,
                totalMemoryMb = 256.0,
                jsBridgeMessageCount = 8
            ),
            logs = listOf(
                BridgeLogMessage(
                    direction = BridgeDirection.INCOMING,
                    payload = """{"type": "COUNTER_CHANGED", "count": 1}""",
                    type = "COUNTER_CHANGED"
                )
            ),
            logFilter = LogFilter.ALL,
            nativeBridge = NativeBridge(),
            onUrlInputChange = {},
            onGoClick = {},
            onLoadMockClick = {},
            onReloadClick = {},
            onClearCacheClick = {},
            onSettingsChange = {},
            onFilterChange = {},
            onSendMessage = {},
            onClearLogs = {},
            onPageStarted = {},
            onPageFinished = { _, _ -> },
            onProgressChanged = {},
            onReceivedTitle = {},
            onWebViewCreated = {},
            onBackClick = {}
        )
    }
}
