package airsign.signage.player.ui.main.viewmodel

import airsign.signage.player.R
import airsign.signage.player.R.string.connecting_to_server_update
import airsign.signage.player.data.MediaPlayer
import airsign.signage.player.data.downloader.FileManager
import airsign.signage.player.data.downloader.WorkContractor
import airsign.signage.player.data.remote.ApiResult
import airsign.signage.player.data.remote.PlaylistResponse
import airsign.signage.player.data.repository.DeviceRepositoryImpl.Companion.ENDPOINT_CURRENT_PLAYLIST
import airsign.signage.player.data.utils.DeviceInfoUtils
import airsign.signage.player.domain.model.Content
import airsign.signage.player.domain.model.DeviceModel
import airsign.signage.player.domain.repository.DeviceRepository
import airsign.signage.player.domain.model.PairingSession
import airsign.signage.player.device.DeviceManager
import airsign.signage.player.domain.model.DeviceState
import com.google.gson.Gson
import airsign.signage.player.ui.main.viewmodel.MainViewModel.ViewState.DisplayMessage
import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.Instant
import java.time.OffsetDateTime

@HiltViewModel
class MainViewModel @Inject constructor(
    private val application: Application,
    private val mediaPlayer: MediaPlayer,
    private val deviceRepository: DeviceRepository,
    private val deviceManager: DeviceManager,
    private val runtimeController: DeviceRuntimeController,
    private val deviceInfoUtils: DeviceInfoUtils
) : ViewModel() {

    private val TAG = "MainViewModel"
    private val _viewState = MutableLiveData<ViewState>()
    val viewState: LiveData<ViewState> = _viewState

    private var mFileManager: FileManager? = null
    private var currentDeviceId: String? = null
    private var unauthorizedRetryJob: Job? = null
    private var lastUnauthorizedAtMs: Long = 0L
    private val gson = Gson()

    init {
        mFileManager = FileManager(application)
    }
    fun onAppResumed() {
        viewModelScope.launch {
            deviceManager.deviceState.collect { state ->
                when (state) {
                    is DeviceState.Unregistered -> {
                        Log.d(TAG, "starting registration ....!!!")
                        deviceManager.startRegistrationFlow(this)
                    }
                    is DeviceState.Pending -> {
                        _viewState.postValue(ViewState.DisplayCode(state.pairingCode))
                    }
                    is DeviceState.Linked -> {
                        postScreenDetailsOnPairing(state.deviceId)
                    }
                    is DeviceState.Active -> {
                        currentDeviceId = deviceRepository.getPersistedDeviceId()
                        if (currentDeviceId != null) {
                            Log.d(TAG, "starting run time ....!!")
                            startRuntime(currentDeviceId!!)
                            fetchPlaylist(deviceId = currentDeviceId!!)
                        }
                    }
                }
            }
        }
    }

    fun startSignage() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "startSignage: resolving latest device state")
                val device = mediaPlayer.getDevice(deviceRepository)
                verifyDeviceState(device)
            } catch (e: Exception) {
                Log.e(TAG, "startSignage failed", e)
                displayMessage(application.getString(connecting_to_server_update), false)
            }
        }
    }

    private fun verifyDeviceState(device: DeviceModel?) {
        Log.d(TAG, "verifyDeviceState: $device")

        if (device == null) {
            val message = application.getString(connecting_to_server_update)
            displayMessage(message, false)
            return
        }

        val pairing = device.paringResponse
        val playlistResponse = device.playlist
        val hasPersistedDeviceId = deviceRepository.getPersistedDeviceId() != null

        if (pairing?.paired != true && !hasPersistedDeviceId) {
            Log.d(TAG, "Device is not paired please display paring code ${device.code}")
            device.code?.let {
                _viewState.postValue(ViewState.DisplayCode(it))
            }
            return
        }
        if (pairing?.paired != true && hasPersistedDeviceId) {
            Log.d(TAG, "Device has persisted id, treating as paired for playlist check")
        }

        Log.d(TAG, "device is already paired ...!!")

        if (playlistResponse == null) {
            val message = application.getString(R.string.add_playlist_label)
            displayMessage(message, true)
            return
        }

        val mediaItems = playlistResponse.mediaItems

        if (mediaItems.isEmpty()) {
            val message = application.getString(R.string.empty_playlist_found)
            displayMessage(message, true)
            return
        }

        Log.d(TAG, "stop_playback: ${device.playlist.stop_playback}")

        Log.d(TAG, "new playlist found successfully...!")

        if (device.playlist.stop_playback){
            val message = application.getString(R.string.stop_playback_message)
            displayMessage(message, true)
            return
        }

        mediaPlayer.initialize(
            mediaPlayer.mediaItemToContent(mediaItems)
        )

        _viewState.postValue(ViewState.StartSignage)
        startMediaCache()
    }

    private fun startMediaCache() {
        viewModelScope.launch {
            mFileManager?.let {
                val downloadable = it.getNonCachedMedia(mediaPlayer.mediaList)
                WorkContractor.get().downloadPlaylistMediaSequentially(downloadable)
            }
        }
    }

    // Methods replaced by DeviceManager

    private fun startRuntime(deviceId: String) {

        runtimeController.start(
            scope = viewModelScope,
            deviceId = deviceId,
            onPlaylistUpdate = { fetchPlaylist(deviceId) },
            onUnauthorized = ::handleUnauthorized
        )
    }

    private suspend fun fetchPlaylist(deviceId: String) {
        val cachedPlaylistJson = deviceRepository.getPersistApiResponse(ENDPOINT_CURRENT_PLAYLIST)
        val cachedPlaylist = cachedPlaylistJson?.let { 
            try {
                gson.fromJson(it, PlaylistResponse::class.java)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse cached playlist", e)
                null
            }
        }
        
        when (val result = deviceRepository.fetchCurrentPlaylist(deviceId)) {
            is ApiResult.Success -> {
                Log.d(TAG, "playlist fetched successfully ...!!")
                if (cachedPlaylist != null) {
                    Log.d(TAG, "cachedPlaylistResponse: $cachedPlaylist")
                    Log.d(TAG, "newplaylist response: ${result.data}")

                    compareAndDeleteChangedMedia(cachedPlaylist, result.data)
                } else {
                    Log.d(TAG, "No cached playlist found, skipping comparison")
                }
                
                startSignage()
            }

            ApiResult.Unauthorized -> handleUnauthorized()
            else -> {
                Log.w(TAG, "something went wrong with playlist ...!!!")
            }
        }
    }

    private suspend fun compareAndDeleteChangedMedia(
        cachedPlaylist: PlaylistResponse,
        newPlaylist: PlaylistResponse
    ) {
        try {
            val cachedMediaMap = cachedPlaylist.mediaItems.associateBy { it.id }
            val newMediaMap = newPlaylist.mediaItems.associateBy { it.id }
            val filesToDelete = mutableSetOf<String>() // de-dupe deletes

            // Changed items: delete old cached file (and differing new filename, if any)
            for (newMediaItem in newPlaylist.mediaItems) {
                val cachedMediaItem = cachedMediaMap[newMediaItem.id] ?: continue

                val cachedTs = parseLastModifiedToEpochMs(cachedMediaItem.last_modified)
                val newTs = parseLastModifiedToEpochMs(newMediaItem.last_modified)
                val hasChanged = when {
                    cachedTs != null && newTs != null -> cachedTs != newTs
                    else -> newMediaItem.last_modified != cachedMediaItem.last_modified
                }

                if (hasChanged) {
                    if (cachedMediaItem.name.isNotBlank()) {
                        filesToDelete.add(cachedMediaItem.name)
                        Log.i(
                            TAG,
                            "Media changed (last_modified): deleting cached file ${cachedMediaItem.name} (cached: ${cachedMediaItem.last_modified}, new: ${newMediaItem.last_modified})"
                        )
                    }
                    if (newMediaItem.name.isNotBlank() && newMediaItem.name != cachedMediaItem.name) {
                        filesToDelete.add(newMediaItem.name)
                        Log.i(
                            TAG,
                            "Media changed (last_modified): deleting new filename ${newMediaItem.name} so it re-downloads"
                        )
                    }
                }
            }

            // Removed items: delete from disk
            for (cachedMediaItem in cachedPlaylist.mediaItems) {
                if (!newMediaMap.containsKey(cachedMediaItem.id) && cachedMediaItem.name.isNotBlank()) {
                    filesToDelete.add(cachedMediaItem.name)
                    Log.i(TAG, "Media file removed from playlist: ${cachedMediaItem.name}")
                }
            }

            mFileManager?.let { fileManager ->
                for (filename in filesToDelete) {
                    fileManager.deleteMediaFile(filename)
                }
                if (filesToDelete.isNotEmpty()) {
                    Log.i(TAG, "Deleted ${filesToDelete.size} media file(s) from cache due to changes")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error comparing and deleting changed media", e)
        }
    }

    private fun parseLastModifiedToEpochMs(value: String?): Long? {
        if (value.isNullOrBlank()) return null
        // Try numeric (epoch millis or seconds), then ISO timestamps
        value.toLongOrNull()?.let { num ->
            // Heuristic: if it's seconds (10 digits), convert to millis
            return if (num in 1_000_000_000..9_999_999_999) num * 1000 else num
        }
        runCatching { Instant.parse(value).toEpochMilli() }.getOrNull()?.let { return it }
        runCatching { OffsetDateTime.parse(value).toInstant().toEpochMilli() }.getOrNull()?.let { return it }
        return null
    }

    private fun postScreenDetailsOnPairing(deviceId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val screenDetailsJson = deviceInfoUtils.getDetailsWithDeviceId(deviceId).toString()

                when (val result = deviceRepository.postScreenDetails(deviceId, screenDetailsJson)) {
                    is ApiResult.Success -> {
                        Log.i(TAG, "Screen details posted successfully")
                    }
                    ApiResult.Unauthorized -> {
                        Log.w(TAG, "Unauthorized while posting screen details")
                    }
                    is ApiResult.Failure -> {
                        Log.e(TAG, "Failed to post screen details: ${result.message}")
                    }
                    else -> {
                        Log.w(TAG, "Unknown result while posting screen details")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error posting screen details", e)
            }
        }
    }

    private fun handleUnauthorized() {
        runtimeController.stop()
        deviceManager.clearDevice()
        currentDeviceId = null

        // Throttle restarts on repeated 401s to avoid API spam
        val now = System.currentTimeMillis()
        val sinceLast = now - lastUnauthorizedAtMs
        val delayMs = if (sinceLast >= UNAUTHORIZED_MIN_RETRY_MS) 0L else UNAUTHORIZED_MIN_RETRY_MS - sinceLast
        lastUnauthorizedAtMs = now

        unauthorizedRetryJob?.cancel()
        unauthorizedRetryJob = viewModelScope.launch(Dispatchers.IO) {
            // Clear cached pairing session so we generate a fresh code instead of reusing old one
            deviceRepository.clearCachedPairingSession()
            if (delayMs > 0) {
                Log.w(TAG, "Unauthorized; retrying pairing after ${delayMs / 1000}s")
                delay(delayMs)
            }
            deviceManager.startRegistrationFlow(this)
        }
    }

    override fun onCleared() {
        runtimeController.stop()
        deviceManager.clearDevice()
        super.onCleared()
    }

    fun nextMedia(): Content {
        return mediaPlayer.nextMedia
    }

    private fun displayMessage(message: String, connected: Boolean) {
        viewModelScope.launch {
            _viewState.postValue(DisplayMessage(message, connected))
        }
    }

    sealed class ViewState {
        data object StartSignage : ViewState()
        data class DisplayMessage(val message: String, val connected: Boolean) : ViewState()
        data class DisplayCode(val code: String) : ViewState()
    }

    companion object {
        private const val TAG = "MainViewModel"
        private const val UNAUTHORIZED_MIN_RETRY_MS = 60_000L
        private const val PAIRING_STATUS_INTERVAL_MS = 30_000L
    }
}

