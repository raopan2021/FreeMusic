package com.freemusic.domain.model

/**
 * 歌曲领域模型
 */
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val coverUrl: String?,
    val duration: Long, // 毫秒
    val neteaseId: String? = null,
    val isNetease: Boolean = true,
    val playCount: Int = 0  // 播放次数
)

/**
 * 歌曲播放信息（包含播放 URL）
 */
data class SongWithUrl(
    val song: Song,
    val url: String
)

/**
 * 歌词
 */
data class Lyrics(
    val songId: String,
    val lrc: String?,      // 标准 LRC 歌词
    val yrc: String?,      // 网易逐字歌词
    val translation: String?, // 翻译
    val ttml: String?,     // TTML 格式
    val metadata: List<String> = emptyList() // 贡献者等信息
)

/**
 * 歌词行（解析后）
 */
data class LyricLine(
    val timeMs: Long,      // 时间戳（毫秒）
    val text: String,      // 纯文本
    val endTimeMs: Long? = null, // 结束时间（可选）
    val words: List<LyricWord> = emptyList()
)

/**
 * 歌词单词（逐字歌词用）
 */
data class LyricWord(
    val startMs: Long,
    val endMs: Long,
    val text: String
)

/**
 * 播放列表
 */
data class Playlist(
    val id: String,
    val name: String,
    val coverUrl: String?,
    val songs: List<Song> = emptyList()
)

/**
 * 搜索结果
 */
data class SearchResult(
    val songs: List<Song>,
    val hasMore: Boolean,
    val total: Int
)
