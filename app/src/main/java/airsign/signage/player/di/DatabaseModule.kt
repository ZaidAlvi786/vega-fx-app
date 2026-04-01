package airsign.signage.player.di

import airsign.signage.player.data.local.AirsignDatabase
import airsign.signage.player.data.local.dao.ApiResponseDao
import airsign.signage.player.data.local.dao.PairingCodeDao
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "aircast.db"

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AirsignDatabase {
        return Room.databaseBuilder(
            context,
            AirsignDatabase::class.java,
            DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideApiResponseDao(database: AirsignDatabase): ApiResponseDao {
        return database.apiResponseDao()
    }

    @Provides
    fun providePairingCodeDao(database: AirsignDatabase): PairingCodeDao {
        return database.pairingCodeDao()
    }
}


