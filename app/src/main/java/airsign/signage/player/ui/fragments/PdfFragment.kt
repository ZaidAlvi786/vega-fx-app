package airsign.signage.player.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.rajat.pdfviewer.HeaderData
import com.rajat.pdfviewer.PdfRendererView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import airsign.signage.player.R
import airsign.signage.player.ui.base.BaseFragment
import java.io.File

class PdfFragment : BaseFragment() {
    private val TAG = "PdfFragment"
    private val asyncScope = CoroutineScope(Job() + Dispatchers.IO)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.pdf_fragment, container, false)
        val pdfView = view.findViewById<PdfRendererView>(R.id.pdfView)

        try {
            asyncScope.launch {
                media?.let {
                    it.filename?.let { fileName->
                        val isCached = mFileManager.isCached(fileName)
                        Log.d(ExoPlayerFragment.TAG, "isCached $isCached")

                        if (isCached) {
                            withContext(Dispatchers.Main) {
                                pdfView.initWithFile(File(mFileManager.getFilePath(fileName)))
                            }
                            Log.d(TAG, "loading cached pdf")
                        } else {
                            withContext(Dispatchers.Main) {
                                Log.d(TAG, "pdf url ${it.url}")
                                pdfView.initWithUrl(
                                    it.url, HeaderData(),
                                    requireActivity().lifecycleScope, requireActivity().lifecycle
                                )
                            }

                            Log.d(TAG, "loading live pdf")
                        }

                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "pdf exception ")
        }

        return view
    }
}