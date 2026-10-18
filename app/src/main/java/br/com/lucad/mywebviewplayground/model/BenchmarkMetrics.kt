package br.com.lucad.mywebviewplayground.model

/**
 * Performance metrics captured across WebView lifecycle, JS bridge interop, system memory, and cold start tracking.
 */
data class BenchmarkMetrics(
    val loadStartTimeMs: Long = 0L,
    val domContentLoadedTimeMs: Long = 0L,
    val pageLoadFinishedTimeMs: Long = 0L,
    val totalLoadTimeMs: Long = 0L,
    val jsBridgeLatencyMs: Long = 0L,
    val jsBridgeMessageCount: Long = 0L,
    val usedMemoryMb: Double = 0.0,
    val totalMemoryMb: Double = 0.0,
    val componentRenderTimeMs: Double = 0.0,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis(),

    // Cold Start & Pre-warm Metrics
    val navigationClickTimeMs: Long = 0L,
    val webViewCreatedTimeMs: Long = 0L,
    val timeToWebViewCreatedMs: Long = 0L,
    val timeToPageStartedMs: Long = 0L,
    val timeToPageFinishedMs: Long = 0L,
    val isPrewarmed: Boolean = false,
    val coldStartRecorded: Boolean = false
)

