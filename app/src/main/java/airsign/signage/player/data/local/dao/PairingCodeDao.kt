package airsign.signage.player.data.local.dao

import airsign.signage.player.data.local.entity.PairingCodeEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PairingCodeDao {

    @Query("SELECT * FROM pairing_code LIMIT 1")
    suspend fun getLatest(): PairingCodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PairingCodeEntity)

    @Query("DELETE FROM pairing_code")
    suspend fun clear()
}


