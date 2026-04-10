package com.freemusic.di

import android.content.Context
import androidx.room.Room
import com.freemusic.data.local.FreeMusicDatabase
import com.freemusic.data.local.dao.FavoriteSongDao
import com.freemusic.data.local.dao.PlayHistoryDao
import com.freemusic.data.local.dao.SearchHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): FreeMusicDatabase {
        return Room.databaseBuilder(
            context,
            FreeMusicDatabase::class.java,
            FreeMusicDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideFavoriteSongDao(database: FreeMusicDatabase): FavoriteSongDao {
        return database.favoriteSongDao()
    }

    @Provides
    @Singleton
    fun provideSearchHistoryDao(database: FreeMusicDatabase): SearchHistoryDao {
        return database.searchHistoryDao()
    }

    @Provides
    @Singleton
    fun providePlayHistoryDao(database: FreeMusicDatabase): PlayHistoryDao {
        return database.playHistoryDao()
    }
}
