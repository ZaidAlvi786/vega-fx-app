package airsign.signage.player.player.cache

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.ExoDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File
import javax.inject.Singleton

@UnstableApi
@Singleton
class MediaCacheManager(context: Context) {

    private val cacheDirectory = File(context.cacheDir, "media_store_exo")
    private val cacheEvictor = LeastRecentlyUsedCacheEvictor(768 * 1024 * 1024) // 768MB
    private val databaseProvider = ExoDatabaseProvider(context)

    val simpleCache: SimpleCache by lazy {
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs()
        }
        SimpleCache(cacheDirectory, cacheEvictor, databaseProvider)
    }
}
