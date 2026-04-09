package com.freemusic.domain.usecase

import com.freemusic.domain.model.*
import com.freemusic.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 搜索歌曲用例
 */
class SearchSongsUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    operator fun invoke(
        keyword: String,
        page: Int = 0,
        pageSize: Int = 20
    ): Flow<Result<SearchResult>> {
        return repository.searchSongs(keyword, page, pageSize)
    }
}

/**
 * 获取歌曲详情用例
 */
class GetSongDetailUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    operator fun invoke(songId: String): Flow<Result<Song>> {
        return repository.getSongDetail(songId)
    }
}

/**
 * 获取播放 URL 用例
 */
class GetPlayUrlUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    operator fun invoke(songId: String): Flow<Result<String>> {
        return repository.getPlayUrl(songId)
    }
}

/**
 * 获取歌词用例（优先使用 LRCLIB）
 */
class GetLyricsUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    operator fun invoke(song: Song): Flow<Result<Lyrics>> {
        return repository.getLyrics(song)
    }
}

/**
 * 获取歌曲和播放 URL 用例
 */
class GetSongWithUrlUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    operator fun invoke(songId: String): Flow<Result<SongWithUrl>> {
        return repository.getSongWithUrl(songId)
    }
}

/**
 * 获取歌单用例
 */
class GetPlaylistUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    operator fun invoke(playlistId: String): Flow<Result<Playlist>> {
        return repository.getPlaylist(playlistId)
    }
}
