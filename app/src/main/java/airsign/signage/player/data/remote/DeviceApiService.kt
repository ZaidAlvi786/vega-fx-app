package airsign.signage.player.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service definition for the Aircast device endpoints.
 */
interface DeviceApiService {

    @POST("api/devices/generate-code")
    suspend fun generatePairingCode(): Response<GenerateCodeResponse>

    @POST("api/devices/screen-details")
    suspend fun postScreenDetails(
        @Body body: String
    ): Response<Unit>

    @GET("api/devices/check-pairing")
    suspend fun checkPairingStatus(
        @Query("code") code: String
    ): Response<CheckPairingResponse>

    @POST("api/devices/heartbeat")
    suspend fun sendHeartbeat(
        @Body body: HeartbeatRequest
    ): Response<HeartbeatResponse>

    @GET("api/devices/{deviceId}/current-playlist")
    suspend fun fetchCurrentPlaylist(
        @Path("deviceId") deviceId: String
    ): Response<PlaylistResponse>
}

data class GenerateCodeResponse(
    val code: String,
    @SerializedName("expires_at") val expiresAt: String,
    @SerializedName("pairing_id") val pairingId: String
)

data class CheckPairingResponse(
    val paired: Boolean,
    @SerializedName("deviceId") val deviceId: String?
)

data class HeartbeatRequest(
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("appVersion") val appVersion: String,
    @SerializedName("osVersion") val osVersion: String
)

data class HeartbeatResponse(
    val success: Boolean,
    val status: String,
    val hasNewPlaylist: Boolean
)

data class PlaylistResponse(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("stop_playback") val stop_playback: Boolean,
    val playlist: PlaylistMeta?,
    @SerializedName("media_items") val mediaItems: List<MediaItem>
)

data class PlaylistMeta(
    val id: String,
    val name: String,
    val status: String,
    val description: String?
)

data class MediaItem(
    val id: String,
    val name: String,
    val type: String,
    val url: String,
    @SerializedName("thumbnail_url") val thumbnailUrl: String?,
    @SerializedName("mime_type") val mimeType: String?,
    @SerializedName("file_size") val fileSize: Long?,
    val last_modified : String,
    val checksum: String?,
    val width: Int?,
    val height: Int?,
    @SerializedName("duration_sec") val durationSec: Int?,
    val position: Int?
)


