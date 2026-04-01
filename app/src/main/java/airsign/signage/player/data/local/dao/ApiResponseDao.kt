package airsign.signage.player.data.local.dao

import airsign.signage.player.data.local.entity.ApiResponseEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ApiResponseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(response: ApiResponseEntity)

    @Query("SELECT * FROM api_responses WHERE endpoint = :endpoint LIMIT 1")
    suspend fun getLatest(endpoint: String): ApiResponseEntity?

    @Query("DELETE FROM api_responses WHERE endpoint = :endpoint")
    suspend fun delete(endpoint: String)
}


