package br.com.lucad.mywebviewplayground.fastwebview

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

class FastWebViewTest {

    @Test
    fun testFastWebViewConstants() {
        assertEquals("NativeFastBridge", FastWebView.FAST_BRIDGE_NAME)
        assertEquals("https://appassets.androidplatform.net/assets/shell.html", FastWebView.SHELL_URL)
    }

    @Test
    fun testRamCachedAssetHandlerMimeTypes() {
        val assetHandlerClass = RamCachedAssetHandler::class.java
        assertNotNull(assetHandlerClass)
    }

    @Test
    fun testWebViewWarmupPoolInitialState() {
        assertFalse(WebViewWarmupPool.isEngineWarm)
    }
}
