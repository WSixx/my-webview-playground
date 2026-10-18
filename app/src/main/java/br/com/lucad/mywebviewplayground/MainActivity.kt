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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import br.com.lucad.mywebviewplayground.ui.FastWebViewScreen
import br.com.lucad.mywebviewplayground.ui.HomeScreen
import br.com.lucad.mywebviewplayground.ui.MainScreen
import br.com.lucad.mywebviewplayground.ui.navigation.AppRoute
import br.com.lucad.mywebviewplayground.ui.theme.MyWebviewPlaygroundTheme
import br.com.lucad.mywebviewplayground.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyWebviewPlaygroundTheme {
                AppNavigation(viewModel = mainViewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val backStack = rememberNavBackStack(AppRoute.Home)

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
        },
        entryProvider = { key ->
            val entry: NavEntry<NavKey> = when (key) {
                is AppRoute.Home -> NavEntry(key) {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToWebView = {
                            backStack.add(AppRoute.WebView)
                        },
                        onNavigateToFastWebView = {
                            backStack.add(AppRoute.FastWebView)
                        }
                    )
                }
                is AppRoute.WebView -> NavEntry(key) {
                    MainScreen(
                        viewModel = viewModel,
                        onBackClick = {
                            if (backStack.size > 1) {
                                backStack.removeAt(backStack.lastIndex)
                            }
                        }
                    )
                }
                is AppRoute.FastWebView -> NavEntry(key) {
                    FastWebViewScreen(
                        onBackClick = {
                            if (backStack.size > 1) {
                                backStack.removeAt(backStack.lastIndex)
                            }
                        }
                    )
                }
                else -> NavEntry(key) {}
            }
            entry
        }
    )
}
