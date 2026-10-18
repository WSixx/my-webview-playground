# FastWebView

FastWebView is a high-performance Android WebView library designed for instant WebComponent rendering and zero-latency native-to-JavaScript communication. It bypasses legacy string-eval bridges by using Chromium Mojo IPC directly (`addWebMessageListener` and `JavaScriptReplyProxy`).

## Key Features

- **Chromium Mojo IPC**: Direct bidirectional native <-> JS messaging using `JavaScriptReplyProxy` without `evaluateJavascript`.
- **Zero-Eval Binary Transfer**: Directly transfer `ArrayBuffer` and raw byte arrays over native Mojo channels.
- **RAM-Cached Asset Loader**: Serves local shell HTML, bundles, and CSS directly from RAM using `WebViewAssetLoader` with immutable cache headers (`Cache-Control: public, max-age=31536000, immutable`).
- **Engine Pre-Warming & Instance Pooling**: Utilizes `WebViewStartUpConfig` via `WebViewCompat.startUpWebView` for background engine warming and `MutableContextWrapper` for pre-allocated instance pooling without Activity memory leaks.
- **Jetpack Compose Native Wrapper**: `FastWebViewComposable` for seamless integration into Compose hierarchies.
- **React Native Compatibility**: `FastWebComponentViewManager` bridge for integration into React Native applications.

---

## Installation

### 1. Include Module in `settings.gradle.kts`

```kotlin
include(":fastwebview")
```

### 2. Add Dependency in `build.gradle.kts`

```kotlin
dependencies {
    implementation(project(":fastwebview"))
}
```

---

## Quick Start

### 1. Pre-warm Engine in `Application` Class

To eliminate cold-start latency, pre-warm the Chromium engine and asset cache inside your `Application` class:

```kotlin
import android.app.Application
import br.com.lucasgoncalves.fastwebview.WebViewWarmupPool

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Pre-warms Chromium rendering engine and pre-loads assets into RAM cache
        WebViewWarmupPool.warmUpEngineAndPool(this)
    }
}
```

---

### 2. Jetpack Compose Usage (`FastWebViewComposable`)

Render WebComponents seamlessly inside your Jetpack Compose screens:

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import br.com.lucasgoncalves.fastwebview.FastWebViewComposable

@Composable
fun CheckoutScreen() {
    FastWebViewComposable(
        modifier = Modifier.fillMaxSize(),
        componentTag = "checkout-widget",
        propsJson = """{"price": 149.99, "quantity": 1}""",
        onMessageReceived = { jsonMessage ->
            println("Native received string message: $jsonMessage")
        },
        onBinaryReceived = { byteArray ->
            println("Native received binary bytes of size: ${byteArray.size}")
        }
    )
}
```

---

### 3. Classic Android View / XML Usage

Or acquire instances directly from the pool in Activity/Fragment layouts:

```kotlin
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.lucasgoncalves.fastwebview.FastWebView
import br.com.lucasgoncalves.fastwebview.WebViewWarmupPool

class MainActivity : AppCompatActivity() {

    private lateinit var fastWebView: FastWebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Acquire pre-warmed instance from pool
        fastWebView = WebViewWarmupPool.acquireFastWebView(this)
        setContentView(fastWebView)

        fastWebView.onNativeMessageReceived = { message ->
            // Handle incoming native messages
        }

        fastWebView.onBinaryMessageReceived = { bytes ->
            // Handle raw ArrayBuffer bytes
        }

        // Mount a custom WebComponent
        fastWebView.mountComponent("checkout-widget", """{"price": 99.99}""")
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release instance back to pool
        WebViewWarmupPool.releaseFastWebView(fastWebView)
    }
}
```

---

### 4. Sending Messages (Zero-Eval)

Send text or binary payloads directly to JavaScript without invoking `evaluateJavascript`:

```kotlin
// Send JSON string
fastWebView.sendDirectMessage("""{"type":"UPDATE_PRICE","price":199.99}""")

// Send raw binary ArrayBuffer
val binaryPayload = byteArrayOf(0x01, 0x02, 0x03, 0x04)
fastWebView.sendDirectBinary(binaryPayload)
```

---

### 5. React Native Integration (`FastWebComponentViewManager`)

Register `FastWebComponentViewManager` in your React Native package provider:

```kotlin
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import br.com.lucasgoncalves.fastwebview.FastWebComponentViewManager

class FastWebViewPackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> = emptyList()

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return listOf(FastWebComponentViewManager())
    }
}
```

In JSX/TSX:

```tsx
import { requireNativeComponent } from 'react-native';

const FastWebComponentView = requireNativeComponent('FastWebComponentView');

export const CheckoutWidget = () => (
  <FastWebComponentView
    style={{ flex: 1 }}
    componentTag="checkout-widget"
    propsJson={JSON.stringify({ price: 149.99, quantity: 2 })}
  />
);
```

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                      FastWebView App                        │
└──────────────────────────────┬──────────────────────────────┘
                               │
               ┌───────────────┴───────────────┐
               │  WebViewWarmupPool (Singleton) │
               └───────────────┬───────────────┘
                               │ (Pre-warms & Pools)
               ┌───────────────▼───────────────┐
               │          FastWebView          │
               └──────┬─────────────────┬──────┘
                      │                 │
    (Mojo IPC Channel)│                 │(RAM-Cached Assets)
                      ▼                 ▼
             JavaScriptReplyProxy   RamCachedAssetHandler
                      │                 │
                      ▼                 ▼
          ┌──────────────────────────────────┐
          │     Chromium WebView Engine      │
          └──────────────────────────────────┘
```

---

## License

```
Copyright (C) 2025 Lucas S Goncalves

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
