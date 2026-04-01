package airsign.signage.player.ui.main.viewmodel

import airsign.signage.player.data.remote.ApiResult
import airsign.signage.player.domain.model.PairingSession
import airsign.signage.player.domain.repository.DeviceRepository
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PairingCoordinator @Inject constructor(
    private val deviceRepository: DeviceRepository
) {

    private val TAG = "PairingCoordinator"

    suspend fun generatePairingCode(
        onCodeGenerated: (String) -> Unit,
        onUnauthorized: () -> Unit,
        onError: () -> Unit
    ): PairingSession? {

        var session: PairingSession? = deviceRepository.getCachedPairingSession()
        Log.d(TAG, "cached session: $session")

        if (session == null || session.isExpired()) {

            when (val result = deviceRepository.generatePairingCode()) {

                is ApiResult.Success -> {
                    deviceRepository.clearCachedPlaylist()
                    session = result.data
                    Log.d(TAG, "screen code generated successfully: $session")
                    onCodeGenerated(result.data.code)
                }

                is ApiResult.Unauthorized -> {
                    onUnauthorized()
                }

                is ApiResult.Failure -> {
                    Log.e(
                        TAG,
                        "generatePairingCode failed: code=${result.code}, message=${result.message}, errorBody=${result.errorBody}"
                    )
                    onError()
                }

                is ApiResult.NotFound -> {
                    Log.e(TAG, "generatePairingCode returned 404 (not found)")
                    onError()
                }
            }
        } else {
            onCodeGenerated(session.code)
        }

        return session
    }
}