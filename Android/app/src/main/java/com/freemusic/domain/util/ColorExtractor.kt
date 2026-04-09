package com.freemusic.domain.util

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 * 从专辑封面提取主色调
 * 用于动态配色（Material You 风格）
 */
object ColorExtractor {

    /**
     * 从 Bitmap 提取主色调
     */
    suspend fun extractDominantColor(bitmap: Bitmap?): Int {
        if (bitmap == null) return 0xFF6200EE.toInt() // 默认紫色
        
        return withContext(Dispatchers.Default) {
            try {
                // 缩放图片以提高性能
                val scaled = Bitmap.createScaledBitmap(bitmap, 50, 50, true)
                
                // 计算颜色直方图
                val colorCounts = mutableMapOf<Int, Int>()
                
                for (x in 0 until scaled.width) {
                    for (y in 0 until scaled.height) {
                        val pixel = scaled.getPixel(x, y)
                        // 降低精度，合并相似颜色
                        val quantized = quantizeColor(pixel)
                        colorCounts[quantized] = (colorCounts[quantized] ?: 0) + 1
                    }
                }
                
                // 找到出现最多的颜色
                val dominant = colorCounts.maxByOrNull { it.value }?.key ?: 0xFF6200EE.toInt()
                
                // 确保颜色不太暗或太亮
                adjustColor(dominant)
            } catch (e: Exception) {
                0xFF6200EE.toInt()
            }
        }
    }

    /**
     * 量化颜色，减少颜色数量
     */
    private fun quantizeColor(color: Int): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        
        // 降低到 32 级
        val qr = (r / 8) * 8
        val qg = (g / 8) * 8
        val qb = (b / 8) * 8
        
        return Color.rgb(qr, qg, qb)
    }

    /**
     * 调整颜色，确保不太暗或太亮
     */
    private fun adjustColor(color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        
        // 确保饱和度和亮度在合理范围
        hsv[1] = hsv[1].coerceIn(0.3f, 0.9f)
        hsv[2] = hsv[2].coerceIn(0.3f, 0.85f)
        
        return Color.HSVToColor(hsv)
    }

    /**
     * 从 URL 加载图片并提取颜色（需要 Coil 的 BitmapFactory）
     * 这是一个简化版本，实际使用需要 Coil 的 imageLoader
     */
    fun Int.toComposeColor(): androidx.compose.ui.graphics.Color {
        return androidx.compose.ui.graphics.Color(this)
    }
}
