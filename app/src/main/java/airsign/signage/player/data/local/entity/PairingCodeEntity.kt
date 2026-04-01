package airsign.signage.player.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pairing_code")
data class PairingCodeEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val code: String,
    val expiresAtEpochMillis: Long,
    val pairingId: String,
    val createdAt: Long
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}


