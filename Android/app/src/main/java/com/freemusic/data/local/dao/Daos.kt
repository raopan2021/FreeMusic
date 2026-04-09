package com.freemusic.data.local.dao

import androidx.room.*
import com.freemusic.data.local.entity.FavoriteSongEntity
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
