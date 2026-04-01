package airsign.signage.player.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import airsign.signage.player.R
import airsign.signage.player.ui.base.BaseFragment

class WebViewFragment : BaseFragment() {
    private var mWebView: WebView? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_webview, container, false)
        mWebView = v.findViewById<View>(R.id.web_view) as WebView


        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUrl()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadUrl() {
        mWebView?.let { webView ->
            webView.setInitialScale(0)
            val webSettings = mWebView!!.settings
            webSettings.mediaPlaybackRequiresUserGesture = false
            webView.webChromeClient = WebChromeClient()

            // Only enable JS for the loaded page; keep other surfaces closed
            webSettings.javaScriptEnabled = true
            webSettings.loadWithOverviewMode = true
            webSettings.builtInZoomControls = false
            webSettings.displayZoomControls = false
            webSettings.domStorageEnabled = true
            webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            webSettings.allowFileAccess = false
            webSettings.allowContentAccess = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                webSettings.allowFileAccessFromFileURLs = false
                webSettings.allowUniversalAccessFromFileURLs = false
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                webSettings.safeBrowsingEnabled = true
            }

            webView.isVerticalScrollBarEnabled = false
            webView.isHorizontalScrollBarEnabled = false

            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url.isNullOrBlank()) return true
                    return if (url.startsWith("https://")) {
                        view?.loadUrl(url)
                        true
                    } else {
                        true // block non-https schemes
                    }
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val target = request?.url?.toString() ?: return true
                    return if (target.startsWith("https://")) {
                        view?.loadUrl(target)
                        true
                    } else {
                        true // block non-https schemes
                    }
                }

                override fun onPageFinished(view: WebView, url: String) = resetTime()
            }

            media?.let { webView.loadUrl(it.url) }
        }

    }

    companion object {
        private val TAG = WebViewFragment::class.java.simpleName
    }
}
