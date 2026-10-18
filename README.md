# FastWebView & WebComponent Playground

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Android MinSDK](https://img.shields.io/badge/MinSDK-28-green.svg)](https://developer.android.com/about/versions/pie)
[![TargetSDK](https://img.shields.io/badge/TargetSDK-37-brightgreen.svg)](https://developer.android.com/about/versions/16)

**FastWebView** is a production-ready, zero-latency Android WebView SDK created by **Lucas S Goncalves**. Designed for high-performance WebComponent and micro-frontend rendering, `fastwebview` eliminates traditional WebView overhead through direct Chromium Mojo IPC messaging, RAM asset pre-loading with V8 bytecode caching, background engine pre-warming, and first-class support for Jetpack Compose and React Native.

---

## Key Features

- ⚡ **Chromium Mojo IPC Zero-Eval Messaging**: Bypasses legacy string-eval bridges (`evaluateJavascript`) by utilizing `addWebMessageListener` and `JavaScriptReplyProxy` for direct, high-speed, bidirectional native <-> JS communication.
- 📦 **RAM Asset Caching & V8 Bytecode Cache**: Serves local shell HTML, bundles, and styles directly from RAM using `WebViewAssetLoader` with `Cache-Control: public, max-age=31536000, immutable` headers, enabling Chromium's V8 engine to store and execute compiled bytecode.
- 🔥 **Background Engine Pre-Warming Pool**: `WebViewWarmupPool` leverages `WebViewStartUpConfig` via `WebViewCompat.startUpWebView` to prime Chromium engines in the background and uses `MutableContextWrapper` for instant instance pooling without Activity context leaks.
- 🔄 **Zero-Copy Binary Transfer**: Transfer raw byte arrays (`ByteArray` / `ArrayBuffer`) directly over Mojo IPC channels without Base64 encoding/decoding overhead.
- 🎨 **Jetpack Compose Native Support**: Idiomatic `FastWebViewComposable` component with automatic state lifecycle management and pool disposal.
- ⚛️ **React Native Compatibility**: Built-in `FastWebComponentViewManager` bridge for seamless integration into React Native apps.

---

## Installation

### 1. Add Repository (JitPack)

In your root `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = java.net.URI("https://jitpack.io") }
    }
}
```

### 2. Add Dependency

In your app module `build.gradle.kts`:

```kotlin
dependencies {
    // Standalone SDK via JitPack
    implementation("com.github.lucasgoncalves:fastwebview:1.0.0")

    // Or via local project / composite build module
    // implementation(project(":fastwebview"))
}
```

---

## Quick Start Guide

### 1. Pre-warm Engine in `Application`

Eliminate cold-start latency by initializing the Chromium engine and asset cache in your `Application` class:

```kotlin
import android.app.Application
import br.com.lucasgoncalves.fastwebview.WebViewWarmupPool

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Pre-warms Chromium rendering engine and loads critical assets into RAM
        WebViewWarmupPool.warmUpEngineAndPool(this)
    }
}
```

### 2. Jetpack Compose Integration

Render WebComponents seamlessly with `FastWebViewComposable`:

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
        propsJson = """{"price": 149.99, "currency": "USD"}""",
        onMessageReceived = { message ->
            println("Received message from JS: $message")
        },
        onBinaryReceived = { bytes ->
            println("Received binary payload of size: ${bytes.size}")
        }
    )
}
```

### 3. Classic Android Views / Activity

Acquire pre-warmed instances from `WebViewWarmupPool`:

```kotlin
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.lucasgoncalves.fastwebview.FastWebView
import br.com.lucasgoncalves.fastwebview.WebViewWarmupPool

class MainActivity : AppCompatActivity() {

    private lateinit var fastWebView: FastWebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Acquire pre-warmed instance re-parented to Activity context
        fastWebView = WebViewWarmupPool.acquireFastWebView(this)
        setContentView(fastWebView)

        fastWebView.onNativeMessageReceived = { msg ->
            println("Native received: $msg")
        }

        // Mount WebComponent with props
        fastWebView.mountComponent("checkout-widget", """{"item": "Laptop"}""")
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release instance back to pool for future reuse
        WebViewWarmupPool.releaseFastWebView(fastWebView)
    }
}
```

### 4. React Native Integration

Register `FastWebComponentViewManager` in your React Native package:

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

Use in JSX/TSX:

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

## Local vs. Remote Loading Comparison

| Metric / Feature | Bundled Local Assets (`RamCachedAssetHandler`) | Remote Server (`https://...`) |
| :--- | :--- | :--- |
| **Availability** | **100% Offline Compatible** | Requires active network connection |
| **Latency & First Paint** | **< 50ms DOM Ready** (RAM I/O + V8 Cache) | Network RTT + DNS + TLS overhead |
| **JS Bridge Speed** | **Zero-Eval Mojo IPC Handshake** | Subject to domain validation delays |
| **Security & Integrity** | **Tamper-resistant** (Signed APK bundle) | Requires strict CSP and CORS headers |

---

## Detailed Documentation

For comprehensive API specifications, Mojo IPC protocol details, and advanced optimization guidelines, see the [fastwebview Library Documentation](fastwebview/README.md).

---

## Building and Running

- **Build Debug APK**: `./gradlew assembleDebug`
- **Run Unit Tests**: `./gradlew test`

---

## License & Author

Created by **Lucas S Goncalves**.

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
