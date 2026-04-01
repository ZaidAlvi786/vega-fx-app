package airsign.signage.player.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_responses")
data class ApiResponseEntity(
    @PrimaryKey val endpoint: String,
    val statusCode: Int?,
    val payload: String?,
    val timestamp: Long
)


