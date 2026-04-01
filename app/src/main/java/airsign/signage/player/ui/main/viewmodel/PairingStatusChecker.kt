package airsign.signage.player.ui.main.viewmodel

import airsign.signage.player.data.remote.ApiResult
import airsign.signage.player.domain.repository.DeviceRepository
import javax.inject.Inject

class PairingStatusChecker @Inject constructor(
    private val deviceRepository: DeviceRepository
) {

    suspend fun checkOnce(code: String): ApiResult<String> {
        return when (val result = deviceRepository.checkPairingStatus(code)) {

            is ApiResult.Success -> {
                if (result.data.paired && !result.data.deviceId.isNullOrBlank()) {
                    deviceRepository.persistDeviceId(result.data.deviceId)
                    ApiResult.Success(result.data.deviceId)
                } else {
                    ApiResult.Failure()
                }
            }

            is ApiResult.Unauthorized -> ApiResult.Unauthorized
            is ApiResult.Failure -> ApiResult.Failure()
            is ApiResult.NotFound -> ApiResult.Failure()
        }
    }
}