package com.freemusic.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * iTunes Search API - 免费专辑封面搜索
 * https://developer.apple.com/library/archive/documentation/AudioVideo/Conceptual/iTuneSearchAPI/
 */
interface ItunesApi {

    /**
     * 搜索专辑封面
     * @param term 搜索关键词（歌曲名+艺术家）
     * @param media 媒体类型（music）
     * @param entity 实体类型（album）
     * @param limit 返回数量
     */
    @GET("search")
    suspend fun searchAlbum(
        @Query("term") term: String,
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "album",
        @Query("limit") limit: Int = 5
    ): ItunesSearchResponse
}

/**
 * iTunes 搜索响应
 */
@kotlinx.serialization.Serializable
data class ItunesSearchResponse(
    val resultCount: Int = 0,
    val results: List<ItunesAlbum> = emptyList()
)

@kotlinx.serialization.Serializable
data class ItunesAlbum(
    val collectionId: Long = 0,
    val collectionName: String = "",
    val artistName: String = "",
    val artworkUrl100: String? = null,
    val artworkUrl60: String? = null,
    val artworkUrl600: String? = null
) {
    /**
     * 获取最大尺寸的封面 URL
     */
    fun getBestArtwork(): String? {
        return artworkUrl600 ?: artworkUrl100 ?: artworkUrl60
    }
}
