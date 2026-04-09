package com.freemusic.domain.lyrics

import com.freemusic.domain.model.LyricLine
import com.freemusic.domain.model.LyricWord

/**
 * 歌词解析器
 * 支持标准 LRC 和网易云逐字歌词 (YRC)
 */
object LyricsParser {

    /**
     * 解析标准 LRC 歌词
     */
    fun parseLrc(lrcContent: String?): List<LyricLine> {
        if (lrcContent.isNullOrBlank()) return emptyList()

        val lines = lrcContent.lines()
            .filter { it.isNotBlank() }
            .mapNotNull { parseLrcLine(it) }
            .sortedBy { it.timeMs }

        return mergeDuplicateLines(lines)
    }

    /**
     * 解析网易云逐字歌词 (YRC)
     * 格式: [00:12.34]<00:01.56>歌<00:01.89>词
     */
    fun parseYrc(yrcContent: String?): List<LyricLine> {
        if (yrcContent.isNullOrBlank()) return emptyList()

        val lines = yrcContent.lines()
            .filter { it.isNotBlank() }
            .mapNotNull { parseYrcLine(it) }
            .flatten()
            .sortedBy { it.timeMs }

        return mergeDuplicateLines(lines)
    }

    /**
     * 解析单行 LRC
     * 格式: [mm:ss.xx] 或 [mm:ss.xxx] 歌词内容
     */
    private fun parseLrcLine(line: String): LyricLine? {
        val timeTagRegex = Regex("""\[(\d{2}):(\d{2})(?:\.(\d{2,3}))?\]""")
        val matches = timeTagRegex.findAll(line).toList()

        if (matches.isEmpty()) return null

        // 获取最后一组时间戳作为该行的时间
        val lastMatch = matches.last()
        val minutes = lastMatch.groupValues[1].toIntOrNull() ?: return null
        val seconds = lastMatch.groupValues[2].toIntOrNull() ?: return null
        val millis = parseMillis(lastMatch.groupValues[3])

        val timeMs = (minutes * 60 + seconds) * 1000L + millis
        val text = line.substringAfterLast("]").trim()

        return LyricLine(timeMs = timeMs, text = text)
    }

    /**
     * 解析单行 YRC
     * 格式: [mm:ss.xx]<mm:ss.xx>字1<mm:ss.xx>字2...
     */
    private fun parseYrcLine(line: String): List<LyricLine>? {
        // 匹配行首时间戳 [mm:ss.xx]
        val lineTimeRegex = Regex("""^\[(\d{2}):(\d{2})(?:\.(\d{2,3}))?\]""")
        val lineTimeMatch = lineTimeRegex.find(line) ?: return null

        val lineMinutes = lineTimeMatch.groupValues[1].toIntOrNull() ?: return null
        val lineSeconds = lineTimeMatch.groupValues[2].toIntOrNull() ?: return null
        val lineMillis = parseMillis(lineTimeMatch.groupValues[3])
        val lineTimeMs = (lineMinutes * 60 + lineSeconds) * 1000L + lineMillis

        // 匹配逐字时间戳 <mm:ss.xx>字
        val wordTimeRegex = Regex("""<(\d{2}):(\d{2})(?:\.(\d{2,3}))?>(.)""")
        val wordMatches = wordTimeRegex.findAll(line).toList()

        if (wordMatches.isEmpty()) return null

        val words = wordMatches.map { match ->
            val minutes = match.groupValues[1].toInt()
            val seconds = match.groupValues[2].toInt()
            val millis = parseMillis(match.groupValues[3])
            val startMs = (minutes * 60 + seconds) * 1000L + millis
            val text = match.groupValues[4]

            LyricWord(startMs = startMs, endMs = 0, text = text)
        }

        // 计算每个字的结束时间
        val wordsWithEndTime = words.mapIndexed { index, word ->
            val endMs = if (index < words.size - 1) {
                words[index + 1].startMs
            } else {
                word.startMs + 2000 // 最后一字假设2秒
            }
            LyricWord(word.startMs, endMs, word.text)
        }

        // 合并所有字作为一行
        val fullText = words.joinToString("") { it.text }

        return listOf(
            LyricLine(
                timeMs = lineTimeMs,
                text = fullText,
                words = wordsWithEndTime
            )
        )
    }

    /**
     * 解析毫秒部分
     */
    private fun parseMillis(millisStr: String): Int {
        return when (millisStr.length) {
            0 -> 0
            1 -> millisStr.toIntOrNull()?.times(100) ?: 0
            2 -> millisStr.toIntOrNull()?.times(10) ?: 0
            else -> millisStr.take(3).toIntOrNull() ?: 0
        }
    }

    /**
     * 合并时间相同的多行
     */
    private fun mergeDuplicateLines(lines: List<LyricLine>): List<LyricLine> {
        return lines.groupBy { it.timeMs }
            .map { (_, grouped) ->
                if (grouped.size == 1) {
                    grouped.first()
                } else {
                    // 合并多行歌词（取第一行，保留逐字信息）
                    val first = grouped.first()
                    val combinedText = grouped.map { it.text }.filter { it.isNotBlank() }.joinToString(" ")
                    first.copy(text = combinedText)
                }
            }
    }

    /**
     * 找到当前播放位置对应的歌词行
     */
    fun findCurrentLine(lines: List<LyricLine>, positionMs: Long): Int {
        return lines.indexOfLast { it.timeMs <= positionMs }.coerceAtLeast(0)
    }

    /**
     * 过滤空白歌词行
     */
    fun filterBlankLines(lines: List<LyricLine>): List<LyricLine> {
        return lines.filter { it.text.isNotBlank() }
    }
}
