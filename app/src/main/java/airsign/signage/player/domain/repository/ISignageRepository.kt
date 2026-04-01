package airsign.signage.player.domain.repository

import okhttp3.Call

interface ISignageRepository {
    suspend fun unpair(identifier: String): Call
    suspend fun getAppUpdates(version: String): Call

    suspend fun pingScreen(identifier: String): Call
}