package com.example.hex_android

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import com.example.hex_android.DetectConnection.checkInternetConnection


private lateinit var myWebView: WebView
private lateinit var mainContext: Context


var errorPage: String = """<head>
    <title>offline</title>
    <!-- meta tags -->
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    
    <!-- import font -->
    
    <!-- (will be cached automatically for the offline page) -->
    
    <link rel="preconnect" href="https://fonts.googleapis.com" />
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400&display=swap" rel="stylesheet"/>


    <style>    
      
      body {
      display: grid;
      place-items: center;
      height: 100vh;
      font-family: "Montserrat", sans-serif;
      font-size: 1em;
      background: #000000;
      }        
      #brand {
      text-align: center;
      line-height: 50%;
      color:#e3de9d;
      letter-spacing:3px;
      text-shadow:0px 1px 2px rgba(132,132,132,0.89);
      font-size: 40px;
      } 
      p {
      text-align: center;
      color:#e3de9d;
      letter-spacing:2px;
      font-size: 27px;

      }
      a {
      text-align: center;
      color:#e3de9d;
      letter-spacing:3px;
      }
    
    </style>
  </head>
  <body>
    <center>
      <span id="brand">Error 106</span>
      <p>No internet connection!</p>
      <a href="https://hexrpg.com">
      Try again
      </a>
    </center>
  </body>"""

class Hex_web : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hex_web)
        window.decorView.setBackgroundColor(Color.BLACK)

        myWebView = findViewById(R.id.webview_box)
        myWebView.setBackgroundColor(Color.TRANSPARENT)
        myWebView.settings.javaScriptEnabled = true
        myWebView.settings.domStorageEnabled = true

        //Allow zooming in and out
        myWebView.settings.builtInZoomControls = true
        myWebView.settings.displayZoomControls = false

        myWebView.webViewClient = CustomWebViewClient()
        CookieManager.getInstance().setAcceptThirdPartyCookies(myWebView, true)

        mainContext = this
        if (!DetectConnection.checkInternetConnection(this)) {
            myWebView.loadDataWithBaseURL("https://hexrpg.com", errorPage, "text/html", "utf-8", "https://hexrpg.com")

        } else {
            myWebView.loadUrl("https://www.hexrpg.com");
        }
    }

    override fun onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack()
            return
        }
        super.onBackPressed()
    }
}

object DetectConnection {
    fun checkInternetConnection(context: Context): Boolean {
        val con_manager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return (con_manager.activeNetworkInfo != null && con_manager.activeNetworkInfo!!.isAvailable
                && con_manager.activeNetworkInfo!!.isConnected)
    }
}

// Function to load all URLs in same webview
private class CustomWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        view.loadUrl(url)

        return true
    }

    override fun onLoadResource(view: WebView, url: String) {
        if (!checkInternetConnection(mainContext)) {
            myWebView.loadDataWithBaseURL("https://hexrpg.com", errorPage, "text/html", "utf-8", "https://hexrpg.com")
        }
    }
}