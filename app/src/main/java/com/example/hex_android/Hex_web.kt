package com.example.hex_android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.http.SslError
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import com.example.hex_android.DetectConnection.checkInternetConnection


private lateinit var myWebView: WebView
private lateinit var mainContext: Context
private lateinit var progressBar: ProgressBar


var errorPage: String = """
    <head>
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
      <span id="brand">${"$"}{errorTitle}</span>
      <p>${"$"}{errorSubTitle}</p>
      <a href="https://hexrpg.com">
      Return to homepage
      </a>
    </center>
  </body>"""


class Hex_web : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hex_web)
        window.decorView.setBackgroundColor(Color.BLACK)

        progressBar = findViewById(R.id.progressBar)
        myWebView = findViewById(R.id.webview_box)
        myWebView.setBackgroundColor(Color.TRANSPARENT)
        myWebView.settings.javaScriptEnabled = true
        myWebView.settings.domStorageEnabled = true
        myWebView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        //Allow zooming in and out
        myWebView.settings.builtInZoomControls = true
        myWebView.settings.displayZoomControls = false

        myWebView.webViewClient = CustomWebViewClient()
        CookieManager.getInstance().setAcceptThirdPartyCookies(myWebView, true)

        mainContext = this
        if (!DetectConnection.checkInternetConnection(this)) {
            handleError("No internet", "PEEVES!!! Router is not a toy!!!")

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
        progressBar.visibility = View.VISIBLE
        view.loadUrl(url)
        return true
    }

    override fun onLoadResource(view: WebView, url: String) {
        if (!checkInternetConnection(mainContext)) {
            handleError("No internet", "PEEVES!!! Router is not a toy!!!")
        }
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        progressBar.visibility = View.INVISIBLE
    }

    override fun onReceivedError(
        view: WebView?,
        errorCode: Int,
        description: String?,
        failingUrl: String?
    ) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        if (failingUrl!!.contains("hexrpg.com")){
            handleError("Error " + errorCode.toString(), description ?: "Something went wrong...")
        }
    }
    override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
        super.onReceivedHttpError(view, request, errorResponse)
        handleError("Error " + errorResponse!!.statusCode.toString(), errorResponse.reasonPhrase)
    }

    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        super.onReceivedSslError(view, handler, error)
        handleError("Error " + error, "Don't be so stupid, SSl!")
    }
}

//This VVV actually handles all errors...
private fun handleError(errorTitle: String, errorSubtitle: String) {
    val errorVariables = mapOf(
        "errorTitle" to errorTitle,
        "errorSubTitle" to errorSubtitle
    )
    val filledText = fillTemplate(errorPage, errorVariables)
    myWebView.loadDataWithBaseURL("https://hexrpg.com", filledText, "text/html", "utf-8", "https://hexrpg.com")

}

private fun fillTemplate(template: String, variables: Map<String, String>): String {
    var result = template
    for ((key, value) in variables) {
        result = result.replace("\${$key}", value)
    }
    return result
}