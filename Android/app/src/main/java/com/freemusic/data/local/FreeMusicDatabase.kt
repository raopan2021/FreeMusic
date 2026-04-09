package com.freemusic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.freemusic.data.local.dao.FavoriteSongDao
import com.freemusic.data.local.dao.SearchHistoryDao
import com.freemusic.data.local.entity.FavoriteSongEntity
import com.freemusic.data.local.entity.SearchHistoryEntity

@Database(
    entities = [
        FavoriteSongEntity::class,
        SearchHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FreeMusicDatabase : RoomDatabase() {

    abstract fun favoriteSongDao(): FavoriteSongDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        const val DATABASE_NAME = "freemusic_db"
    }
}
