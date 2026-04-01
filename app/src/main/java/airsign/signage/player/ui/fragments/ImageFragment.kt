package airsign.signage.player.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide.with
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import airsign.signage.player.R
import airsign.signage.player.ui.base.BaseFragment

@AndroidEntryPoint
class ImageFragment : BaseFragment() {
    private var mImageView: ImageView? = null

    private val TAG = "ImageFragment"

    private val asyncScope = CoroutineScope(Job() + Dispatchers.IO)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.image_fragment, container, false)
        mImageView = view.findViewById(R.id.imageView)


        asyncScope.launch {
            media?.let {
                it.filename?.let {fileName->
                    val isCached = mFileManager.isCached(fileName)
                    Log.d(TAG, "isFileCached $isCached")

                    val path = if (isCached) mFileManager.getFilePath(fileName) else it.url
                    withContext(Dispatchers.Main) { loadImage(path) }
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
            if (file.exists()) {
                // Signature from file lastModified so when file is replaced (updated content),
                // Glide cache invalidates and new image is loaded instead of old cached one
                val opts = RequestOptions().signature(ObjectKey(file.lastModified().toString()))
                with(context).load(file).apply(opts).into(mImageView!!)
            } else {
                with(context).load(path).into(mImageView!!)
            }
        } catch (e: Exception) {
            playlistController?.nextMedia()
        }
    }
}