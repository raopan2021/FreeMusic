package com.freemusic.data.repository

import com.freemusic.data.local.LocalDataSource
import com.freemusic.data.remote.api.LrclibApi
import com.freemusic.data.remote.api.MetingApi
import com.freemusic.data.remote.api.NeteaseApi
import com.freemusic.data.remote.dto.toDomain
import com.freemusic.data.remote.dto.toDomainList
import com.freemusic.data.remote.dto.toDomain as lyricToDomain
import com.freemusic.domain.model.*
import com.freemusic.domain.repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val neteaseApi: NeteaseApi,
    private val metingApi: MetingApi,
    @Named("lrclib") private val lrclibApi: LrclibApi,
    private val localDataSource: LocalDataSource
) : MusicRepository {

    override fun searchSongs(
        keyword: String,
        page: Int,
        pageSize: Int
    ): Flow<Result<SearchResult>> = flow {
        try {
            val offset = page * pageSize
            val response = neteaseApi.searchSongs(
                keywords = keyword,
                limit = pageSize,
                offset = offset
            )
            
            // 检查响应是否有效
            if (response.result == null) {
                emit(Result.success(SearchResult(
                    songs = emptyList(),
                    hasMore = false,
                    total = 0
                )))
                return@flow
            }
            
            val songs = response.result.songs?.toDomainList() ?: emptyList()
            val hasMore = songs.size == pageSize
            
            emit(Result.success(SearchResult(
                songs = songs,
                hasMore = hasMore,
                total = response.result.songs?.size ?: 0
            )))
        } catch (e: Exception) {
            // 网络错误，返回空结果
            emit(Result.success(SearchResult(
                songs = emptyList(),
                hasMore = false,
                total = 0
            )))
        }
    }.flowOn(Dispatchers.IO)

    override fun getSongDetail(songId: String): Flow<Result<Song>> = flow {
        // 先检查本地缓存
        val cachedSong = localDataSource.getCachedSong(songId)
            ?: localDataSource.getCachedFavoriteSong(songId)
        
        // 如果有缓存，先返回缓存（包含coverUrl）
        if (cachedSong != null) {
            emit(Result.success(cachedSong))
            return@flow
        }
        
        // 缓存没有，从网络获取
        try {
            val response = neteaseApi.getSongDetail(songId)
            val song = response.songs?.firstOrNull()?.toDomain()
            
            if (song != null) {
                // 存入缓存
                localDataSource.cacheSong(song)
                emit(Result.success(song))
            } else {
                emit(Result.failure(Exception("歌曲不存在")))
            }
        } catch (e: Exception) {
            emit(Result.failure(Exception("获取歌曲详情失败: ${e.message}")))
        }
    }.flowOn(Dispatchers.IO)

    override fun getPlayUrl(songId: String): Flow<Result<String>> = flow {
        try {
            // Meting API 返回可能是 URL 字符串或 JSON
            val responseBody = metingApi.getPlayUrl(id = songId)
            val rawResponse = responseBody.string().trim()
            
            // 尝试解析为 JSON
            val url = try {
                if (rawResponse.startsWith("{")) {
                    // JSON 对象，尝试提取 url 字段
                    val jsonRegex = Regex(""""url""\s*:\s*"([^"]+)"""")
                    jsonRegex.find(rawResponse)?.groupValues?.get(1) 
                        ?: rawResponse // 如果解析失败，返回原始内容
                } else if (rawResponse.startsWith("[")) {
                    // JSON 数组，尝试提取第一个 url
                    val arrayUrlRegex = Regex(""""url""\s*:\s*"([^"]+)"""")
                    arrayUrlRegex.find(rawResponse)?.groupValues?.get(1)
                        ?: rawResponse
                } else {
                    // 假设是直接的 URL
                    rawResponse
                }
            } catch (e: Exception) {
                rawResponse
            }
            
            if (url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                emit(Result.success(url))
            } else {
                emit(Result.failure(Exception("播放链接无效或为空")))
            }
        } catch (e: Exception) {
            emit(Result.failure(Exception("无法获取播放链接: ${e.message}")))
        }
    }.flowOn(Dispatchers.IO)

    override fun getLyrics(song: Song): Flow<Result<Lyrics>> = flow {
        // 先检查本地缓存
        val cachedLyrics = localDataSource.getCachedLyrics(song.id)
        if (cachedLyrics != null && (cachedLyrics.lrc != null || cachedLyrics.yrc != null)) {
            emit(Result.success(cachedLyrics))
            return@flow
        }
        
        // 缓存没有，从网络获取
        // 优先使用 LRCLIB
        try {
            val durationSeconds = (song.duration / 1000).toInt()
            val results = lrclibApi.searchLyrics(
                artistName = song.artist,
                trackName = song.title,
                duration = durationSeconds.takeIf { it > 0 }
            )
            
            // 找到最佳匹配
            val bestMatch = results.firstOrNull { !it.instrumental }
            
            if (bestMatch != null && (bestMatch.syncedLyrics != null || bestMatch.plainLyrics != null)) {
                val lyrics = Lyrics(
                    songId = song.id,
                    lrc = bestMatch.syncedLyrics ?: bestMatch.plainLyrics,
                    yrc = null,
                    translation = null,
                    ttml = null,
                    metadata = listOf("来源: LRCLIB", "艺术家: ${bestMatch.artistName}", "专辑: ${bestMatch.albumName}")
                )
                // 存入缓存
                localDataSource.cacheLyrics(lyrics)
                emit(Result.success(lyrics))
                return@flow
            }
        } catch (e: Exception) {
            // LRCLIB 失败，继续尝试网易云
        }
        
        // 兜底：使用网易云歌词
        try {
            val neteaseId = song.neteaseId ?: song.id
            val response = neteaseApi.getLyric(neteaseId)
            val lyrics = response.lyricToDomain(song.id)
            // 存入缓存
            localDataSource.cacheLyrics(lyrics)
            emit(Result.success(lyrics))
        } catch (e: Exception) {
            emit(Result.failure(Exception("获取歌词失败: ${e.message}")))
        }
    }.flowOn(Dispatchers.IO)

    override fun getSongWithUrl(songId: String): Flow<Result<SongWithUrl>> = flow {
        // 先检查本地缓存
        val cachedSong = localDataSource.getCachedSong(songId)
            ?: localDataSource.getCachedFavoriteSong(songId)
        
        try {
            // 获取歌曲详情（优先用缓存的coverUrl）
            val songResponse = neteaseApi.getSongDetail(songId)
            val networkSong = songResponse.songs?.firstOrNull()?.toDomain()
            
            // 使用网络数据，但保留缓存的coverUrl（如果有）
            val song = networkSong?.copy(
                coverUrl = networkSong.coverUrl ?: cachedSong?.coverUrl
            ) ?: cachedSong?.also {
                emit(Result.failure(Exception("歌曲不存在")))
                return@flow
            } ?: run {
                emit(Result.failure(Exception("歌曲不存在")))
                return@flow
            }
            
            // 存入缓存
            localDataSource.cacheSong(song)
            
            // 获取播放 URL
            val rawResponse = metingApi.getPlayUrl(id = songId).string().trim()
            
            // 尝试解析为 JSON
            val url = try {
                if (rawResponse.startsWith("{")) {
                    val jsonRegex = Regex(""""url""\s*:\s*"([^"]+)"""")
                    jsonRegex.find(rawResponse)?.groupValues?.get(1) ?: rawResponse
                } else if (rawResponse.startsWith("[")) {
                    val arrayUrlRegex = Regex(""""url""\s*:\s*"([^"]+)"""")
                    arrayUrlRegex.find(rawResponse)?.groupValues?.get(1) ?: rawResponse
                } else {
                    rawResponse
                }
            } catch (e: Exception) {
                rawResponse
            }
            
            if (url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                emit(Result.success(SongWithUrl(song, url)))
            } else {
                emit(Result.failure(Exception("播放链接无效")))
            }
        } catch (e: Exception) {
            // 网络失败时，如果有缓存，返回缓存数据
            if (cachedSong != null) {
                val rawResponse = metingApi.getPlayUrl(id = songId).string().trim()
                val url = try {
                    if (rawResponse.startsWith("{")) {
                        val jsonRegex = Regex(""""url""\s*:\s*"([^"]+)"""")
                        jsonRegex.find(rawResponse)?.groupValues?.get(1) ?: rawResponse
                    } else {
                        rawResponse
                    }
                } catch (e: Exception) {
                    rawResponse
                }
                if (url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                    emit(Result.success(SongWithUrl(cachedSong, url)))
                } else {
                    emit(Result.failure(Exception("播放失败: ${e.message}")))
                }
            } else {
                emit(Result.failure(Exception("播放失败: ${e.message}")))
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun getPlaylist(playlistId: String): Flow<Result<Playlist>> = flow {
        try {
            val allSongs = mutableListOf<Song>()
            var offset = 0
            val limit = 50
            var hasMore = true

            while (hasMore) {
                val response = neteaseApi.getPlaylistTracks(
                    id = playlistId,
                    limit = limit,
                    offset = offset
                )
                
                val songs = response.songs?.toDomainList() ?: emptyList()
                allSongs.addAll(songs)
                
                hasMore = songs.size == limit
                offset += limit
            }

            emit(Result.success(Playlist(
                id = playlistId,
                name = "",
                coverUrl = allSongs.firstOrNull()?.coverUrl,
                songs = allSongs
            )))
        } catch (e: Exception) {
            emit(Result.failure(Exception("获取歌单失败: ${e.message}")))
        }
    }.flowOn(Dispatchers.IO)
}
