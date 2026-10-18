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
