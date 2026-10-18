package br.com.lucad.mywebviewplayground.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface AppRoute : NavKey {

    @Serializable
    data object Home : AppRoute

    @Serializable
    data object WebView : AppRoute

    @Serializable
    data object FastWebView : AppRoute
}
