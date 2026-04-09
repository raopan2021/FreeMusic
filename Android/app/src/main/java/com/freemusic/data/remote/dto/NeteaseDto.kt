package com.freemusic.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 网易云音乐搜索响应
 */
@Serializable
data class NeteaseSearchResponse(
    val result: NeteaseSearchResult? = null,
    val code: Int = 0
)

@Serializable
data class NeteaseSearchResult(
    val songs: List<NeteaseSongDto>? = null
)

/**
 * 网易云歌曲 DTO
 */
@Serializable
data class NeteaseSongDto(
    val id: Long = 0,
    val name: String = "",
    val ar: List<NeteaseArtistDto> = emptyList(),
    val al: NeteaseAlbumDto? = null,
    val dt: Long = 0 // 时长，毫秒
)

@Serializable
data class NeteaseArtistDto(
    val name: String = ""
)

@Serializable
data class NeteaseAlbumDto(
    val name: String = "",
    val picUrl: String = ""
)

/**
 * 网易云歌曲详情响应
 */
@Serializable
data class NeteaseSongDetailResponse(
    val code: Int = 0,
    val songs: List<NeteaseSongDto>? = null
)

/**
 * 网易云歌词响应
 */
@Serializable
data class NeteaseLyricResponse(
    val lrc: NeteaseLyricContent? = null,
    val yrc: NeteaseLyricContent? = null,
    val tlyric: NeteaseLyricContent? = null,
    val ytlrc: NeteaseLyricContent? = null,
    val lyricUser: NeteaseLyricUser? = null,
    val transUser: NeteaseLyricUser? = null,
    val code: Int = 0
)

@Serializable
data class NeteaseLyricContent(
    val lyric: String = ""
)

@Serializable
data class NeteaseLyricUser(
    val nickname: String = ""
)

/**
 * 网易云歌单响应
 */
@Serializable
data class NeteasePlaylistResponse(
    val songs: List<NeteaseSongDto>? = null,
    val code: Int = 0
)
