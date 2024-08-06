package com.example.hex_android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.http.SslError
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.HttpAuthHandler
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.hex_android.DetectConnection.checkInternetConnection


private lateinit var myWebView: WebView
private lateinit var progressBar: ProgressBar
private lateinit var errorPage: String


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
        myWebView.clearCache(false)
        myWebView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

        //Allow zooming in and out
        myWebView.settings.builtInZoomControls = true
        myWebView.settings.displayZoomControls = false

        myWebView.webViewClient = CustomWebViewClient(this)
        CookieManager.getInstance().setAcceptThirdPartyCookies(myWebView, true)


        //Read errorPage.html
        val inputStream = assets.open("errorPage.html")
        val bufferedReader = inputStream.bufferedReader()
        errorPage = bufferedReader.use { it.readText() }
        bufferedReader.close()


        if (!checkInternetConnection(this)) {
            handleError("Lost connection", "Peeves! Router is not a toy!")
        } else {
            myWebView.loadUrl("https://www.hexrpg.com");
        }
    }

    //This does things when back button is pressed
    override fun onBackPressed() {
        if (myWebView.canGoBack()) {

            //Get url where myWebView.goBack() directs you
            var previousUrl: String
            val mWebBackForwardList = myWebView.copyBackForwardList()
            previousUrl = mWebBackForwardList.getItemAtIndex(mWebBackForwardList.currentIndex - 1).url

            //All redirects from * to *, when pressing back button
            val redirects = mapOf(
                "https://www.hexrpg.com/clubs/?action=all" to "https://www.hexrpg.com/clubs/?action=search"
            )

            //Iterate through all things in redirects if there's something that directs somewhere else
            run breaker@ {
                redirects.forEach  { entry ->
                    if (entry.key == previousUrl) {
                        progressBar.visibility = View.VISIBLE
                        myWebView.loadUrl(entry.value)
                        return@breaker
                    }
                }
                progressBar.visibility = View.VISIBLE
                myWebView.goBack()
            }
            return
        }
        super.onBackPressed()
    }
}


//This checks internet connection
object DetectConnection {
    fun checkInternetConnection(context: Context): Boolean {
        val con_manager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return (con_manager.activeNetworkInfo != null && con_manager.activeNetworkInfo!!.isAvailable
                && con_manager.activeNetworkInfo!!.isConnected)
    }
}

private class CustomWebViewClient(val context: Context) : WebViewClient() {

    //This shows and hides loading bar
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        progressBar.visibility = View.VISIBLE
        return super.shouldOverrideUrlLoading(view, request)
    }
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        progressBar.visibility = View.INVISIBLE
    }


    //Getting and forwarding errors below
    override fun onLoadResource(view: WebView, url: String) {
        if (!checkInternetConnection(context)) {
            handleError("Lost connection", "Peeves! Router is not a toy!")
        }
    }
    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
        if (failingUrl!!.contains("hexrpg.com")){
            handleError("Error " + errorCode.toString(), description ?: "Something went wrong...")
        }
    }
    override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
        if (request!!.url.toString().startsWith("https://hexrpg.com")){
            handleError("Error " + errorResponse!!.statusCode.toString(), errorResponse.reasonPhrase)
        }
    }
    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show()
    }
}

//This handles errors
private fun handleError(errorTitle: String, errorSubtitle: String, linkTitle: String = "Return to homepage", link: String = "https://hexrpg.com") {
    val errorVariables = mapOf(
        "errorTitle" to errorTitle,
        "errorSubTitle" to errorSubtitle,
        "linkTitle" to linkTitle,
        "link" to link
    )
    val filledText = fillTemplate(errorPage, errorVariables)
    myWebView.loadDataWithBaseURL("https://hexrpg.com", filledText, "text/html", "utf-8", "https://hexrpg.com")

}

//This fills variables in string
private fun fillTemplate(template: String, variables: Map<String, String>): String {
    var result = template
    for ((key, value) in variables) {
        result = result.replace("\${$key}", value)
    }
    return result
}