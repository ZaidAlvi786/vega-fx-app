package airsign.signage.player.di

import android.app.Application
import airsign.signage.player.data.MediaPlayer
import airsign.signage.player.data.local.dao.ApiResponseDao
import airsign.signage.player.data.local.dao.PairingCodeDao
import airsign.signage.player.data.remote.DeviceApiService
import airsign.signage.player.data.repository.DeviceRepositoryImpl
import airsign.signage.player.data.utils.BasePref
import airsign.signage.player.data.utils.DeviceInfoUtils
import airsign.signage.player.data.utils.NetworkUtils
import airsign.signage.player.domain.repository.DeviceRepository
import airsign.signage.player.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.engine.cio.CIO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Postgrest)
            install(Realtime)
            httpEngine = CIO.create()
        }
    }

    @Provides
    @Singleton
    fun provideBasePref(app: Application): BasePref {
        return BasePref(app)
    }

    @Provides
    @Singleton
    fun providePlaylist(mPref: BasePref): MediaPlayer {
        return MediaPlayer(mPref)
    }

    @Provides
    @Singleton
    fun provideNetworkUtils(application: Application): NetworkUtils {
        return NetworkUtils(application)
    }

    @Provides
    @Singleton
    fun deviceUtils(
        networkUtils: NetworkUtils,
        application: Application,
        basePref: BasePref
    ): DeviceInfoUtils {
        return DeviceInfoUtils(networkUtils, application, basePref)
    }

    @Provides
    @Singleton
    fun provideDeviceRepository(
        apiService: DeviceApiService,
        basePref: BasePref,
        apiResponseDao: ApiResponseDao,
        pairingCodeDao: PairingCodeDao
    ): DeviceRepository {
        return DeviceRepositoryImpl(apiService, basePref, apiResponseDao, pairingCodeDao)
    }

    @Provides
    @Singleton
    fun providePlaylistRepository(
        apiService: DeviceApiService,
        apiResponseDao: ApiResponseDao
    ): airsign.signage.player.domain.repository.PlaylistRepository {
        return airsign.signage.player.data.repository.PlaylistRepositoryImpl(apiService, apiResponseDao)
    }

    @androidx.media3.common.util.UnstableApi
    @Provides
    @Singleton
    fun provideMediaCacheManager(app: Application): airsign.signage.player.player.cache.MediaCacheManager {
        return airsign.signage.player.player.cache.MediaCacheManager(app)
    }

    @Provides
    @Singleton
    fun provideFileManager(app: Application): airsign.signage.player.data.downloader.FileManager {
        return airsign.signage.player.data.downloader.FileManager(app)
    }
}