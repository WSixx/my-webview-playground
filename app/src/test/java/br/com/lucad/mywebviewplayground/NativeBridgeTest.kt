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

import br.com.lucad.mywebviewplayground.bridge.NativeBridge
import br.com.lucad.mywebviewplayground.model.BridgeDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NativeBridgeTest {

    private lateinit var nativeBridge: NativeBridge

    @Before
    fun setUp() {
        nativeBridge = NativeBridge()
    }

    @Test
    fun testPostMessageUpdatesLogsAndLastMessage() {
        val testJson = """{"type":"TEST_EVENT","payload":{"key":"value"}}"""
        nativeBridge.postMessage(testJson)

        assertEquals(testJson, nativeBridge.lastMessage.value)

        val logs = nativeBridge.logs.value
        assertEquals(1, logs.size)
        assertEquals(BridgeDirection.INCOMING, logs[0].direction)
        assertEquals("TEST_EVENT", logs[0].type)
        assertEquals(testJson, logs[0].payload)
    }

    @Test
    fun testSendMetricsParsesPerformanceMetrics() {
        val now = System.currentTimeMillis()
        val metricsJson = """
            {
                "type": "PERFORMANCE_METRICS",
                "payload": {
                    "domContentLoadedTimeMs": 150,
                    "renderTimeMs": 45.5,
                    "totalInitTimeMs": 200,
                    "timestamp": $now
                }
            }
        """.trimIndent()

        nativeBridge.sendMetrics(metricsJson)

        val metrics = nativeBridge.metrics.value
        assertEquals(150L, metrics.domContentLoadedTimeMs)
        assertEquals(45.5, metrics.componentRenderTimeMs, 0.01)
        assertEquals(200L, metrics.totalLoadTimeMs)
        assertTrue(metrics.jsBridgeMessageCount > 0)
    }

    @Test
    fun testOnMessageListenerTriggered() {
        var receivedType: String? = null
        var receivedPayload: String? = null

        nativeBridge.onMessageListener = { type, payload ->
            receivedType = type
            receivedPayload = payload
        }

        val testMsg = """{"type":"COUNTER_CHANGED","payload":{"count":5}}"""
        nativeBridge.postMessage(testMsg)

        assertEquals("COUNTER_CHANGED", receivedType)
        assertNotNull(receivedPayload)
        assertTrue(receivedPayload!!.contains("count"))
    }

    @Test
    fun testClearLogsResetsLogsList() {
        nativeBridge.log("Log message 1")
        nativeBridge.log("Log message 2")

        assertEquals(2, nativeBridge.logs.value.size)

        nativeBridge.clearLogs()

        assertTrue(nativeBridge.logs.value.isEmpty())
    }

    @Test
    fun testLogOutgoingMessage() {
        nativeBridge.logOutgoingMessage("Sending hello to web", "HELLO_ACTION")

        val logs = nativeBridge.logs.value
        assertEquals(1, logs.size)
        assertEquals(BridgeDirection.OUTGOING, logs[0].direction)
        assertEquals("HELLO_ACTION", logs[0].type)
    }
}
