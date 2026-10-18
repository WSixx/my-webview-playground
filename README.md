# My Webview Playground

An Android application demonstrating advanced `WebView` integration, bi-directional JavaScript bridging, benchmarking, pre-warming, and modern Jetpack Compose UI architecture.

## Project Architecture & Build Logic

The project uses modern Gradle build conventions with a composite `build-logic` setup and organized Version Catalog (`gradle/libs.versions.toml`).

### Convention Plugins (`build-logic`)

- **`mywebviewplayground.android.application`**: Configures Android Application DSL defaults (SDK versions `compileSdk 37`, `minSdk 28`, `targetSdk 37`, Java 21 compatibility, test runners).
- **`mywebviewplayground.android.compose`**: Configures Jetpack Compose build features and dependencies.

### Dependency Catalog (`gradle/libs.versions.toml`)

Dependencies are organized into clean logical categories:
- `# AndroidX Core & Lifecycle` - Core KTX, Coroutines, Lifecycle runtime and ViewModel composables.
- `# Jetpack Compose & UI` - Activity Compose, Compose BOM, Material 3, Icons.
- `# Navigation` - Jetpack Navigation 3 runtime and UI, Kotlinx Serialization.
- `# WebKit` - AndroidX WebKit for extended WebView features.
- `# Testing & Quality` - JUnit, Coroutines Test, Espresso, Compose UI Testing.

---

## Mock React / WebComponent Bundle Architecture

In `My Webview Playground`, the local asset `mock_webcomponent.html` (`app/src/main/assets/mock_webcomponent.html`) serves as a self-contained representation of a compiled React or WebComponent production bundle.

### Bundled Local Loading vs. Remote Server Loading

| Feature / Metric | Bundled Local Loading (`file:///android_asset/...` or `http://appassets.androidlocal/...`) | Remote Server Loading (`https://...`) |
| :--- | :--- | :--- |
| **Availability & Offline** | **100% Offline Compatible**: Guaranteed availability without network connectivity or cellular signal. | **Network Dependent**: Requires active internet connection unless complex Service Worker / PWA caching is configured. |
| **Latency & Load Speed** | **Zero Network Overhead**: Immediate file I/O from APK storage, yielding DOM-ready times typically under 50ms. | **Network Overhead**: Subject to DNS resolution, TLS handshake, and network RTT, leading to slower initial paint times. |
| **Security & Integrity** | **High Integrity**: Assets are signed as part of the APK bundle. Tamper-resistant against MITM or remote script injection. | **Dynamic / Content Risks**: Allows real-time remote updates, but requires domain verification, CSP headers, and CORS handling. |
| **JS Bridge Interaction** | **Instant Bridge Readiness**: Exposes `@JavascriptInterface` or `postMessage` channels immediately upon page initialization. | **Delayed Bridge Setup**: Exposing native interfaces over remote URLs requires strict domain allowlisting and origin validation. |
| **Updates & Lifecycle** | **App Release Bound**: Updating the frontend bundle requires an APK/AAB app update or OTA asset bundle sync. | **Instant Deployment**: Pushing web content updates to the server reflects immediately across all client apps without app updates. |

---

## Building and Running

- **Build Debug APK**: `./gradlew assembleDebug`
- **Run Unit Tests**: `./gradlew test`
