package airsign.signage.player.domain.model

sealed class DeviceState {
    object Unregistered : DeviceState()
    data class Pending(val pairingCode: String) : DeviceState()
    data class Linked(val deviceId: String) : DeviceState()
    object Active : DeviceState()
}
