# Project Plan

Cria um app simples e também um mock de uma webview react nativo webcomponent. O objetivo é ter esse mock e uma UI simples para fazer analises, testes e benchmarks de webview.

## Project Brief

# Project Brief: WebView Playground & Benchmark App

## Overview
A lightweight Android application designed to host and test WebViews with React/WebComponent mocks. The app enables developers to perform performance analysis, benchmarking, and JS-native interop testing in a clean, configurable environment.

## Features
- **Embedded Mock & Custom URL Launcher**: Load bundled HTML/JS mock web components (simulating React/WebComponent apps) or enter external URLs into an embedded WebView.
- **Real-Time Performance & Benchmark Overlay**: Measure and display key performance indicators including Page Load Time, DOMContentLoaded timing, and memory consumption.
- **JavaScript Bridge Console**: Interactive panel to initiate, observe, and log bidirectional communications between native Kotlin code and JavaScript/WebComponents.
- **Dynamic WebView Configuration**: On-the-fly toggles for WebView settings (JavaScript execution, hardware acceleration, DOM storage, and cache modes) to analyze runtime behaviors under varying conditions.

## High-Level Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Navigation & Adaptive Strategy**: Jetpack Navigation 3 (state-driven navigation) and Compose Material Adaptive (`androidx.compose.material3.adaptive`) for responsive layouts
- **Concurrency & State Management**: Kotlin Coroutines & StateFlow
- **WebView Integration**: AndroidX WebKit (`androidx.webkit`)

## Implementation Steps
**Total Duration:** 29m 26s

### Task_1_SetupDependenciesAndMockAssets: Add androidx.webkit dependency and create bundled HTML/JS mock React/WebComponent asset in app/src/main/assets.
- **Status:** COMPLETED
- **Updates:** Configured androidx.webkit dependency in build.gradle.kts and libs.versions.toml. Created mock_webcomponent.html in assets with WebComponent/React mock, interactive controls, bridge message handler, and performance timing scripts. Build verified successfully.
- **Acceptance Criteria:**
  - androidx.webkit dependency configured
  - mock_webcomponent.html created in assets folder with JS bridge mock
  - build pass
- **Duration:** 4m 17s

### Task_2_CoreWebViewAndBridge: Implement configurable WebView setup with WebChromeClient/WebViewClient timing metrics, JS bridge interop, and settings management.
- **Status:** COMPLETED
- **Updates:** Implemented WebView settings management (WebViewSettingsState & WebViewSettingsManager), CustomWebViewClient & CustomWebChromeClient with timing & memory benchmarks, NativeBridge @JavascriptInterface with StateFlow streams, and safe evaluateJavascript extensions. Added unit tests and verified build.
- **Acceptance Criteria:**
  - WebView client with load timing and DOM metrics callbacks created
  - JavascriptInterface bridge for native-JS interop implemented
  - build pass
- **Duration:** 4m 11s

### Task_3_ComposeUIAndBenchmarkOverlay: Build Jetpack Compose screens for URL launcher, WebView configuration controls, JS bridge console, and real-time benchmark overlay.
- **Status:** COMPLETED
- **Updates:** Implemented Compose MainScreen with UrlLauncherBar, WebViewViewport, BenchmarkOverlayCard, JsBridgeConsolePanel, and WebViewConfigPanel. Fully integrated with MainViewModel and MainActivity. Unit tests passed.
- **Acceptance Criteria:**
  - Compose UI with URL input, WebView host, settings toggles, JS bridge console, and benchmark metrics overlay created
  - Bundled mock and external URLs load properly
  - build pass
  - app does not crash
- **Duration:** 4m 9s

### Task_4_RunAndVerify: Run and verify application stability, confirm all user features are working, and perform final sanity check. Critic agent verifies application stability (no crashes), confirms alignment with user requirements, and reports critical UI issues.
- **Status:** COMPLETED
- **Updates:** Verified app on device/emulator with critic_agent. App is stable, no crashes. Verified mock React WebComponent page loading, URL launcher, real-time benchmark overlay, JS bridge interop console, and native WebView configuration toggles. Fixed edge-to-edge status bar insets. All verification checks passed.
- **Acceptance Criteria:**
  - project builds successfully
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - critic_agent verified stability and user requirement alignment
- **Duration:** 9m 28s

### Task_5_NativeHomeScreenAndColdStartMetrics: Implement native HomeScreen launch screen with pre-warm toggle, navigation to WebView screen, and WebView cold start latency tracking.
- **Status:** COMPLETED
- **Updates:** Implemented HomeScreen starting screen, Navigation 3 flow between HomeScreen and WebViewScreen, WebViewPrewarmer for background pre-warming experiments, and Cold Start latency metrics tracking from tap to page load completion. Unit tests passed.
- **Acceptance Criteria:**
  - Native HomeScreen set as default starting screen with pre-warm toggle and launcher button
  - Navigation flow between HomeScreen and WebView Screen created
  - Cold start latency measured from tap to onPageStarted/onPageFinished
  - build pass
- **Duration:** 7m 21s

### Task_6_RunAndVerify: Run and verify application stability, confirm native home screen launch, cold start benchmark metrics, and pre-warm feature. Instruct critic_agent to verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - project builds successfully
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - critic_agent verified stability and user requirement alignment
- **StartTime:** 2026-10-18 16:57:38 GMT-03:00

