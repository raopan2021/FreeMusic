package com.freemusic.data.local.dao

import androidx.room.*
import com.freemusic.data.local.entity.FavoriteSongEntity
import com.freemusic.data.local.entity.LyricsCacheEntity
import com.freemusic.data.local.entity.LocalSongEntity
import com.freemusic.data.local.entity.PlayHistoryEntity
import com.freemusic.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 收藏歌曲 DAO
 */
@Dao
interface FavoriteSongDao {

    @Query("SELECT * FROM favorite_songs ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteSongEntity>>

    @Query("SELECT * FROM favorite_songs WHERE id = :songId")
    suspend fun getFavoriteById(songId: String): FavoriteSongEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_songs WHERE id = :songId)")
    fun isFavorite(songId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(song: FavoriteSongEntity)

    @Delete
    suspend fun deleteFavorite(song: FavoriteSongEntity)

    @Query("DELETE FROM favorite_songs WHERE id = :songId")
    suspend fun deleteFavoriteById(songId: String)

    @Query("DELETE FROM favorite_songs")
    suspend fun deleteAllFavorites()
}

/**
 * 搜索历史 DAO
 */
@Dao
interface SearchHistoryDao {

    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT :limit")
    fun getRecentSearches(limit: Int = 10): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(history: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE keyword = :keyword")
    suspend fun deleteSearch(keyword: String)

    @Query("DELETE FROM search_history")
    suspend fun clearHistory()
}

/**
 * 播放历史 DAO
 */
@Dao
interface PlayHistoryDao {

    @Query("SELECT * FROM play_history ORDER BY playedAt DESC LIMIT :limit")
    fun getRecentPlays(limit: Int = 50): Flow<List<PlayHistoryEntity>>

    @Query("SELECT * FROM play_history ORDER BY playCount DESC LIMIT :limit")
    fun getMostPlayed(limit: Int = 50): Flow<List<PlayHistoryEntity>>

    @Query("SELECT * FROM play_history WHERE id = :songId")
    suspend fun getPlayHistoryById(songId: String): PlayHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayHistory(history: PlayHistoryEntity)

    @Query("UPDATE play_history SET playCount = playCount + 1, playedAt = :playedAt WHERE id = :songId")
    suspend fun incrementPlayCount(songId: String, playedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM play_history WHERE id = :songId")
    suspend fun deletePlayHistory(songId: String)

    @Query("DELETE FROM play_history")
    suspend fun clearHistory()
    
    @Query("SELECT COUNT(*) FROM play_history")
    suspend fun getHistoryCount(): Int
    
    @Query("DELETE FROM play_history WHERE id NOT IN (SELECT id FROM play_history ORDER BY playedAt DESC LIMIT :keepCount)")
    suspend fun trimHistory(keepCount: Int)
}

/**
 * 歌词缓存 DAO
 */
@Dao
interface LyricsCacheDao {
    
    @Query("SELECT * FROM lyrics_cache WHERE songId = :songId")
    suspend fun getLyrics(songId: String): LyricsCacheEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLyrics(lyrics: LyricsCacheEntity)
    
    @Query("DELETE FROM lyrics_cache WHERE cachedAt < :timestamp")
    suspend fun deleteOldCache(timestamp: Long)
    
    @Query("DELETE FROM lyrics_cache")
    suspend fun clearCache()
    
    @Query("SELECT COUNT(*) FROM lyrics_cache")
    suspend fun getCacheCount(): Int
}

/**
 * 本地歌曲 DAO
 */
@Dao
interface LocalSongDao {
    
    @Query("SELECT * FROM local_songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<LocalSongEntity>>
    
    @Query("SELECT * FROM local_songs ORDER BY title ASC")
    suspend fun getAllSongsList(): List<LocalSongEntity>
    
    @Query("SELECT * FROM local_songs WHERE id = :songId")
    suspend fun getSongById(songId: String): LocalSongEntity?
    
    @Query("SELECT * FROM local_songs WHERE id IN (:songIds)")
    suspend fun getSongsByIds(songIds: List<String>): List<LocalSongEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: LocalSongEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<LocalSongEntity>)
    
    @Delete
    suspend fun deleteSong(song: LocalSongEntity)
    
    @Query("DELETE FROM local_songs")
    suspend fun deleteAllSongs()
    
    @Query("SELECT COUNT(*) FROM local_songs")
    suspend fun getSongCount(): Int
    
    @Query("DELETE FROM local_songs WHERE id NOT IN (:validIds)")
    suspend fun deleteRemovedSongs(validIds: List<String>)
}
