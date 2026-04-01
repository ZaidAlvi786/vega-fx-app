package airsign.signage.player.data

import airsign.signage.player.BuildConfig
import airsign.signage.player.data.MediaFactory.IMAGE
import airsign.signage.player.data.MediaFactory.VIDEO
import airsign.signage.player.data.remote.CheckPairingResponse
import airsign.signage.player.data.remote.MediaItem
import airsign.signage.player.data.remote.PlaylistResponse
import airsign.signage.player.data.repository.DeviceRepositoryImpl.Companion.ENDPOINT_CHECK_PAIRING
import airsign.signage.player.data.repository.DeviceRepositoryImpl.Companion.ENDPOINT_CURRENT_PLAYLIST
import airsign.signage.player.data.utils.BasePref
import airsign.signage.player.domain.model.Content
import airsign.signage.player.domain.model.DeviceModel
import airsign.signage.player.domain.repository.DeviceRepository
import android.util.Log
import com.android.multitasker.domain.utils.CONSTANT
import com.google.gson.Gson
import org.json.JSONException
import java.io.Serializable

class MediaPlayer(private val mPref: BasePref) : Serializable {
    val mediaList: ArrayList<Content> = ArrayList()
    private var ptr = -1

    private fun parse(playlist: List<Content>): MediaPlayer {
        try {
            mediaList.clear()

            for (content in playlist)
                if (content.type == IMAGE || content.type == VIDEO) {

                    content.url = content.url.replace("http://", "https://")

                    if (content.url.startsWith("/uploads/"))
                        content.url = "${BuildConfig.DISPLAY_URL}${content.url}"
                }

            mediaList.addAll(playlist)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return this
    }

    fun initialize(playlist: List<Content>) {
        try {
            parse(playlist)
            ptr = -1
        } catch (e: Exception) {
            e.printStackTrace()
            mPref.remove(CONSTANT.screenlist.toString())
        }
    }

    private val TAG = "MediaPlayer"

    suspend fun getDevice(deviceRepository: DeviceRepository): DeviceModel? {
        return try {
            val pResponse =
                deviceRepository.getPersistApiResponse(ENDPOINT_CURRENT_PLAYLIST)

            val pairingResponse =
                deviceRepository.getPersistApiResponse(ENDPOINT_CHECK_PAIRING)

            val playlistData =
                Gson().fromJson(pResponse, PlaylistResponse::class.java)

            val paringStatus =
                Gson().fromJson(pairingResponse, CheckPairingResponse::class.java)

            val screenCode =
                deviceRepository.getCachedPairingSession()?.code

            DeviceModel(
                playlistData,
                paringStatus,
                screenCode
            )

        } catch (e: Exception) {
            Log.e(TAG, "getDevice failed", e)
            DeviceModel(null, null, null)
        }
    }

    fun mediaItemToContent(mediaItem: List<MediaItem>): List<Content> {
        val contentList = ArrayList<Content>().toMutableList()
        for (item in mediaItem) {
            val content = Content(
                type = item.type,
                url = item.url,
                duration = item.durationSec,
                filename = item.name
            )
            contentList.add(content)
        }

        return contentList
    }


    val nextMedia: Content
        get() {
            val size = mediaList.size
            return mediaList[++ptr % size]
        }


    /*
     response of empty playlist
     playlistResponse {"device_id":"43d246db-7232-433b-b174-b5da5e9fd6a9","media_items":[]}
     response when device has playlist
     {"device_id":"43d246db-7232-433b-b174-b5da5e9fd6a9","media_items":[{"duration_sec":15,"file_size":163945,"height":627,"id":"e1599271-0d42-4811-856e-2f9211828f58","mime_type":"image/jpeg","name":"1662401893177.jpeg","position":0,"thumbnail_url":"https://vbpmfumdwdkkjjudnyqm.supabase.co/storage/v1/object/sign/media/250d74a0-2a2b-455c-8cc1-7f3ab777a14c/thumbnails/1765478186027_thumb_1662401893177.jpg?token\u003deyJraWQiOiJzdG9yYWdlLXVybC1zaWduaW5nLWtleV9iZjU5YmI5NC1hNDQ5LTRlM2MtODUyNS1jODk5ZWVjNmM4ZTMiLCJhbGciOiJIUzI1NiJ9.eyJ1cmwiOiJtZWRpYS8yNTBkNzRhMC0yYTJiLTQ1NWMtOGNjMS03ZjNhYjc3N2ExNGMvdGh1bWJuYWlscy8xNzY1NDc4MTg2MDI3X3RodW1iXzE2NjI0MDE4OTMxNzcuanBnIiwiaWF0IjoxNzY3NDcwMTY0LCJleHAiOjE3Njc0NzM3NjR9.yQx-QpGUsWIituit5X-uhJL33GKP18Ap8nWa0-TkILo","type":"image","url":"https://vbpmfumdwdkkjjudnyqm.supabase.co/storage/v1/object/sign/media/250d74a0-2a2b-455c-8cc1-7f3ab777a14c/1765478183138_1662401893177.jpeg?token\u003deyJraWQiOiJzdG9yYWdlLXVybC1zaWduaW5nLWtleV9iZjU5YmI5NC1hNDQ5LTRlM2MtODUyNS1jODk5ZWVjNmM4ZTMiLCJhbGciOiJIUzI1NiJ9.eyJ1cmwiOiJtZWRpYS8yNTBkNzRhMC0yYTJiLTQ1NWMtOGNjMS03ZjNhYjc3N2ExNGMvMTc2NTQ3ODE4MzEzOF8xNjYyNDAxODkzMTc3LmpwZWciLCJpYXQiOjE3Njc0NzAxNjQsImV4cCI6MTc2NzQ3Mzc2NH0.hKy1YL58-qa9maKnie2kOcObh5tdQHG9gTDxT-qF1X8","width":1200}],"playlist":{"description":"","id":"1fd564f9-8b84-4ebe-84df-ad1ec7e27403","name":"realtime testing","status":"draft"}}

     response when device paired
     {"deviceId":"43d246db-7232-433b-b174-b5da5e9fd6a9","paired":true}
     response when device is not paired
     {"paired":false}
    * */

}