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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.lucasgoncalves.fastwebview.FastWebView
import br.com.lucasgoncalves.fastwebview.FastWebViewComposable
import org.json.JSONObject

data class FastLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String,
    val message: String,
    val isIncoming: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastWebViewScreen(
    onBackClick: () -> Unit
) {
    var selectedComponentIndex by remember { mutableIntStateOf(0) }
    val componentTags = listOf("checkout-widget", "react-mock-component")
    val activeTag = componentTags[selectedComponentIndex]

    var currentPropsJson by remember {
        mutableStateOf("""{"title": "Fast Store Checkout", "price": 99.99, "quantity": 2}""")
    }

    var activeFastWebView by remember { mutableStateOf<FastWebView?>(null) }
    val logs = remember { mutableStateListOf<FastLogEntry>() }

    var mojoLatencyMs by remember { mutableStateOf<Long?>(null) }
    var lastMountTimeMs by remember { mutableStateOf<Long?>(null) }

    fun addLog(tag: String, message: String, isIncoming: Boolean) {
        logs.add(0, FastLogEntry(tag = tag, message = message, isIncoming = isIncoming))
        if (logs.size > 50) {
            logs.removeAt(logs.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "FastWebView Playground",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Mojo IPC • RAM Cache • Zero-Eval",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        logs.clear()
                        addLog("SYSTEM", "Logs cleared", false)
                    }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Logs"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Component Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Selecionar WebComponent:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        componentTags.forEachIndexed { index, tag ->
                            SegmentedButton(
                                selected = selectedComponentIndex == index,
                                onClick = {
                                    selectedComponentIndex = index
                                    if (index == 0) {
                                        currentPropsJson = """{"title": "Fast Store Checkout", "price": 99.99, "quantity": 2}"""
                                    } else {
                                        currentPropsJson = """{"componentName": "React Metrics Dashboard", "count": 10, "status": "Live"}"""
                                    }
                                    addLog("CONTROL", "Switched component to <$tag>", false)
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = componentTags.size)
                            ) {
                                Text(
                                    text = "<$tag>",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Real-Time Mojo IPC Benchmark Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("Mojo IPC", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Text(
                            text = if (mojoLatencyMs != null) "${mojoLatencyMs} ms" else "< 1 ms",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("Mount Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Text(
                            text = if (lastMountTimeMs != null) "${lastMountTimeMs} ms" else "Warm",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.Memory, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("Asset Cache", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Text(
                            text = "RAM (0ms)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // FastWebView Viewport
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                FastWebViewComposable(
                    modifier = Modifier.fillMaxSize(),
                    componentTag = activeTag,
                    propsJson = currentPropsJson,
                    onMessageReceived = { msg ->
                        try {
                            val json = JSONObject(msg)
                            val type = json.optString("type", "MSG")
                            if (json.has("mountTimeMs")) {
                                lastMountTimeMs = json.optLong("mountTimeMs")
                            }
                            if (json.has("timestamp")) {
                                val sendTime = json.optLong("timestamp")
                                if (sendTime > 0) {
                                    mojoLatencyMs = (System.currentTimeMillis() - sendTime).coerceAtLeast(0L)
                                }
                            }
                            addLog("IPC IN", "[$type] $msg", true)
                        } catch (_: Exception) {
                            addLog("IPC IN", msg, true)
                        }
                    },
                    onBinaryReceived = { bytes ->
                        addLog("IPC BIN", "Received ${bytes.size} bytes ArrayBuffer directly via Mojo IPC", true)
                    },
                    onFastWebViewCreated = { webView ->
                        activeFastWebView = webView
                        addLog("POOL", "FastWebView acquired from WebViewWarmupPool", false)
                    }
                )
            }

            // Quick Direct Action Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val updatedPrice = (80..190).random() + 0.99
                        currentPropsJson = """{"title": "Updated Cart Item", "price": $updatedPrice, "quantity": ${(1..5).random()}}"""
                        activeFastWebView?.updateProps(currentPropsJson)
                        addLog("IPC OUT", "updateProps(zero-eval) -> $currentPropsJson", false)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Update Props", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        val binaryData = byteArrayOf(0x46, 0x41, 0x53, 0x54, 0x57, 0x45, 0x42, 0x21) // "FASTWEB!"
                        activeFastWebView?.sendDirectBinary(binaryData)
                        addLog("IPC OUT", "sendDirectBinary(zero-eval ArrayBuffer, 8 bytes)", false)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(imageVector = Icons.Default.DataObject, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Send Binary", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        activeFastWebView?.resetForPool()
                        addLog("CONTROL", "resetForPool() executed", false)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }

            // Live IPC Console Logs
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("Mojo IPC Event Log", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        Text("${logs.size} msgs", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(logs) { entry ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (entry.isIncoming) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (entry.isIncoming) "← ${entry.tag}" else "→ ${entry.tag}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (entry.isIncoming) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = entry.message,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
