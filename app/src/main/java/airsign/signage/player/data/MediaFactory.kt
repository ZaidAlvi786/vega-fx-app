package airsign.signage.player.data

import airsign.signage.player.domain.model.Content
import airsign.signage.player.domain.utils.WebviewUtils
import airsign.signage.player.ui.base.BaseFragment
import airsign.signage.player.ui.fragments.ExoPlayerFragment
import airsign.signage.player.ui.fragments.ImageFragment
import airsign.signage.player.ui.fragments.PdfFragment
import airsign.signage.player.ui.fragments.WebViewFragment
import android.os.Bundle
import android.util.Log

object MediaFactory {
    const val IMAGE = "image"
    const val VIDEO = "video"
    const val URL = "url"
    const val PDF = "document"
    const val APP = "app"

    fun create(media: Content): BaseFragment {
        Log.d("MediaFactory", "create: $media")
        
        val fragment: BaseFragment = when (media.type) {
            IMAGE -> ImageFragment()
            VIDEO -> ExoPlayerFragment()
            PDF -> PdfFragment()
            APP -> {
                when {
                    WebviewUtils.isVimeoUrl(media.url) -> {
                        Log.d("MediaFactory", "Detected Vimeo URL, falling back to WebView fragment")
                        WebViewFragment()
                    }
                    else -> {
                        Log.d("MediaFactory", "Non-YouTube/Vimeo app, loading WebView fragment")
                        WebViewFragment()
                    }
                }
            }
            URL -> {
                // Check if it's a YouTube or Vimeo URL
                when {
                    WebviewUtils.isVimeoUrl(media.url) -> {
                        Log.d("MediaFactory", "Detected Vimeo URL, falling back to WebView fragment")
                        WebViewFragment()
                    }
                    else -> {
                        Log.d("MediaFactory", "Non-YouTube/Vimeo URL, loading WebView fragment")
                        WebViewFragment()
                    }
                }
            }
            else -> {
                when {
                    WebviewUtils.isVimeoUrl(media.url) -> {
                        Log.d("MediaFactory", "Detected Vimeo URL for unknown type, falling back to WebView fragment")
                        WebViewFragment()
                    }
                    else -> {
                        Log.d("MediaFactory", "Unknown type, loading WebView fragment")
                        WebViewFragment()
                    }
                }
            }
        }
        
        val bundle = Bundle()
        bundle.putSerializable(BaseFragment.EXTRA_MEDIA, media)
        fragment.arguments = bundle
        return fragment
    }


}