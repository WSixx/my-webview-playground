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

package br.com.lucasgoncalves.fastwebview

import android.content.Context
import android.webkit.WebResourceResponse
import androidx.webkit.WebViewAssetLoader
import java.io.ByteArrayInputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * Custom [WebViewAssetLoader.PathHandler] that loads and caches critical assets
 * (`shell.html`, `webcomponents.bundle.js`, `global.css`) directly in RAM.
 * Serves them with high-performance `Cache-Control: public, max-age=31536000, immutable` headers.
 */
class RamCachedAssetHandler(
    private val context: Context
) : WebViewAssetLoader.PathHandler {

    override fun handle(path: String): WebResourceResponse? {
        val cleanPath = path.removePrefix("/").trim()
        val cachedBytes = getOrLoadAsset(cleanPath) ?: return null

        val mimeType = getMimeType(cleanPath)
        val headers = mapOf(
            "Cache-Control" to "public, max-age=31536000, immutable",
            "Access-Control-Allow-Origin" to "*",
            "X-FastWebView-RamCached" to "true"
        )

        return WebResourceResponse(
            mimeType,
            "UTF-8",
            200,
            "OK",
            headers,
            ByteArrayInputStream(cachedBytes)
        )
    }

    private fun getOrLoadAsset(assetPath: String): ByteArray? {
        ramCache[assetPath]?.let { return it }

        return try {
            context.assets.open(assetPath).use { stream ->
                val bytes = stream.readBytes()
                ramCache[assetPath] = bytes
                bytes
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun getMimeType(path: String): String {
        return when {
            path.endsWith(".html", ignoreCase = true) || path.endsWith(".htm", ignoreCase = true) -> "text/html"
            path.endsWith(".js", ignoreCase = true) -> "text/javascript"
            path.endsWith(".css", ignoreCase = true) -> "text/css"
            path.endsWith(".json", ignoreCase = true) -> "application/json"
            path.endsWith(".png", ignoreCase = true) -> "image/png"
            path.endsWith(".jpg", ignoreCase = true) || path.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            path.endsWith(".svg", ignoreCase = true) -> "image/svg+xml"
            else -> "application/octet-stream"
        }
    }

    companion object {
        private val ramCache = ConcurrentHashMap<String, ByteArray>()

        /**
         * Pre-loads critical assets into RAM cache to ensure zero disk I/O when serving requests.
         */
        fun preloadAssets(
            context: Context,
            paths: List<String> = listOf("shell.html", "webcomponents.bundle.js", "global.css")
        ) {
            val appContext = context.applicationContext
            for (path in paths) {
                try {
                    appContext.assets.open(path).use { stream ->
                        ramCache[path] = stream.readBytes()
                    }
                } catch (_: Exception) {}
            }
        }

        fun clearCache() {
            ramCache.clear()
        }
    }
}
