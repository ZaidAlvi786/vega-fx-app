package airsign.signage.player.di

import airsign.signage.player.BuildConfig
import airsign.signage.player.data.remote.ApiLoggingInterceptor
import airsign.signage.player.data.remote.AuthenticationInterceptor
import airsign.signage.player.data.remote.DeviceApiService
import airsign.signage.player.data.remote.NetworkExceptionInterceptor
import airsign.signage.player.data.utils.BasePref
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(basePref: BasePref): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthenticationInterceptor(basePref))
            .addInterceptor(ApiLoggingInterceptor()) // Log all API calls
            .addInterceptor(NetworkExceptionInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create()) // For raw JSON strings
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideDeviceApiService(retrofit: Retrofit): DeviceApiService {
        return retrofit.create(DeviceApiService::class.java)
    }
}


