package com.freemusic.presentation.ui.player

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freemusic.R
import com.freemusic.presentation.viewmodel.QueueItem
import java.util.Collections

/**
 * RecyclerView 队列列表（支持拖动排序）
 * 使用 ItemTouchHelper 实现拖动，比 Compose Reorderable 更适合 400+ 大量数据
 */
@SuppressLint("ClickableViewAccessibility")
@Composable
fun QueueRecyclerView(
    queueItems: List<QueueItem>,
    currentIndex: Int,
    onRemove: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onPlay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // 颜色
    val highlightColor = Color(0x4D6200EE).toArgb()
    val primaryColor = Color(0xFF6200EE).toArgb()
    val normalTextColor = Color(0xFF000000).toArgb()
    val secondaryTextColor = Color(0xFF888888).toArgb()

    // 内部 mutable 列表，ItemTouchHelper 直接操作
    val items = remember { mutableStateListOf<QueueItem>() }

    // 外部列表变化时同步
    DisposableEffect(queueItems) {
        items.clear()
        items.addAll(queueItems)
        onDispose { }
    }

    var draggingFromIndex by remember { mutableIntStateOf(-1) }
    var draggingToIndex by remember { mutableIntStateOf(-1) }

    Box(modifier = modifier.fillMaxWidth()) {
        AndroidView(
            factory = { ctx ->
                RecyclerView(ctx).apply {
                    layoutManager = LinearLayoutManager(ctx)
                    setBackgroundColor(Color.Transparent.toArgb())

                    val adapter = QueueAdapter(
                        items = items,
                        currentIndex = currentIndex,
                        highlightColor = highlightColor,
                        primaryColor = primaryColor,
                        normalTextColor = normalTextColor,
                        secondaryTextColor = secondaryTextColor,
                        onRemove = { index -> onRemove(index) },
                        onPlay = { index -> onPlay(index) }
                    )
                    this.adapter = adapter

                    val callback = object : ItemTouchHelper.SimpleCallback(
                        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                        0
                    ) {
                        override fun onMove(
                            recyclerView: RecyclerView,
                            viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder
                        ): Boolean {
                            val fromPos = viewHolder.adapterPosition
                            val toPos = target.adapterPosition
                            if (fromPos == RecyclerView.NO_POSITION || toPos == RecyclerView.NO_POSITION) return false
                            Collections.swap(items, fromPos, toPos)
                            adapter.notifyItemMoved(fromPos, toPos)
                            if (draggingFromIndex < 0) draggingFromIndex = fromPos
                            draggingToIndex = toPos
                            return true
                        }

                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

                        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                            super.onSelectedChanged(viewHolder, actionState)
                            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                                viewHolder?.itemView?.alpha = 0.7f
                            }
                        }

                        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                            super.clearView(recyclerView, viewHolder)
                            viewHolder.itemView.alpha = 1.0f
                            // 拖动结束后，通知 ViewModel 数据已变化
                            if (draggingFromIndex >= 0 && draggingToIndex >= 0 && draggingFromIndex != draggingToIndex) {
                                onMove(draggingFromIndex, draggingToIndex)
                            }
                            draggingFromIndex = -1
                            draggingToIndex = -1
                        }

                        override fun isLongPressDragEnabled(): Boolean = false
                    }

                    val touchHelper = ItemTouchHelper(callback)
                    touchHelper.attachToRecyclerView(this)

                    // 长按开始拖动
                    setOnTouchListener { _, event ->
                        if (event.action == MotionEvent.ACTION_DOWN) {
                            // 让 ItemTouchHelper 处理
                        }
                        false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            update = { rv ->
                // 数据整体刷新
                items.clear()
                items.addAll(queueItems)
                rv.adapter?.notifyDataSetChanged()
            }
        )
    }
}

private class QueueAdapter(
    private val items: MutableList<QueueItem>,
    private val currentIndex: Int,
    private val highlightColor: Int,
    private val primaryColor: Int,
    private val normalTextColor: Int,
    private val secondaryTextColor: Int,
    private val onRemove: (Int) -> Unit,
    private val onPlay: (Int) -> Unit
) : RecyclerView.Adapter<QueueAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: LinearLayout = view.findViewById(R.id.itemContainer)
        val title: TextView = view.findViewById(R.id.songTitle)
        val artist: TextView = view.findViewById(R.id.songArtist)
        val removeBtn: ImageButton = view.findViewById(R.id.removeBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_queue, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val isCurrentSong = position == currentIndex

        holder.title.text = item.song.title
        holder.title.setTextColor(if (isCurrentSong) primaryColor else normalTextColor)
        holder.title.setTypeface(null, if (isCurrentSong) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
        holder.artist.text = item.song.artist ?: "未知艺术家"
        holder.artist.setTextColor(secondaryTextColor)
        holder.container.setBackgroundColor(if (isCurrentSong) highlightColor else android.graphics.Color.TRANSPARENT)

        holder.itemView.setOnClickListener {
            if (!isCurrentSong) onPlay(position)
        }
        holder.removeBtn.setOnClickListener {
            onRemove(position)
        }
    }

    override fun getItemCount(): Int = items.size
}
