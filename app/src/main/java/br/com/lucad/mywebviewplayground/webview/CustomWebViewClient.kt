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
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import br.com.lucad.mywebviewplayground.bridge.NativeBridge
import br.com.lucad.mywebviewplayground.model.BenchmarkMetrics
import kotlin.math.roundToLong

/**
 * Custom WebViewClient tracking page load timing, DOM metrics, and error handling.
 */
class CustomWebViewClient(
    private val nativeBridge: NativeBridge? = null,
    private val injectBenchmarkingScript: Boolean = true,
    var onPageStartedCallback: ((url: String?) -> Unit)? = null,
    var onPageFinishedCallback: ((url: String?, metrics: BenchmarkMetrics) -> Unit)? = null,
    var onErrorCallback: ((url: String?, errorCode: Int, description: String) -> Unit)? = null,
) : WebViewClient() {

    private var pageLoadStartTime: Long = 0L

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        pageLoadStartTime = System.currentTimeMillis()

        nativeBridge?.logSystemEvent("Page load started: $url")

        val (usedMemory, totalMemory) = getMemoryUsageMb()

        nativeBridge?.updateMetrics { current ->
            current.copy(
                loadStartTimeMs = pageLoadStartTime,
                usedMemoryMb = usedMemory,
                totalMemoryMb = totalMemory,
                lastUpdatedTimestamp = pageLoadStartTime,
            )
        }

        onPageStartedCallback?.invoke(url)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        val finishTime = System.currentTimeMillis()
        val duration = if (pageLoadStartTime > 0) finishTime - pageLoadStartTime else 0L

        nativeBridge?.logSystemEvent("Page load finished: $url (Duration: ${duration}ms)")

        val (usedMemory, totalMemory) = getMemoryUsageMb()

        var updatedMetrics = BenchmarkMetrics()
        nativeBridge?.updateMetrics { current ->
            val clickTime = current.navigationClickTimeMs
            val timeToFinished = if (clickTime > 0 && !current.coldStartRecorded) {
                finishTime - clickTime
            } else {
                current.timeToPageFinishedMs
            }
            val markRecorded = if (clickTime > 0) true else current.coldStartRecorded

            updatedMetrics = current.copy(
                pageLoadFinishedTimeMs = finishTime,
                totalLoadTimeMs = if (duration > 0) duration else current.totalLoadTimeMs,
                timeToPageFinishedMs = timeToFinished,
                coldStartRecorded = markRecorded,
                usedMemoryMb = usedMemory,
                totalMemoryMb = totalMemory,
                lastUpdatedTimestamp = finishTime,
            )
            updatedMetrics
        }

        if (injectBenchmarkingScript && (view != null)) {
            injectPerformanceScript(view)
        }

        onPageFinishedCallback?.invoke(url, updatedMetrics)
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?,
    ) {
        super.onReceivedError(view, request, error)
        val isMainFrame = request?.isForMainFrame ?: true
        if (isMainFrame) {
            val description = error?.description?.toString() ?: "Unknown error"
            val errorCode = error?.errorCode ?: -1
            val url = request?.url?.toString()

            nativeBridge?.logSystemEvent("Page load error ($errorCode): $description for $url")
            onErrorCallback?.invoke(url, errorCode, description)
        }
    }

    private fun injectPerformanceScript(view: WebView) {
        val jsScript = """
            (function() {
                try {
                    if (window.performance && window.performance.timing) {
                        var timing = window.performance.timing;
                        var navStart = timing.navigationStart;
                        if (navStart > 0) {
                            var domContentLoaded = timing.domContentLoadedEventEnd - navStart;
                            var domInteractive = timing.domInteractive - navStart;
                            var loadEnd = timing.loadEventEnd - navStart;
                            
                            var metricsObj = {
                                domContentLoadedTimeMs: domContentLoaded > 0 ? domContentLoaded : (domInteractive > 0 ? domInteractive : 0),
                                totalInitTimeMs: loadEnd > 0 ? loadEnd : 0,
                                timestamp: Date.now()
                            };
                            
                            if (window.NativeBridge && typeof window.NativeBridge.sendMetrics === 'function') {
                                window.NativeBridge.sendMetrics(JSON.stringify(metricsObj));
                            } else if (window.AndroidBridge && typeof window.AndroidBridge.sendMetrics === 'function') {
                                window.AndroidBridge.sendMetrics(JSON.stringify(metricsObj));
                            }
                        }
                    }
                } catch(e) {
                    console.log("Error extracting performance timing: " + e.message);
                }
            })();
        """.trimIndent()

        view.evaluateJavascript(jsScript, null)
    }

    private fun getMemoryUsageMb(): Pair<Double, Double> {
        val runtime = Runtime.getRuntime()
        val usedMem = (runtime.totalMemory() - runtime.freeMemory()) / (1024.0 * 1024.0)
        val totalMem = runtime.maxMemory() / (1024.0 * 1024.0)
        return Pair(
            (usedMem * 100.0).roundToLong() / 100.0,
            (totalMem * 100.0).roundToLong() / 100.0,
        )
    }
}
