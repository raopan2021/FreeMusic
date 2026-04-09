package com.freemusic.data.remote.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Meting API - 获取音乐播放 URL
 * https://github.com/Binaryify/NeteaseCloudMusicApi
 */
interface MetingApi {

    /**
     * 获取歌曲播放 URL
     * @param type 来源类型，默认 url
     * @param id 歌曲 ID
     */
    @GET("meting/")
    suspend fun getPlayUrl(
        @Query("type") type: String = "url",
        @Query("id") id: String
    ): ResponseBody
}
