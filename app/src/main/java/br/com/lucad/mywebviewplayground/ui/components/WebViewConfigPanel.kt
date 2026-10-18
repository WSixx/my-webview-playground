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

import android.webkit.WebSettings
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.lucad.mywebviewplayground.model.WebViewSettingsState
import br.com.lucad.mywebviewplayground.ui.theme.MyWebviewPlaygroundTheme

@Composable
fun WebViewConfigPanel(
    settingsState: WebViewSettingsState,
    onSettingsChange: (WebViewSettingsState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Native WebView Configuration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Presets Bar
        Text(
            text = "Presets",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = { onSettingsChange(WebViewSettingsState.Default) },
                label = { Text("Default") },
                leadingIcon = { Icon(Icons.Default.Restore, null) }
            )

            AssistChip(
                onClick = { onSettingsChange(WebViewSettingsState.HighPerformance) },
                label = { Text("High Performance / No Cache") },
                leadingIcon = { Icon(Icons.Default.Speed, null) }
            )

            AssistChip(
                onClick = {
                    onSettingsChange(
                        settingsState.copy(
                            cacheMode = WebSettings.LOAD_CACHE_ONLY,
                            javaScriptEnabled = true
                        )
                    )
                },
                label = { Text("Offline / Cache Only") },
                leadingIcon = { Icon(Icons.Default.WifiOff, null) }
            )
        }

        // Cache Mode Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Cache Strategy Mode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val cacheModes = listOf(
                        WebSettings.LOAD_DEFAULT to "Default",
                        WebSettings.LOAD_NO_CACHE to "No Cache",
                        WebSettings.LOAD_CACHE_ELSE_NETWORK to "Cache Else Network",
                        WebSettings.LOAD_CACHE_ONLY to "Cache Only"
                    )

                    cacheModes.forEach { (mode, label) ->
                        FilterChip(
                            selected = settingsState.cacheMode == mode,
                            onClick = { onSettingsChange(settingsState.copy(cacheMode = mode)) },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // Feature Toggles
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Feature Toggles",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                ConfigToggleRow(
                    label = "JavaScript Enabled",
                    description = "Enable execution of JS code in WebView",
                    checked = settingsState.javaScriptEnabled,
                    onCheckedChange = { onSettingsChange(settingsState.copy(javaScriptEnabled = it)) }
                )

                ConfigToggleRow(
                    label = "DOM Storage (localStorage)",
                    description = "Allow web page to store key-value data",
                    checked = settingsState.domStorageEnabled,
                    onCheckedChange = { onSettingsChange(settingsState.copy(domStorageEnabled = it)) }
                )

                ConfigToggleRow(
                    label = "Hardware Acceleration",
                    description = "GPU acceleration layer for rendering",
                    checked = settingsState.hardwareAccelerationEnabled,
                    onCheckedChange = { onSettingsChange(settingsState.copy(hardwareAccelerationEnabled = it)) }
                )

                ConfigToggleRow(
                    label = "Built-in Zoom Controls",
                    description = "Enable on-screen zoom mechanisms",
                    checked = settingsState.builtInZoomControls,
                    onCheckedChange = {
                        onSettingsChange(
                            settingsState.copy(
                                builtInZoomControls = it,
                                supportZoom = it || settingsState.supportZoom
                            )
                        )
                    }
                )

                ConfigToggleRow(
                    label = "Database Enabled (WebSQL/IndexedDB)",
                    description = "Enable client-side HTML5 databases",
                    checked = settingsState.databaseEnabled,
                    onCheckedChange = { onSettingsChange(settingsState.copy(databaseEnabled = it)) }
                )

                ConfigToggleRow(
                    label = "Allow Local File Access",
                    description = "Permit access to file:/// asset URLs",
                    checked = settingsState.allowFileAccess,
                    onCheckedChange = { onSettingsChange(settingsState.copy(allowFileAccess = it)) }
                )

                ConfigToggleRow(
                    label = "Allow Content Access",
                    description = "Enable content:// provider URLs",
                    checked = settingsState.allowContentAccess,
                    onCheckedChange = { onSettingsChange(settingsState.copy(allowContentAccess = it)) }
                )

                ConfigToggleRow(
                    label = "Media Playback User Gesture",
                    description = "Require user touch gesture for audio/video",
                    checked = settingsState.mediaPlaybackRequiresUserGesture,
                    onCheckedChange = { onSettingsChange(settingsState.copy(mediaPlaybackRequiresUserGesture = it)) }
                )
            }
        }
    }
}

@Composable
private fun ConfigToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WebViewConfigPanelPreview() {
    MyWebviewPlaygroundTheme {
        WebViewConfigPanel(
            settingsState = WebViewSettingsState.Default,
            onSettingsChange = {}
        )
    }
}
