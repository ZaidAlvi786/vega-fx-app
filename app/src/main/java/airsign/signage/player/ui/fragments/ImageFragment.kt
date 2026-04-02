package airsign.signage.player.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide.with
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import airsign.signage.player.R
import airsign.signage.player.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import java.io.File

@AndroidEntryPoint
class ImageFragment : BaseFragment() {
    private var mImageView: ImageView? = null

    private val TAG = "ImageFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.image_fragment, container, false)
        mImageView = view.findViewById(R.id.imageView)

        viewLifecycleOwner.lifecycleScope.launch {
            media?.let { item ->
                val fileName = item.filename
                val isCached = fileName?.let { mFileManager.isCached(it) } ?: false
                Log.d(TAG, "Image caching status: $isCached for $fileName")

                val path = if (isCached && fileName != null) {
                    mFileManager.getFilePath(fileName)
                } else {
                    item.url
                }
                
                withContext(Dispatchers.Main) { 
                    loadImage(path) 
                }
            }
        }

        return view
    }

    override fun onDestroyView() {
        try {
            if (activity != null) with(requireActivity()).clear(mImageView!!)
        } catch (e: Exception) {
            Log.e(TAG, "onDestroyView: ", e)
        }

        super.onDestroyView()
    }

    private fun loadImage(path: String) {
        if (activity == null) return
        Log.d(TAG, "path $path")
        val context = requireActivity().applicationContext
        try {
            val file = File(path)
            
            // Hardened Graphics chain for low-end Firesticks
            val hardenedOptions = RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565) // Save 50% RAM
                .override(1920, 1080)              // Cap resolution to 1080p
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

            if (file.exists()) {
                val opts = hardenedOptions.signature(ObjectKey(file.lastModified().toString()))
                with(context).load(file).apply(opts).into(mImageView!!)
            } else {
                with(context).load(path).apply(hardenedOptions).into(mImageView!!)
            }
        } catch (e: Exception) {
            playlistController?.nextMedia()
        }
    }
}