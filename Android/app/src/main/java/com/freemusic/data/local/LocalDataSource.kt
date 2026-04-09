package com.freemusic.data.local

import com.freemusic.data.local.dao.FavoriteSongDao
import com.freemusic.data.local.dao.SearchHistoryDao
import com.freemusic.data.local.entity.FavoriteSongEntity
import com.freemusic.data.local.entity.SearchHistoryEntity
import com.freemusic.data.local.entity.toDomain
import com.freemusic.data.local.entity.toFavoriteEntity
import com.freemusic.domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataSource @Inject constructor(
    private val favoriteSongDao: FavoriteSongDao,
    private val searchHistoryDao: SearchHistoryDao
) {

    // ============ 收藏歌曲 ============

    fun getFavorites(): Flow<List<Song>> {
        return favoriteSongDao.getAllFavorites().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun isFavorite(songId: String): Flow<Boolean> {
        return favoriteSongDao.isFavorite(songId)
    }

    suspend fun addFavorite(song: Song) {
        favoriteSongDao.insertFavorite(song.toFavoriteEntity())
    }

    suspend fun removeFavorite(songId: String) {
        favoriteSongDao.deleteFavoriteById(songId)
    }

    suspend fun toggleFavorite(song: Song) {
        val existing = favoriteSongDao.getFavoriteById(song.id)
        if (existing != null) {
            favoriteSongDao.deleteFavoriteById(song.id)
        } else {
            favoriteSongDao.insertFavorite(song.toFavoriteEntity())
        }
    }

    // ============ 搜索历史 ============

    fun getSearchHistory(limit: Int = 10): Flow<List<String>> {
        return searchHistoryDao.getRecentSearches(limit).map { entities ->
            entities.map { it.keyword }
        }
    }

    suspend fun addSearchHistory(keyword: String) {
        if (keyword.isNotBlank()) {
            searchHistoryDao.insertSearch(SearchHistoryEntity(keyword = keyword))
        }
    }

    suspend fun removeSearchHistory(keyword: String) {
        searchHistoryDao.deleteSearch(keyword)
    }

    suspend fun clearSearchHistory() {
        searchHistoryDao.clearHistory()
    }
}
