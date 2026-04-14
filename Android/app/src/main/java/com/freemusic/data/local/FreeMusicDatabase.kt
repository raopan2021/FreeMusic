package com.freemusic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.freemusic.data.local.dao.FavoriteSongDao
import com.freemusic.data.local.dao.LyricsCacheDao
import com.freemusic.data.local.dao.LocalSongDao
import com.freemusic.data.local.dao.PlayHistoryDao
import com.freemusic.data.local.dao.SearchHistoryDao
import com.freemusic.data.local.entity.FavoriteSongEntity
import com.freemusic.data.local.entity.LyricsCacheEntity
import com.freemusic.data.local.entity.LocalSongEntity
import com.freemusic.data.local.entity.PlayHistoryEntity
import com.freemusic.data.local.entity.SearchHistoryEntity

@Database(
    entities = [
        FavoriteSongEntity::class,
        SearchHistoryEntity::class,
        PlayHistoryEntity::class,
        LyricsCacheEntity::class,
        LocalSongEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class FreeMusicDatabase : RoomDatabase() {

    abstract fun favoriteSongDao(): FavoriteSongDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun playHistoryDao(): PlayHistoryDao
    abstract fun lyricsCacheDao(): LyricsCacheDao
    abstract fun localSongDao(): LocalSongDao

    companion object {
        const val DATABASE_NAME = "freemusic_db"
    }
}
