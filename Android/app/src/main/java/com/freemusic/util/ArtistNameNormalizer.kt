package com.freemusic.util

/**
 * 歌手名字标准化工具
 */
object ArtistNameNormalizer {

    /**
     * 标准化歌手名字：统一大小写、去除多余空格、规范化标点
     */
    fun normalize(name: String): String {
        if (name.isBlank()) return name
        
        return name
            // 统一空格：多个空格变一个
            .replace(Regex("\\s+"), " ")
            // 去除首尾空格
            .trim()
            // 规范化英文标点空格（如 "g.e.m.  邓紫棋" -> "g.e.m. 邓紫棋"）
            .replace(Regex("([a-zA-Z])\\.\\s*"), "$1. ")
            .replace(Regex("\\s+\\."), ".")
            // 规范化中文和英文之间的空格
            .replace(Regex("([\\u4e00-\\u9fa5])([a-zA-Z])"), "$1 $2")
            .replace(Regex("([a-zA-Z])([\\u4e00-\\u9fa5])"), "$1 $2")
            // 再次清理多余空格
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * 检查是否是"未知艺术家"
     */
    fun isUnknown(name: String?): Boolean {
        if (name.isNullOrBlank()) return true
        val normalized = normalize(name).lowercase()
        return normalized in listOf("unknown", "未知", "未知艺术家", "<unknown>", "unknown artist")
    }

    /**
     * 尝试从歌名中提取歌手名
     * 例如："Artist - Song Name" 或 "Artist: Song Name"
     */
    fun extractArtistFromTitle(title: String): Pair<String?, String> {
        // Pattern: "Artist - Song" or "Artist: Song" or "Artist – Song"
        val pattern = Regex("^(.+?)\\s*[-\\u2013\\u2014:\\uFF1a\\u2502]\\s*(.+)$", RegexOption.IGNORE_CASE)
        val match = pattern.find(title)
        
        if (match != null) {
            val potentialArtist = match.groupValues[1].trim()
            val songTitle = match.groupValues[2].trim()
            // 如果提取的歌手看起来合理（非空，不太短）
            if (potentialArtist.length >= 2 && !isUnknown(potentialArtist)) {
                return Pair(normalize(potentialArtist), songTitle)
            }
        }
        
        return Pair(null, title)
    }

    /**
     * 匹配歌手名（模糊匹配）
     * 返回匹配度 (0.0 - 1.0)
     */
    fun matchScore(name1: String, name2: String): Double {
        val n1 = normalize(name1).lowercase()
        val n2 = normalize(name2).lowercase()
        
        if (n1 == n2) return 1.0
        
        // 检查包含关系
        if (n1.contains(n2) || n2.contains(n1)) {
            return 0.8
        }
        
        // 检查相似度（简单实现）
        val longer = if (n1.length > n2.length) n1 else n2
        val shorter = if (n1.length > n2.length) n2 else n1
        
        if (longer.startsWith(shorter)) {
            return 0.7
        }
        
        return 0.0
    }

    /**
     * 从候选列表中找到最佳匹配的歌手
     */
    fun findBestMatch(inputName: String, candidates: List<String>): String? {
        if (inputName.isBlank() || candidates.isEmpty()) return null
        
        val normalizedInput = normalize(inputName)
        
        // 首先精确匹配
        candidates.find { normalize(it) == normalizedInput }?.let { return it }
        
        // 模糊匹配
        var bestMatch: String? = null
        var bestScore = 0.0
        
        for (candidate in candidates) {
            val score = matchScore(normalizedInput, candidate)
            if (score > bestScore) {
                bestScore = score
                bestMatch = candidate
            }
        }
        
        return if (bestScore >= 0.7) bestMatch else null
    }
}
