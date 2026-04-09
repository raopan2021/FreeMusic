package com.freemusic.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * LRCLIB API - 免费开源歌词 API
 * https://lrclib.net
 */
interface LrclibApi {

    /**
     * 搜索歌词
     * @param artistName 艺术家名称
     * @param trackName 歌曲名称
     * @param duration 歌曲时长（秒），可选
     */
    @GET("api/search")
    suspend fun searchLyrics(
        @Query("artist_name") artistName: String,
        @Query("track_name") trackName: String,
        @Query("duration") duration: Int? = null
    ): List<LrclibLyric>

    /**
     * 获取指定歌词
     * @param id 歌词 ID
     */
    @GET("api/get")
    suspend fun getLyrics(
        @Query("id") id: Int
    ): LrclibLyric
}

/**
 * LRCLIB 歌词响应
 */
@kotlinx.serialization.Serializable
data class LrclibLyric(
    val id: Int = 0,
    val name: String = "",
    val trackName: String = "",
    val artistName: String = "",
    val albumName: String = "",
    val duration: Double = 0.0,
    val instrumental: Boolean = false,
    val plainLyrics: String? = null,
    val syncedLyrics: String? = null
)
