package com.freemusic.data.remote.api

import com.freemusic.data.remote.dto.*
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 网易云音乐 API
 */
interface NeteaseApi {

    /**
     * 搜索歌曲
     * @param keywords 搜索关键词
     * @param limit 返回数量
     * @param offset 偏移量
     */
    @GET("cloudsearch")
    suspend fun searchSongs(
        @Query("keywords") keywords: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): NeteaseSearchResponse

    /**
     * 获取歌曲详情
     * @param ids 歌曲 ID，多个用逗号分隔
     */
    @GET("song/detail")
    suspend fun getSongDetail(
        @Query("ids") ids: String
    ): NeteaseSongDetailResponse

    /**
     * 获取歌词
     * @param id 歌曲 ID
     */
    @GET("lyric/new")
    suspend fun getLyric(
        @Query("id") id: String
    ): NeteaseLyricResponse

    /**
     * 获取歌单所有歌曲
     * @param id 歌单 ID
     * @param limit 每页数量
     * @param offset 偏移量
     */
    @GET("playlist/track/all")
    suspend fun getPlaylistTracks(
        @Query("id") id: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): NeteasePlaylistResponse
}
