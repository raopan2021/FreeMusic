package com.freemusic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 歌曲收藏实体
 */
@Entity(tableName = "favorite_songs")
data class FavoriteSongEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val coverUrl: String?,
    val duration: Long,
    val neteaseId: String?,
    val addedAt: Long = System.currentTimeMillis()
)

/**
 * 搜索历史实体
 */
@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey
    val keyword: String,
    val searchedAt: Long = System.currentTimeMillis()
)

/**
 * 播放历史实体
 */
@Entity(tableName = "play_history")
data class PlayHistoryEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val coverUrl: String?,
    val duration: Long,
    val neteaseId: String?,
    val playedAt: Long = System.currentTimeMillis(),
    val playCount: Int = 1
)

/**
 * 歌词缓存实体
 */
@Entity(tableName = "lyrics_cache")
data class LyricsCacheEntity(
    @PrimaryKey
    val songId: String,
    val lrc: String?,
    val yrc: String?,
    val translation: String?,
    val ttml: String?,
    val metadata: String?, // JSON encoded list
    val cachedAt: Long = System.currentTimeMillis()
)

/**
 * 本地歌曲实体 - 持久化扫描的本地音乐
 */
@Entity(tableName = "local_songs")
data class LocalSongEntity(
    @PrimaryKey
    val id: String, // MediaStore ID
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val coverUrl: String?, // content://media/external/audio/albumart/xxx
    val duration: Long,
    val filePath: String,
    val displayName: String,
    val size: Long,
    val dateAdded: Long,
    val lastScanned: Long = System.currentTimeMillis()
)
