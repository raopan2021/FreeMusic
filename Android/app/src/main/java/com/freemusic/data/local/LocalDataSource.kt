package com.freemusic.data.local

import com.freemusic.data.local.dao.FavoriteSongDao
import com.freemusic.data.local.dao.LyricsCacheDao
import com.freemusic.data.local.dao.PlayHistoryDao
import com.freemusic.data.local.dao.SearchHistoryDao
import com.freemusic.data.local.entity.FavoriteSongEntity
import com.freemusic.data.local.entity.LyricsCacheEntity
import com.freemusic.data.local.entity.PlayHistoryEntity
import com.freemusic.data.local.entity.SearchHistoryEntity
import com.freemusic.data.local.entity.toDomain
import com.freemusic.data.local.entity.toFavoriteEntity
import com.freemusic.data.local.entity.toPlayHistoryEntity
import com.freemusic.domain.model.Lyrics
import com.freemusic.domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataSource @Inject constructor(
    private val favoriteSongDao: FavoriteSongDao,
    private val searchHistoryDao: SearchHistoryDao,
    private val playHistoryDao: PlayHistoryDao,
    private val lyricsCacheDao: LyricsCacheDao
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

    // ============ 播放历史 ============

    fun getPlayHistory(limit: Int = 50): Flow<List<Song>> {
        return playHistoryDao.getRecentPlays(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getMostPlayed(limit: Int = 50): Flow<List<Song>> {
        return playHistoryDao.getMostPlayed(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun addToPlayHistory(song: Song) {
        val existing = playHistoryDao.getPlayHistoryById(song.id)
        if (existing != null) {
            playHistoryDao.incrementPlayCount(song.id)
        } else {
            playHistoryDao.insertPlayHistory(song.toPlayHistoryEntity())
        }
    }

    suspend fun clearPlayHistory() {
        playHistoryDao.clearHistory()
    }
    
    // ============ 歌曲缓存 ============
    
    /**
     * 从播放历史获取缓存的歌曲
     */
    suspend fun getCachedSong(songId: String): Song? {
        return playHistoryDao.getPlayHistoryById(songId)?.toDomain()
    }
    
    /**
     * 从收藏获取缓存的歌曲
     */
    suspend fun getCachedFavoriteSong(songId: String): Song? {
        return favoriteSongDao.getFavoriteById(songId)?.toDomain()
    }
    
    /**
     * 保存歌曲到播放历史（用于缓存）
     */
    suspend fun cacheSong(song: Song) {
        // 同时存入播放历史以供缓存
        playHistoryDao.insertPlayHistory(song.toPlayHistoryEntity())
    }
    
    // ============ 歌词缓存 ============
    
    /**
     * 获取缓存的歌词
     */
    suspend fun getCachedLyrics(songId: String): Lyrics? {
        val entity = lyricsCacheDao.getLyrics(songId) ?: return null
        return Lyrics(
            songId = entity.songId,
            lrc = entity.lrc,
            yrc = entity.yrc,
            translation = entity.translation,
            ttml = entity.ttml,
            metadata = entity.metadata?.split("|") ?: emptyList()
        )
    }
    
    /**
     * 保存歌词到缓存
     */
    suspend fun cacheLyrics(lyrics: Lyrics) {
        lyricsCacheDao.insertLyrics(LyricsCacheEntity(
            songId = lyrics.songId,
            lrc = lyrics.lrc,
            yrc = lyrics.yrc,
            translation = lyrics.translation,
            ttml = lyrics.ttml,
            metadata = lyrics.metadata.joinToString("|")
        ))
    }
}
