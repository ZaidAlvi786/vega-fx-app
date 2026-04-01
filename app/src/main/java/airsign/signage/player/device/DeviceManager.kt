package airsign.signage.player.device

import airsign.signage.player.data.remote.ApiResult
import airsign.signage.player.domain.model.DeviceState
import airsign.signage.player.domain.model.PairingSession
import airsign.signage.player.domain.repository.DeviceRepository
import airsign.signage.player.ui.main.viewmodel.PairingCoordinator
import airsign.signage.player.ui.main.viewmodel.PairingStatusChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class DeviceManager @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val pairingCoordinator: PairingCoordinator,
    private val pairingStatusChecker: PairingStatusChecker
) {
    private val TAG = "DeviceManager"
    private val _deviceState = MutableStateFlow<DeviceState>(DeviceState.Unregistered)
    val deviceState: StateFlow<DeviceState> = _deviceState.asStateFlow()

    private var pollingJob: Job? = null
    private var generateJob: Job? = null

    companion object {
        private const val BASE_BACKOFF_MS = 5_000L
        private const val MAX_BACKOFF_MS = 60_000L
    }

    init {
        val savedId = deviceRepository.getPersistedDeviceId()
        if (savedId != null) {
            _deviceState.value = DeviceState.Active
        }
    }

    fun startRegistrationFlow(scope: CoroutineScope) {
        if (_deviceState.value is DeviceState.Active || _deviceState.value is DeviceState.Linked) {
            return
        }

        generateJob?.cancel()
        generateJob = scope.launch(Dispatchers.IO) {
            var currentDelay = BASE_BACKOFF_MS
            while (isActive && _deviceState.value is DeviceState.Unregistered) {
                val session = pairingCoordinator.generatePairingCode(
                    onCodeGenerated = { code ->
                        _deviceState.value = DeviceState.Pending(code)
                    },
                    onUnauthorized = {
                        clearDevice()
                    },
                    onError = {
                        Log.e(TAG, "Pairing generation error, retrying in ${currentDelay}ms")
                    }
                )

                if (session != null) {
                    startPolling(scope, session)
                    return@launch
                }

                if (_deviceState.value is DeviceState.Unregistered) {
                    delay(currentDelay)
                    currentDelay = (currentDelay * 2).coerceAtMost(MAX_BACKOFF_MS)
                }
            }
        }
    }

    private fun startPolling(scope: CoroutineScope, session: PairingSession) {
        pollingJob?.cancel()
        pollingJob = scope.launch(Dispatchers.IO) {
            var currentDelay = BASE_BACKOFF_MS
            while (isActive && _deviceState.value is DeviceState.Pending) {
                if (session.isExpired()) {
                    Log.w(TAG, "Pairing code expired; generating a new one")
                    deviceRepository.clearCachedPairingSession()
                    startRegistrationFlow(scope)
                    return@launch
                }

                when (val result = pairingStatusChecker.checkOnce(session.code)) {
                    is ApiResult.Success -> {
                        val deviceId = result.data
                        deviceRepository.persistDeviceId(deviceId)
                        _deviceState.value = DeviceState.Linked(deviceId)
                        // Transition to Active naturally
                        _deviceState.value = DeviceState.Active
                        return@launch
                    }
                    ApiResult.Unauthorized -> {
                        clearDevice()
                        return@launch
                    }
                    is ApiResult.Failure, ApiResult.NotFound -> {
                        Log.d(TAG, "Status check returned failure/not found, retrying in ${currentDelay}ms")
                        delay(currentDelay)
                        currentDelay = (currentDelay * 2).coerceAtMost(MAX_BACKOFF_MS)
                        continue
                    }
                    else -> {}
                }
                delay(currentDelay)
            }
        }
    }

    fun clearDevice() {
        pollingJob?.cancel()
        generateJob?.cancel()
        deviceRepository.clearDeviceData()
        _deviceState.value = DeviceState.Unregistered
    }
}
