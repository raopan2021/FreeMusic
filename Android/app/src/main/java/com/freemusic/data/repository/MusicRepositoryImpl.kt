package com.freemusic.data.repository

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
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val neteaseApi: NeteaseApi,
    private val metingApi: MetingApi,
    private val lrclibApi: LrclibApi
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
        try {
            val response = neteaseApi.getSongDetail(songId)
            val song = response.songs?.firstOrNull()?.toDomain()
            
            if (song != null) {
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
            // Meting API 直接返回 URL
            val responseBody = metingApi.getPlayUrl(id = songId)
            val url = responseBody.string()
            emit(Result.success(url))
        } catch (e: Exception) {
            emit(Result.failure(Exception("无法获取播放链接: ${e.message}")))
        }
    }.flowOn(Dispatchers.IO)

    override fun getLyrics(song: Song): Flow<Result<Lyrics>> = flow {
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
                emit(Result.success(Lyrics(
                    songId = song.id,
                    lrc = bestMatch.syncedLyrics ?: bestMatch.plainLyrics,
                    yrc = null,
                    translation = null,
                    ttml = null,
                    metadata = listOf("来源: LRCLIB", "艺术家: ${bestMatch.artistName}", "专辑: ${bestMatch.albumName}")
                )))
                return@flow
            }
        } catch (e: Exception) {
            // LRCLIB 失败，继续尝试网易云
        }
        
        // 兜底：使用网易云歌词
        try {
            val neteaseId = song.neteaseId ?: song.id
            val response = neteaseApi.getLyric(neteaseId)
            emit(Result.success(response.lyricToDomain(song.id)))
        } catch (e: Exception) {
            emit(Result.failure(Exception("获取歌词失败: ${e.message}")))
        }
    }.flowOn(Dispatchers.IO)

    override fun getSongWithUrl(songId: String): Flow<Result<SongWithUrl>> = flow {
        try {
            // 获取歌曲详情
            val songResponse = neteaseApi.getSongDetail(songId)
            val song = songResponse.songs?.firstOrNull()?.toDomain()
                ?: run {
                    emit(Result.failure(Exception("歌曲不存在")))
                    return@flow
                }
            
            // 获取播放 URL
            val urlResponse = metingApi.getPlayUrl(id = songId)
            val url = urlResponse.string()
            
            if (url.isNotBlank()) {
                emit(Result.success(SongWithUrl(song, url)))
            } else {
                emit(Result.failure(Exception("播放链接无效")))
            }
        } catch (e: Exception) {
            emit(Result.failure(Exception("播放失败: ${e.message}")))
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
