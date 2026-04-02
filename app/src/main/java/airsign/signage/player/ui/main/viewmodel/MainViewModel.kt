package airsign.signage.player.ui.main.viewmodel

import airsign.signage.player.R
import airsign.signage.player.R.string.connecting_to_server_update
import airsign.signage.player.data.MediaPlayer
import airsign.signage.player.data.downloader.FileManager
import airsign.signage.player.data.downloader.WorkContractor
import airsign.signage.player.data.remote.ApiResult
import airsign.signage.player.data.remote.PlaylistResponse
import airsign.signage.player.data.repository.DeviceRepositoryImpl.Companion.ENDPOINT_CHECK_PAIRING
import airsign.signage.player.data.utils.DeviceInfoUtils
import airsign.signage.player.domain.model.Content
import airsign.signage.player.domain.model.DeviceModel
import airsign.signage.player.data.utils.BasePref
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
    private val playlistRepository: airsign.signage.player.domain.repository.PlaylistRepository,
    private val syncManager: airsign.signage.player.sync.SyncManager,
    private val deviceManager: DeviceManager,
    private val runtimeController: DeviceRuntimeController,
    private val deviceInfoUtils: DeviceInfoUtils,
    private val fileManager: airsign.signage.player.data.downloader.FileManager,
    private val basePref: BasePref
) : ViewModel() {

    private val TAG = "MainViewModel"
    private val _viewState = MutableLiveData<ViewState>()
    val viewState: LiveData<ViewState> = _viewState

    private var isSyncing = false

    private var currentDeviceId: String? = null
    private var unauthorizedRetryJob: Job? = null
    private var lastUnauthorizedAtMs: Long = 0L
    private val gson = Gson()

    init {
    }
    fun onAppResumed() {
        viewModelScope.launch {
            deviceManager.deviceState.collect { state ->
                when (state) {
                    is DeviceState.Unregistered -> {
                        Log.d(TAG, "starting registration ....!!!")
                        _viewState.postValue(ViewState.DisplayMessage("Connecting to server...", false))
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
                val cachedResponse = playlistRepository.getCachedPlaylist()
                val pairingResponse = deviceRepository.getPersistApiResponse(ENDPOINT_CHECK_PAIRING)
                val paringStatus = Gson().fromJson(pairingResponse, airsign.signage.player.data.remote.CheckPairingResponse::class.java)
                val screenCode = deviceRepository.getCachedPairingSession()?.code

                val device = DeviceModel(
                    cachedResponse,
                    paringStatus,
                    screenCode
                )
                verifyDeviceState(device)
            } catch (e: Exception) {
                Log.e(TAG, "startSignage failed", e)
                displayMessage(application.getString(connecting_to_server_update), false)
            }
        }
    }

    private fun verifyDeviceState(device: DeviceModel?) {
        try {
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

        val contentItems = mediaPlayer.mediaItemToContent(mediaItems)
        
        Log.i(TAG, "════════════ PLAYLIST TRACE ════════════")
        Log.i(TAG, "Device: ${device.paringResponse?.deviceId}")
        Log.i(TAG, "Total Items: ${contentItems.size}")
        contentItems.forEachIndexed { index, content ->
            Log.i(TAG, "[$index] ID: ${mediaItems.getOrNull(index)?.id} | File: ${content.filename} | Duration: ${content.duration}s | Type: ${content.type}")
        }
        Log.i(TAG, "═══════════════════════════════════════")

        mediaPlayer.initialize(contentItems)

        _viewState.postValue(ViewState.StartSignage)
        startMediaCache()
        } catch (e: Exception) {
            Log.e(TAG, "════════════ FATAL SYNC ERROR ════════════")
            Log.e(TAG, "Crash during playlist initialization: ${e.message}", e)
            Log.e(TAG, "══════════════════════════════════════════")
        }
    }

    private fun startMediaCache() {
        viewModelScope.launch {
            val downloadable = fileManager.getNonCachedMedia(mediaPlayer.mediaList)
            WorkContractor.get().downloadPlaylistMediaSequentially(downloadable)
        }
    }

    // Methods replaced by DeviceManager

    private fun startRuntime(deviceId: String) {

        runtimeController.start(
            scope = viewModelScope,
            deviceId = deviceId,
            onPlaylistUpdate = { timestamp -> fetchPlaylist(deviceId, timestamp) },
            onGetLastSyncTime = {
                val time = basePref.getLong(BasePref.LAST_PLAYLIST_UPDATED_AT, -1L)
                if (time == -1L) null else time
            },
            onUnauthorized = ::handleUnauthorized
        )
    }

    private suspend fun fetchPlaylist(deviceId: String, serverUpdatedAt: Long? = null) {
        if (isSyncing) {
            Log.i(TAG, "fetchPlaylist: SKIPPING sync request (Sync already in progress)")
            return
        }

        // Timestamp-based synchronization check
        val lastSyncAt = basePref.getLong(BasePref.LAST_PLAYLIST_UPDATED_AT, -1L)
        if (serverUpdatedAt != null && lastSyncAt != -1L && serverUpdatedAt <= lastSyncAt) {
            Log.d(TAG, "fetchPlaylist: Skipping sync, playlist timestamp is already up-to-date (server: $serverUpdatedAt, local: $lastSyncAt)")
            return
        }

        try {
            isSyncing = true
            Log.i(TAG, "fetchPlaylist: sync trigger received for $deviceId (server_ts: $serverUpdatedAt, local_ts: $lastSyncAt)")
            Log.d(TAG, "fetchPlaylist: starting sync for $deviceId")
            val result = syncManager.sync(deviceId)
            
            when (result) {
                is ApiResult.Success -> {
                    Log.i(TAG, "Playlist sync successful - re-initializing playback pipeline")
                    
                    // Periodically persist the new timestamp if provided
                    if (serverUpdatedAt != null) {
                        basePref.setLong(BasePref.LAST_PLAYLIST_UPDATED_AT, serverUpdatedAt)
                        Log.d(TAG, "fetchPlaylist: Updated local sync timestamp to $serverUpdatedAt")
                    }
                    
                    startSignage()
                }
                ApiResult.Unauthorized -> handleUnauthorized()
                else -> {
                    Log.e(TAG, "Playlist sync failed: $result")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during fetchPlaylist", e)
        } finally {
            isSyncing = false
        }
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

