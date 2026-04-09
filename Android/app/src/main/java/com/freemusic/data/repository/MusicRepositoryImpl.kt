package com.freemusic.data.repository

import com.freemusic.data.remote.api.MetingApi
import com.freemusic.data.remote.api.NeteaseApi
import com.freemusic.data.remote.dto.toDomain
import com.freemusic.data.remote.dto.toDomainList
import com.freemusic.data.remote.dto.toDomain as lyricToDomain
import com.freemusic.domain.model.*
import com.freemusic.domain.repository.MusicRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val neteaseApi: NeteaseApi,
    private val metingApi: MetingApi
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
            
            val songs = response.result?.songs?.toDomainList() ?: emptyList()
            val hasMore = songs.size == pageSize
            
            emit(Result.success(SearchResult(
                songs = songs,
                hasMore = hasMore,
                total = response.result?.songs?.size ?: 0
            )))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getSongDetail(songId: String): Flow<Result<Song>> = flow {
        try {
            val response = neteaseApi.getSongDetail(songId)
            val song = response.songs?.firstOrNull()?.toDomain()
            if (song != null) {
                emit(Result.success(song))
            } else {
                emit(Result.failure(Exception("Song not found")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getPlayUrl(songId: String): Flow<Result<String>> = flow {
        try {
            // Meting API 直接返回 URL
            val responseBody = metingApi.getPlayUrl(id = songId)
            val url = responseBody.string()
            emit(Result.success(url))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getLyrics(songId: String): Flow<Result<Lyrics>> = flow {
        try {
            val response = neteaseApi.getLyric(songId)
            emit(Result.success(response.lyricToDomain(songId)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getSongWithUrl(songId: String): Flow<Result<SongWithUrl>> = flow {
        try {
            // 并行获取歌曲详情和播放 URL
            val songDeferred = async { neteaseApi.getSongDetail(songId) }
            val urlDeferred = async { metingApi.getPlayUrl(id = songId) }
            
            val songResponse = songDeferred.await()
            val urlResponse = urlDeferred.await()
            
            val song = songResponse.songs?.firstOrNull()?.toDomain()
            val url = urlResponse.string()
            
            if (song != null) {
                emit(Result.success(SongWithUrl(song, url)))
            } else {
                emit(Result.failure(Exception("Song not found")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
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
                name = "", // 歌单名称需要单独 API 获取
                coverUrl = allSongs.firstOrNull()?.coverUrl,
                songs = allSongs
            )))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun <T> async(block: suspend () -> T): Deferred<T> {
        return CoroutineScope(Dispatchers.IO).async { block() }
    }
}
