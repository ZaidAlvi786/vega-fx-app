package airsign.signage.player.data.local

import airsign.signage.player.data.local.dao.ApiResponseDao
import airsign.signage.player.data.local.dao.PairingCodeDao
import airsign.signage.player.data.local.entity.ApiResponseEntity
import airsign.signage.player.data.local.entity.PairingCodeEntity
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ApiResponseEntity::class,
        PairingCodeEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AirsignDatabase : RoomDatabase() {
    abstract fun apiResponseDao(): ApiResponseDao
    abstract fun pairingCodeDao(): PairingCodeDao
}


