package airsign.signage.player.domain.model

import airsign.signage.player.data.remote.CheckPairingResponse
import airsign.signage.player.data.remote.PlaylistResponse
import java.io.Serializable

data class DeviceModel(
    val playlist: PlaylistResponse? = null,
    val paringResponse: CheckPairingResponse? = null,
    val code: String? = null,
    val schedule: ScheduleInfo? = null
)


data class Content(
    val type: String,
    var url: String,
    val duration: Int? = 30,
    val filename: String? = null,
) : Serializable

data class ScheduleInfo(
    val _id: String,
    val playlistId: PlaylistId,
    val startDate: String,
    val endDate: String,
    val startTime: String,
    val endTime: String,
    val daysOfWeek: List<String>,
    val timezone: String,
    val isActive: Boolean,
) : Serializable

data class PlaylistId(
    val _id: String,
    val name: String,
    val items: List<Content>,
    val totalDuration: Int
) : Serializable

data class Schedule(
    val startDate: String,
    val endDate: String,
    val endTime: String,
    val startTime: String,
    val days: List<Day>
) : Serializable

data class Day(val day: String) : Serializable
