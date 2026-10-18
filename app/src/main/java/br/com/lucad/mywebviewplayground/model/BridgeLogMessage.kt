package br.com.lucad.mywebviewplayground.model

import java.util.UUID

enum class BridgeDirection {
    INCOMING,
    OUTGOING,
    SYSTEM,
    WEB_CONSOLE,
}

/**
 * Log entry capturing message traffic between Android Native and JS WebView bridge.
 */
data class BridgeLogMessage(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val direction: BridgeDirection = BridgeDirection.INCOMING,
    val payload: String,
    val type: String? = null,
)
