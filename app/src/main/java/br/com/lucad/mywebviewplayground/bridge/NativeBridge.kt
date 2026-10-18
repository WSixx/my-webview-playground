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

package br.com.lucad.mywebviewplayground.bridge

import android.webkit.JavascriptInterface
import br.com.lucad.mywebviewplayground.model.BenchmarkMetrics
import br.com.lucad.mywebviewplayground.model.BridgeDirection
import br.com.lucad.mywebviewplayground.model.BridgeLogMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONObject

/**
 * JavaScript interface bridge exposed to WebView for bidirectional native-JS communication.
 */
class NativeBridge {

    private val _logs = MutableStateFlow<List<BridgeLogMessage>>(emptyList())
    val logs: StateFlow<List<BridgeLogMessage>> = _logs.asStateFlow()

    private val _metrics = MutableStateFlow(BenchmarkMetrics())
    val metrics: StateFlow<BenchmarkMetrics> = _metrics.asStateFlow()

    private val _lastMessage = MutableStateFlow<String?>(null)
    val lastMessage: StateFlow<String?> = _lastMessage.asStateFlow()

    var onMessageListener: ((type: String?, payload: String) -> Unit)? = null

    @JavascriptInterface
    fun postMessage(json: String) {
        handleIncomingMessage(json)
    }

    @JavascriptInterface
    fun onMessage(json: String) {
        handleIncomingMessage(json)
    }

    @JavascriptInterface
    fun onMessageFromWeb(json: String) {
        handleIncomingMessage(json)
    }

    @JavascriptInterface
    fun log(msg: String) {
        val logEntry = BridgeLogMessage(
            direction = BridgeDirection.INCOMING,
            payload = msg,
            type = "LOG",
        )
        addLogEntry(logEntry)
    }

    @JavascriptInterface
    fun sendMetrics(json: String) {
        handleIncomingMessage(json)
    }

    fun logOutgoingMessage(payload: String, type: String? = "OUTGOING") {
        val logEntry = BridgeLogMessage(
            direction = BridgeDirection.OUTGOING,
            payload = payload,
            type = type,
        )
        addLogEntry(logEntry)
    }

    fun logSystemEvent(payload: String) {
        val logEntry = BridgeLogMessage(
            direction = BridgeDirection.SYSTEM,
            payload = payload,
            type = "SYSTEM",
        )
        addLogEntry(logEntry)
    }

    fun logConsoleMessage(payload: String, level: String) {
        val logEntry = BridgeLogMessage(
            direction = BridgeDirection.WEB_CONSOLE,
            payload = payload,
            type = "CONSOLE_$level",
        )
        addLogEntry(logEntry)
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun updateMetrics(transform: (BenchmarkMetrics) -> BenchmarkMetrics) {
        _metrics.update(transform)
    }

    private fun handleIncomingMessage(rawMessage: String) {
        val receiveTime = System.currentTimeMillis()
        _lastMessage.value = rawMessage

        var msgType: String? = null
        var msgPayloadStr = rawMessage

        try {
            val jsonObj = JSONObject(rawMessage)
            if (jsonObj.has("type")) {
                msgType = jsonObj.optString("type")
            }
            if (jsonObj.has("payload")) {
                val payloadObj = jsonObj.opt("payload")
                if (payloadObj != null) {
                    msgPayloadStr = payloadObj.toString()
                    if ((msgType == null || "PERFORMANCE_METRICS".equals(msgType, ignoreCase = true)) && payloadObj is JSONObject) {
                        parseMetricsObject(payloadObj, receiveTime)
                    }
                }
            } else if ("PERFORMANCE_METRICS".equals(msgType, ignoreCase = true) ||
                jsonObj.has("domContentLoadedTimeMs") ||
                jsonObj.has("totalInitTimeMs") ||
                jsonObj.has("renderTimeMs")
            ) {
                if (msgType == null) msgType = "PERFORMANCE_METRICS"
                parseMetricsObject(jsonObj, receiveTime)
            }
        } catch (_: Exception) {
            // Raw string payload
        }

        val logEntry = BridgeLogMessage(
            timestamp = receiveTime,
            direction = BridgeDirection.INCOMING,
            payload = rawMessage,
            type = msgType ?: "POST_MESSAGE",
        )
        addLogEntry(logEntry)

        _metrics.update { current ->
            val updatedMsgCount = current.jsBridgeMessageCount + 1
            current.copy(
                jsBridgeMessageCount = updatedMsgCount,
                lastUpdatedTimestamp = receiveTime,
            )
        }

        onMessageListener?.invoke(msgType, msgPayloadStr)
    }

    private fun parseMetricsObject(jsonObj: JSONObject, receiveTime: Long) {
        val domContentLoaded = jsonObj.optLong(
            "domContentLoadedTimeMs",
            jsonObj.optDouble("domContentLoadedTimeMs", 0.0).toLong(),
        )
        val renderTime = jsonObj.optDouble("renderTimeMs", 0.0)
        val totalInit = jsonObj.optDouble("totalInitTimeMs", 0.0)
        val sendTimestamp = jsonObj.optLong("timestamp", 0L)

        val latency = if (sendTimestamp in 1..receiveTime) {
            receiveTime - sendTimestamp
        } else {
            0L
        }

        _metrics.update { current ->
            current.copy(
                domContentLoadedTimeMs = if (domContentLoaded > 0) domContentLoaded else current.domContentLoadedTimeMs,
                componentRenderTimeMs = if (renderTime > 0.0) renderTime else current.componentRenderTimeMs,
                totalLoadTimeMs = if (totalInit > 0.0) totalInit.toLong() else current.totalLoadTimeMs,
                jsBridgeLatencyMs = if (latency > 0) latency else current.jsBridgeLatencyMs,
                lastUpdatedTimestamp = receiveTime,
            )
        }
    }

    private fun addLogEntry(entry: BridgeLogMessage) {
        _logs.update { current ->
            (listOf(entry) + current).take(100)
        }
    }

    companion object {
        const val BRIDGE_NAME = "NativeBridge"
        const val LEGACY_BRIDGE_NAME = "AndroidBridge"
    }
}
