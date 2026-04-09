package com.freemusic.data.local.entity

import com.freemusic.domain.model.Song

/**
 * 实体转领域模型
 */

fun FavoriteSongEntity.toDomain(): Song {
    return Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        coverUrl = coverUrl,
        duration = duration,
        neteaseId = neteaseId,
        isNetease = neteaseId != null
    )
}

fun Song.toFavoriteEntity(): FavoriteSongEntity {
    return FavoriteSongEntity(
        id = id,
        title = title,
        artist = artist,
        album = album,
        coverUrl = coverUrl,
        duration = duration,
        neteaseId = neteaseId
    )
}
