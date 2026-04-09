package com.freemusic.data.remote.dto

import com.freemusic.domain.model.Lyrics
import com.freemusic.domain.model.Song

/**
 * 将网易云 DTO 转换为领域模型
 */

fun NeteaseSongDto.toDomain(): Song {
    return Song(
        id = id.toString(),
        title = name,
        artist = ar.joinToString("/") { it.name },
        album = al?.name ?: "",
        coverUrl = al?.picUrl?.replace("http:", "https:"),
        duration = dt,
        neteaseId = id.toString(),
        isNetease = true
    )
}

fun List<NeteaseSongDto>.toDomainList(): List<Song> {
    return map { it.toDomain() }
}

fun NeteaseLyricResponse.toDomain(songId: String): Lyrics {
    return Lyrics(
        songId = songId,
        lrc = lrc?.lyric,
        yrc = yrc?.lyric,
        translation = tlyric?.lyric ?: ytlrc?.lyric,
        ttml = null,
        metadata = buildMetadata()
    )
}

fun NeteaseLyricResponse.buildMetadata(): List<String> {
    val metadata = mutableListOf<String>()
    
    lyricUser?.nickname?.takeIf { it.isNotBlank() }?.let {
        metadata.add("歌词贡献者: $it")
    }
    
    transUser?.nickname?.takeIf { it.isNotBlank() }?.let {
        metadata.add("翻译贡献者: $it")
    }
    
    return metadata
}
