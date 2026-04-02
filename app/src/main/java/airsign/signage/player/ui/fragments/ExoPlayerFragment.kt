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
import javax.inject.Inject
import androidx.media3.common.util.UnstableApi
import airsign.signage.player.player.cache.MediaCacheManager
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ExoPlayerFragment : BaseFragment(), Player.Listener {
    private var mExoPlayer: ExoPlayer? = null
    private var bufferingStartMs: Long = 0L

    @Inject
    lateinit var cacheManager: MediaCacheManager

    private lateinit var dataSourceFactory: DataSource.Factory

    init {
        useTimer = false
    }

    @UnstableApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.vlc_player_fragment, container, false)
        val mPlayerView: PlayerView = view.findViewById(R.id.surface)
        
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        val defaultDataSourceFactory = DefaultDataSource.Factory(inflater.context, httpDataSourceFactory)
        
        dataSourceFactory = CacheDataSource.Factory()
            .setCache(cacheManager.simpleCache)
            .setUpstreamDataSourceFactory(defaultDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        // TUNE: Minimizing buffer to save RAM on low-end devices
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                15000, // minBufferMs
                30000, // maxBufferMs
                1500,  // bufferForPlaybackMs
                2500   // bufferForPlaybackAfterRebufferMs
            )
            .build()

        mExoPlayer = ExoPlayer.Builder(inflater.context)
            .setMediaSourceFactory(androidx.media3.exoplayer.source.DefaultMediaSourceFactory(dataSourceFactory))
            .setLoadControl(loadControl)
            .build()

        mPlayerView.player = mExoPlayer
        mPlayerView.useController = false

        return view
    }

    override fun onResume() {
        super.onResume()
        if (mExoPlayer == null) {
            // Re-init player if it was released during onPause
            // (Standard fragment logic handles this via onCreateView)
        }
        createPlayer()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: releasing hardware decoders immediately")
        mExoPlayer?.pause()
        // Aggressive memory release for signage: free decoder resources but keep object if possible
        // Actually, on low-end devices, full release is safer
        releasePlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
    }

    private fun releasePlayer() {
        mExoPlayer?.removeListener(this)
        mExoPlayer?.release()
        mExoPlayer = null
    }

    @UnstableApi
    private fun createPlayer() {
        val player = mExoPlayer ?: return
        Log.d(TAG, "createPlayer: initializing media pipeline")

        if (media == null) {
            playlistController?.nextMedia()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            media?.let { item ->
                val fileName = item.filename
                val isCached = fileName?.let { mFileManager.isCached(it) } ?: false
                
                val uri = if (isCached && fileName != null) {
                    android.net.Uri.fromFile(java.io.File(mFileManager.getFilePath(fileName)))
                } else {
                    android.net.Uri.parse(item.url)
                }

                withContext(Dispatchers.Main) {
                    player.setMediaItem(androidx.media3.common.MediaItem.fromUri(uri))
                    player.addListener(this@ExoPlayerFragment)
                    player.prepare()
                    player.play()
                }
            }
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        Log.e(TAG, "Playback error: ${error.errorCodeName} for ${media?.url}", error)
        playlistController?.nextMedia()
    }

    override fun onPlaybackStateChanged(state: Int) {
        when (state) {
            Player.STATE_IDLE -> {
                Log.d(TAG, "Player State: IDLE")
            }
            Player.STATE_BUFFERING -> {
                Log.d(TAG, "Player State: BUFFERING")
                if (bufferingStartMs == 0L) bufferingStartMs = System.currentTimeMillis()
                
                // Stuck detection: If buffering for > 20s, skip
                if (System.currentTimeMillis() - bufferingStartMs > 20000) {
                    Log.w(TAG, "Buffering timeout reached - skipping media")
                    playlistController?.nextMedia()
                }
            }
            Player.STATE_READY -> {
                Log.d(TAG, "Player State: READY")
                bufferingStartMs = 0L
            }
            Player.STATE_ENDED -> {
                Log.d(TAG, "Player State: ENDED - progressing to next media")
                playlistController?.nextMedia()
            }
        }
    }

    companion object {
        val TAG = ExoPlayerFragment::class.java.simpleName
    }
}