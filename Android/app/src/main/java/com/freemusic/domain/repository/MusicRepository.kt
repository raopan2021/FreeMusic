package com.freemusic.domain.repository

import com.freemusic.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * 音乐仓库接口
 */
interface MusicRepository {

    /**
     * 搜索歌曲
     * @param keyword 搜索关键词
     * @param page 页码
     * @param pageSize 每页数量
     */
    fun searchSongs(
        keyword: String,
        page: Int = 0,
        pageSize: Int = 20
    ): Flow<Result<SearchResult>>

    /**
     * 获取歌曲详情
     * @param songId 歌曲 ID
     */
    fun getSongDetail(songId: String): Flow<Result<Song>>

    /**
     * 获取播放 URL
     * @param songId 歌曲 ID
     */
    fun getPlayUrl(songId: String): Flow<Result<String>>

    /**
     * 获取歌词
     * @param songId 歌曲 ID
     */
    fun getLyrics(songId: String): Flow<Result<Lyrics>>

    /**
     * 获取歌曲和播放 URL
     */
    fun getSongWithUrl(songId: String): Flow<Result<SongWithUrl>>

    /**
     * 获取歌单
     * @param playlistId 歌单 ID
     */
    fun getPlaylist(playlistId: String): Flow<Result<Playlist>>
}
