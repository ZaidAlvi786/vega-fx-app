package airsign.signage.player.domain.model

import java.time.Instant

data class PairingSession(
    val code: String,
    val expiresAt: Instant,
    val pairingId: String
) {
    fun isExpired(currentTime: Instant = Instant.now()): Boolean = currentTime.isAfter(expiresAt)
}


