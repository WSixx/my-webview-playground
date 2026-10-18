# FastWebView SDK

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Android MinSDK](https://img.shields.io/badge/MinSDK-28-green.svg)](https://developer.android.com/about/versions/pie)
[![TargetSDK](https://img.shields.io/badge/TargetSDK-37-brightgreen.svg)](https://developer.android.com/about/versions/16)

`fastwebview` is a standalone, high-performance Android WebView SDK created by **Lucas S Goncalves**. Designed specifically for WebComponent, micro-frontend, and hybrid mobile application architectures, `fastwebview` achieves near-native performance by replacing legacy string-eval bridges with direct Chromium Mojo IPC messaging, RAM asset caching, and background engine pre-warming.

---

## Table of Contents

1. [Gradle Dependency Configuration](#gradle-dependency-configuration)
2. [Mojo IPC & Zero-Eval Communication Deep Dive](#mojo-ipc--zero-eval-communication-deep-dive)
3. [API Reference](#api-reference)
   - [FastWebView](#fastwebview-1)
   - [WebViewWarmupPool](#webviewwarmuppool)
   - [RamCachedAssetHandler](#ramcachedassethandler)
   - [FastWebViewComposable](#fastwebviewcomposable)
   - [FastWebComponentViewManager](#fastwebcomponentviewmanager)
4. [Integration Recipes](#integration-recipes)
   - [Application Initialization](#1-application-initialization)
   - [Jetpack Compose](#2-jetpack-compose)
   - [Android Views & XML Layouts](#3-android-views--xml-layouts)
   - [React Native Bridge](#4-react-native-bridge)
5. [Memory Management & Context Re-parenting](#memory-management--context-re-parenting)
6. [License](#license)

---

## Gradle Dependency Configuration

### Option A: JitPack Repository

In `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = java.net.URI("https://jitpack.io") }
    }
}
```

In `build.gradle.kts` (app module):

```kotlin
dependencies {
    implementation("com.github.lucasgoncalves:fastwebview:1.0.0")
}
```

### Option B: Version Catalog (`libs.versions.toml`)

```toml
[versions]
fastwebview = "1.0.0"

[libraries]
fastwebview = { module = "com.github.lucasgoncalves:fastwebview", version.ref = "fastwebview" }
```

In `build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.fastwebview)
}
```

### Option C: Local Composite / Multi-Module Project

In `settings.gradle.kts`:

```kotlin
include(":fastwebview")
```

In `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":fastwebview"))
}
```

---

## Mojo IPC & Zero-Eval Communication Deep Dive

Traditional Android WebView bridges rely on `WebView.evaluateJavascript("javascriptString")` or `@JavascriptInterface`. Both approaches introduce significant runtime overhead:
- `evaluateJavascript` requires passing raw JS code strings, which V8 must tokenize, parse into ASTs, and execute on the JS main thread.
- `@JavascriptInterface` operates over synchronous Java Reflection, causing main-thread blockages and security vulnerabilities on older Android versions.

`fastwebview` leverages Chromium's underlying **Mojo IPC architecture** via AndroidX WebKit's `WebViewCompat.addWebMessageListener` and `JavaScriptReplyProxy`:

```
┌───────────────────────────────┐                  ┌───────────────────────────────┐
│         Native Android        │                  │      Chromium V8 Engine       │
│         (FastWebView)         │                  │      (WebComponent App)       │
└───────────────┬───────────────┘                  └───────────────┬───────────────┘
                │                                                  │
                │ 1. Register WebMessageListener ("NativeFastBridge")│
                ├─────────────────────────────────────────────────►│
                │                                                  │
                │ 2. Load shell.html & trigger JS initialization   │
                ├─────────────────────────────────────────────────►│
                │                                                  │
                │ 3. JS sends "__HANDSHAKE_READY__" over bridge    │
                │◄─────────────────────────────────────────────────┤
                │   (Captures JavaScriptReplyProxy)                │
                │                                                  │
                │ 4. Flush Pending Queues & trigger Handshake Done │
                │                                                  │
                │ 5. Direct Message / ArrayBuffer via replyProxy   │
                ├─────────────────────────────────────────────────►│
                │   (Zero-eval execution without AST parsing)     │
                │                                                  │
                │ 6. JS posts WebMessage directly to Native        │
                │◄─────────────────────────────────────────────────┤
```

### Handshake & Queueing Guarantee

When `FastWebView` loads the local shell HTML (`SHELL_URL`), JavaScript posts an initial `__HANDSHAKE_READY__` message over `window.NativeFastBridge`.

1. **Proxy Capture**: `FastWebView` captures the active `JavaScriptReplyProxy`.
2. **State Transition**: `isHandshakeReady` transitions to `true`.
3. **Queue Flushing**: Messages or binary `ArrayBuffer` payloads sent before handshake completion are automatically queued in `pendingMessageQueue` / `pendingBinaryQueue` and flushed in FIFO order upon handshake completion.

---

## API Reference

### `FastWebView`

Core `WebView` subclass providing zero-eval Mojo IPC communication and asset loading.

#### Package
`br.com.lucasgoncalves.fastwebview.FastWebView`

#### Constructor
```kotlin
class FastWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr)
```

#### Properties

| Property | Type | Description |
| :--- | :--- | :--- |
| `isHandshakeReady` | `Boolean` (read-only) | Indicates whether the Mojo IPC channel is active and ready for direct messaging. |
| `onNativeMessageReceived` | `((String) -> Unit)?` | Callback invoked when a text message is received from JavaScript. |
| `onBinaryMessageReceived` | `((ByteArray) -> Unit)?` | Callback invoked when an `ArrayBuffer` binary payload is received from JavaScript. |
| `onHandshakeCompleted` | `(() -> Unit)?` | Callback invoked immediately after Mojo IPC handshake completes and pending queues are flushed. |

#### Methods

- `fun sendDirectMessage(message: String)`: Posts a text string over Mojo IPC via `JavaScriptReplyProxy.postMessage(String)`.
- `fun sendDirectBinary(data: ByteArray)`: Posts raw binary bytes over Mojo IPC via `JavaScriptReplyProxy.postMessage(ByteArray)`.
- `fun mountComponent(tag: String, propsJson: String)`: Sends a JSON message instructing the shell to mount a WebComponent with specified props.
- `fun updateProps(propsJson: String)`: Sends updated props JSON to the currently mounted component.
- `fun resetForPool()`: Sends reset message to JS shell, clears callbacks (`onNativeMessageReceived`, etc.), and empties pending queues to prepare the instance for pool reuse.

#### Constants

- `FAST_BRIDGE_NAME`: `"NativeFastBridge"`
- `SHELL_URL`: `"https://appassets.androidplatform.net/assets/shell.html"`

---

### `WebViewWarmupPool`

Singleton manager for pre-warming Chromium rendering engines and managing `FastWebView` instance allocation.

#### Package
`br.com.lucasgoncalves.fastwebview.WebViewWarmupPool`

#### Properties

| Property | Type | Description |
| :--- | :--- | :--- |
| `isEngineWarm` | `Boolean` (read-only) | `true` if the Chromium engine and asset RAM cache have been initialized. |

#### Methods

- `fun warmUpEngineAndPool(context: Context)`: Asynchronously initializes Chromium via `WebViewCompat.startUpWebView` (if supported), pre-loads static assets into RAM cache, and pre-instantiates a `FastWebView` wrapped in `MutableContextWrapper`.
- `fun acquireFastWebView(activityContext: Context): FastWebView`: Retrieves a pre-warmed instance from the pool and re-parents its `MutableContextWrapper` base context to the provided `activityContext`. Returns a fresh instance if pool is empty.
- `fun releaseFastWebView(webView: FastWebView)`: Resets `webView` via `resetForPool()`, re-parents its context back to `applicationContext`, and places it back into the pool.
- `fun clearPool()`: Stops, destroys, and clears all pre-allocated instances in the pool.

---

### `RamCachedAssetHandler`

Implementation of `WebViewAssetLoader.PathHandler` that caches assets in RAM and serves them with immutable HTTP cache headers.

#### Package
`br.com.lucasgoncalves.fastwebview.RamCachedAssetHandler`

#### Constructor
```kotlin
class RamCachedAssetHandler(context: Context) : WebViewAssetLoader.PathHandler
```

#### HTTP Cache Headers
Serves files with:
- `Cache-Control: public, max-age=31536000, immutable`
- `Access-Control-Allow-Origin: *`
- `X-FastWebView-RamCached: true`

*Note*: Serving JS and CSS files with `immutable` headers enables Chromium's V8 engine to cache compiled bytecode in disk/RAM, skipping tokenization and parsing on subsequent loads.

#### Companion Methods

- `fun preloadAssets(context: Context, paths: List<String> = listOf("shell.html", "webcomponents.bundle.js", "global.css"))`: Pre-loads specified asset files from APK assets directly into `ConcurrentHashMap` RAM cache.
- `fun clearCache()`: Flushes the RAM asset cache.

---

### `FastWebViewComposable`

Jetpack Compose wrapper for `FastWebView`.

#### Package
`br.com.lucasgoncalves.fastwebview.FastWebViewComposable`

#### Signature
```kotlin
@Composable
fun FastWebViewComposable(
    modifier: Modifier = Modifier,
    componentTag: String = "checkout-widget",
    propsJson: String = "{}",
    onMessageReceived: ((String) -> Unit)? = null,
    onBinaryReceived: ((ByteArray) -> Unit)? = null,
    onFastWebViewCreated: ((FastWebView) -> Unit)? = null
)
```

#### Lifecycle & Behavior
1. **Acquisition**: Automatically acquires a `FastWebView` instance from `WebViewWarmupPool` inside `AndroidView.factory`.
2. **Callback Binding**: Binds `onNativeMessageReceived` and `onBinaryMessageReceived` during recompositions.
3. **Component Mounting**: Re-triggers `mountComponent` in `LaunchedEffect` whenever `componentTag` or `propsJson` changes.
4. **Disposal**: Automatically calls `WebViewWarmupPool.releaseFastWebView` inside `DisposableEffect.onDispose`.

---

### `FastWebComponentViewManager`

React Native `SimpleViewManager<FastWebView>` subclass exposing `FastWebView` as a React Native view.

#### Package
`br.com.lucasgoncalves.fastwebview.FastWebComponentViewManager`

#### Class Name in JS
`"FastWebComponentView"`

#### React Properties

- `componentTag`: String prop to specify WebComponent tag name.
- `propsJson`: JSON string prop containing initial or updated props.

#### React Native Commands

- `mountComponent` (Command ID `1`): Accepts `[tag, propsJson]`.
- `updateProps` (Command ID `2`): Accepts `[propsJson]`.
- `sendMessage` (Command ID `3`): Accepts `[messageString]`.

---

## Integration Recipes

### 1. Application Initialization

```kotlin
import android.app.Application
import br.com.lucasgoncalves.fastwebview.WebViewWarmupPool

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Pre-warms engine and loads shell.html into RAM cache
        WebViewWarmupPool.warmUpEngineAndPool(this)
    }
}
```

### 2. Jetpack Compose

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import br.com.lucasgoncalves.fastwebview.FastWebViewComposable

@Composable
fun ProductDetailsScreen(productId: String) {
    FastWebViewComposable(
        modifier = Modifier.fillMaxSize(),
        componentTag = "product-card",
        propsJson = """{"productId": "$productId"}""",
        onMessageReceived = { json ->
            println("Received from JS: $json")
        }
    )
}
```

### 3. Android Views & XML Layouts

```kotlin
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.lucasgoncalves.fastwebview.FastWebView
import br.com.lucasgoncalves.fastwebview.WebViewWarmupPool

class DetailActivity : AppCompatActivity() {

    private lateinit var fastWebView: FastWebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fastWebView = WebViewWarmupPool.acquireFastWebView(this)
        setContentView(fastWebView)

        fastWebView.onNativeMessageReceived = { message ->
            // Process JS message
        }

        fastWebView.mountComponent("user-profile-widget", """{"userId": "usr_12345"}""")
    }

    override fun onDestroy() {
        super.onDestroy()
        WebViewWarmupPool.releaseFastWebView(fastWebView)
    }
}
```

### 4. React Native Bridge

```kotlin
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import br.com.lucasgoncalves.fastwebview.FastWebComponentViewManager

class FastWebViewPackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> = emptyList()

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> =
        listOf(FastWebComponentViewManager())
}
```

---

## Memory Management & Context Re-parenting

A common issue with Android `WebView` pooling is context memory leaks: instantiating a `WebView` with an `Activity` context prevents that Activity from being garbage collected when destroyed.

`fastwebview` resolves this using `MutableContextWrapper`:

1. **Pre-warming**: The pooled `FastWebView` instance is created using `MutableContextWrapper(applicationContext)`.
2. **Acquisition**: Upon calling `acquireFastWebView(activityContext)`, the wrapper's base context is updated:
   ```kotlin
   wrapper.baseContext = activityContext
   ```
3. **Release**: Upon calling `releaseFastWebView(webView)`, the wrapper's base context is restored to the application context:
   ```kotlin
   wrapper.baseContext = webView.context.applicationContext
   ```

This ensures zero `Activity` context leaks while retaining the benefits of pre-instantiated, pre-warmed `WebView` instances.

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
