package airsign.signage.player.ui.base

import airsign.signage.player.data.MediaPlayer
import airsign.signage.player.data.downloader.FileManager
import airsign.signage.player.data.utils.BasePref
import airsign.signage.player.domain.IPlaylistController
import airsign.signage.player.domain.model.Content
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseFragment : Fragment(), Runnable {
    private val TAG = "BaseFragment"
    private val mHandler = Handler()

    @Inject
    lateinit var mMediaPlayer: MediaPlayer

    @Inject
    lateinit var mPref: BasePref

    @Inject
    lateinit var mFileManager: FileManager

    var media: Content? = null
    var playlistController: IPlaylistController? = null

    /**
     * If true, the fragment will automatically advance to the next media after [Content.duration].
     * Fragments that manage their own completion (like ExoPlayerFragment) should set this to false.
     */
    protected var useTimer = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            media = requireArguments()[EXTRA_MEDIA] as Content?
            Log.d(TAG, "Media Received :$media")
        }
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "ViewCreated :")
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        if (!useTimer) return

        media?.let {
            if (mMediaPlayer.mediaList.size <= 1) {
                Log.d(TAG, "Single media playlist - navigation timer disabled")
            } else {
                it.duration?.let { duration ->
                    val milliseconds = duration * 1000
                    mHandler.postDelayed(this, milliseconds.toLong())
                    Log.d(TAG, "Navigation timer scheduled for $milliseconds ms")
                }
            }
        }
    }

    @CallSuper
    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacks(this)

    }

    fun resetTime() {
        if (!useTimer) return
        // Handle both String and Int duration formats from different CMS versions
        media?.let {
            val milliseconds = (it.duration ?: 30) * 1000
            mHandler.removeCallbacks(this)
            mHandler.postDelayed(this, milliseconds.toLong())
        }
    }

    fun addPlaylistController(controller: IPlaylistController?) {
        playlistController = controller
    }

    override fun run() {
        playlistController?.nextMedia()
    }

    companion object {
        const val EXTRA_MEDIA = "extra_media"
    }


}