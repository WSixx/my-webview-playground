package br.com.lucad.mywebviewplayground

import android.app.Application
import br.com.lucad.mywebviewplayground.fastwebview.WebViewWarmupPool

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        WebViewWarmupPool.warmUpEngineAndPool(this)
    }
}
