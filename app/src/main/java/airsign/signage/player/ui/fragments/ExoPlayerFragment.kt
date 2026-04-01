package airsign.signage.player.ui.fragments

import airsign.signage.player.R
import airsign.signage.player.ui.base.BaseFragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.MediaItem.fromUri
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ExoPlayerFragment : BaseFragment(), Player.Listener {
    private lateinit var mExoPlayer: ExoPlayer
    private val asyncScope = CoroutineScope(Job() + Dispatchers.IO)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.vlc_player_fragment, container, false)
        val mPlayerView: PlayerView = view.findViewById(R.id.surface)
        mExoPlayer = ExoPlayer.Builder(inflater.context).build()

        mPlayerView.player = mExoPlayer
        mPlayerView.useController = false

        return view
    }

    override fun onResume() {
        super.onResume()
        createPlayer()
    }

    override fun onStop() {
        super.onStop()
        mExoPlayer.release()
        mExoPlayer.clearVideoSurface()
        mExoPlayer.removeListener(this)
    }

    private fun createPlayer() {
        Log.d(TAG, "Calling createPlayer method ---")

        if (media == null) {
            playlistController?.nextMedia()
            return
        }

        asyncScope.launch {
            media?.let {
                it.filename?.let { fileName->
                    val isCached = mFileManager.isCached(fileName)
                    Log.d(TAG, "isCached $isCached")

                    val mediaItem = if (isCached) fromUri(mFileManager.getFilePath(fileName))
                    else fromUri(it.url)

                    withContext(Dispatchers.Main) {
                        mExoPlayer.setMediaItem(mediaItem)
                        mExoPlayer.addListener(this@ExoPlayerFragment)

                        mExoPlayer.prepare()
                        mExoPlayer.play()
                    }
                }
            }


        }
    }

    override fun onPlayerError(error: PlaybackException) {
        if (error.errorCode == PlaybackException.ERROR_CODE_FAILED_RUNTIME_CHECK) {
            Log.e(TAG, "ERROR_CODE_FAILED_RUNTIME_CHECK --- > " + media?.url)
        } else {
            Log.e(TAG, "play next media --- > " + media?.url)
        }

        playlistController?.nextMedia()
    }

    override fun onPlaybackStateChanged(state: Int) {
        when (state) {
            Player.STATE_IDLE -> {
                playlistController!!.nextMedia()
                Log.d(TAG, "STATE_IDLE: ")
            }
            //This is the initial state, the state when the player is stopped, and when playback failed.
            Player.STATE_READY ->                 //The player is able to immediately play from its current position.
                Log.d(TAG, "STATE_READY: ")
            Player.STATE_BUFFERING -> Log.d(TAG, "STATE_BUFFERING: ")
            Player.STATE_ENDED -> {
                Log.d(TAG, "STATE_ENDED: ")
                mExoPlayer.seekTo(0)
            }
        }
    }

    companion object {
        val TAG = ExoPlayerFragment::class.java.simpleName
    }
}