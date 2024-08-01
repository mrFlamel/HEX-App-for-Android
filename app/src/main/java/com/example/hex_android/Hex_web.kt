package com.example.hex_android

import android.graphics.Color
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity

class Hex_web : ComponentActivity() {
    private lateinit var myWebView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hex_web)
        window.decorView.setBackgroundColor(Color.BLACK)

        myWebView = findViewById(R.id.webview_box)
        myWebView.setBackgroundColor(Color.TRANSPARENT)
        myWebView.settings.javaScriptEnabled = true
        myWebView.settings.domStorageEnabled = true
        myWebView.settings.builtInZoomControls = true
        myWebView.settings.displayZoomControls = false
        myWebView.webViewClient = WebViewClient()
        CookieManager.getInstance().setAcceptThirdPartyCookies(myWebView, true)
        myWebView.loadUrl("https://hexrpg.com")
    }

    override fun onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack()
            return
        }
        super.onBackPressed()
    }
}
