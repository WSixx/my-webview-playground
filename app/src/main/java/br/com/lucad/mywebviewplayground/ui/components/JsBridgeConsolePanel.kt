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

package br.com.lucad.mywebviewplayground.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.lucad.mywebviewplayground.model.BridgeDirection
import br.com.lucad.mywebviewplayground.model.BridgeLogMessage
import br.com.lucad.mywebviewplayground.ui.theme.MyWebviewPlaygroundTheme
import br.com.lucad.mywebviewplayground.viewmodel.LogFilter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JsBridgeConsolePanel(
    logs: List<BridgeLogMessage>,
    activeFilter: LogFilter,
    onFilterChange: (LogFilter) -> Unit,
    onSendMessage: (String) -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    var messageInput by remember { mutableStateOf("""{"action": "SET_COUNTER", "value": 42}""") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Section Header & Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Native JS Bridge Console",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${logs.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            IconButton(onClick = onClearLogs) {
                Icon(
                    imageVector = Icons.Default.CleaningServices,
                    contentDescription = "Clear Logs",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        // Send Message Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageInput,
                onValueChange = { messageInput = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type native JSON or string payload...", fontSize = 12.sp) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            IconButton(
                onClick = {
                    if (messageInput.isNotBlank()) {
                        onSendMessage(messageInput)
                    }
                },
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Native Message",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // Preset Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AssistChip(
                onClick = {
                    val preset = """{"action": "SET_COUNTER", "value": 42}"""
                    messageInput = preset
                    onSendMessage(preset)
                },
                label = { Text("SET_COUNTER", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Code, null, modifier = Modifier.size(14.dp)) }
            )

            AssistChip(
                onClick = {
                    val preset = """{"action": "PING", "timestamp": ${System.currentTimeMillis()}}"""
                    messageInput = preset
                    onSendMessage(preset)
                },
                label = { Text("PING", fontSize = 11.sp) }
            )

            AssistChip(
                onClick = {
                    val preset = """{"action": "ECHO", "message": "Hello from Native!"}"""
                    messageInput = preset
                    onSendMessage(preset)
                },
                label = { Text("ECHO", fontSize = 11.sp) }
            )
        }

        // Log Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LogFilter.entries.forEach { filter ->
                FilterChip(
                    selected = activeFilter == filter,
                    onClick = { onFilterChange(filter) },
                    label = { Text(filter.label, fontSize = 12.sp) }
                )
            }
        }

        // Log Message List
        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No bridge messages in current filter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(logs, key = { it.id }) { log ->
                    BridgeLogItem(log = log)
                }
            }
        }
    }
}

@Composable
private fun BridgeLogItem(log: BridgeLogMessage) {
    val (icon: ImageVector, tagColor: Color, tagLabel: String) = when (log.direction) {
        BridgeDirection.INCOMING -> Triple(
            Icons.AutoMirrored.Filled.ArrowBack,
            Color(0xFF2E7D32),
            "IN"
        )
        BridgeDirection.OUTGOING -> Triple(
            Icons.AutoMirrored.Filled.ArrowForward,
            Color(0xFF1565C0),
            "OUT"
        )
        BridgeDirection.SYSTEM -> Triple(
            Icons.Default.Info,
            Color(0xFFEF6C00),
            "SYS"
        )
        BridgeDirection.WEB_CONSOLE -> Triple(
            Icons.Default.Terminal,
            Color(0xFF6A1B9A),
            "CONSOLE"
        )
    }

    val formattedTime = remember(log.timestamp) {
        SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(log.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = tagColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = tagColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tagLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = tagColor
                            )
                        }
                    }

                    if (!log.type.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = log.type,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Text(
                    text = formattedTime,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = log.payload,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JsBridgeConsolePanelPreview() {
    MyWebviewPlaygroundTheme {
        JsBridgeConsolePanel(
            logs = listOf(
                BridgeLogMessage(
                    direction = BridgeDirection.INCOMING,
                    payload = """{"type": "COUNTER_CHANGED", "payload": {"count": 42}}""",
                    type = "COUNTER_CHANGED"
                ),
                BridgeLogMessage(
                    direction = BridgeDirection.OUTGOING,
                    payload = """{"action": "SET_COUNTER", "value": 42}""",
                    type = "POST_TO_WEB"
                ),
                BridgeLogMessage(
                    direction = BridgeDirection.SYSTEM,
                    payload = "Page load finished: file:///android_asset/mock_webcomponent.html",
                    type = "SYSTEM"
                )
            ),
            activeFilter = LogFilter.ALL,
            onFilterChange = {},
            onSendMessage = {},
            onClearLogs = {}
        )
    }
}
