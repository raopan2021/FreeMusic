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
