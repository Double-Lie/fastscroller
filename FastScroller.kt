// FastScroller.kt
//package com.example.main

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.compose.animation.fadeOut
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FastScroller @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {


    private var isVisible = false
    private val fadeDuration = 300L
    private val hideDelay = 3000L
    private val hideRunnable = Runnable { fadeOut() }
    private val handler = Handler(Looper.getMainLooper())


    // 滑块尺寸参数（最大和最小高度）
    private val minThumbHeight = 500f
    private val maxThumbHeight = 800f
    private var currentThumbHeight = 600f  // 当前滑块高度
    private val thumbWidth = 40f
    private val thumbPadding = 4f
    private val thumbRadius = 8f
    private val thumbColor = ContextCompat.getColor(context, R.color.primary)
    private val trackColor = ContextCompat.getColor(context, R.color.surface_variant)
    
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = thumbColor
    }
    
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = trackColor
        alpha = 150
    }
    
    private var recyclerView: RecyclerView? = null
    private var isDragging = false
    
    private val thumbRect = RectF()
    private val trackRect = RectF()



    // 添加淡入淡出方法
    private fun fadeIn() {
        if (isVisible) return
        isVisible = true
        animate()
            .alpha(1f)
            .setDuration(fadeDuration)
            .start()
        scheduleHide()
    }

    private fun fadeOut() {
        if (!isVisible) return
        isVisible = false
        animate()
            .alpha(0f)
            .setDuration(fadeDuration)
            .start()
    }

    private fun scheduleHide() {
        handler.removeCallbacks(hideRunnable)
        handler.postDelayed(hideRunnable, hideDelay)
    }

    // 修改 attachToRecyclerView 方法
    fun attachToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        alpha = 0f // 初始设置为透明
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!isDragging) {
                    updateThumbPosition()
                    fadeIn() // 滚动时显示
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    scheduleHide() // 停止滚动后开始隐藏计时
                } else {
                    handler.removeCallbacks(hideRunnable) // 滚动时取消隐藏
                }
            }
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // 绘制轨道
        canvas.drawRoundRect(trackRect, thumbRadius, thumbRadius, trackPaint)
        
        // 绘制滑块
        canvas.drawRoundRect(thumbRect, thumbRadius, thumbRadius, thumbPaint)
    }

    // 修改 onTouchEvent 方法
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (thumbRect.contains(event.x, event.y)) {
                    fadeIn() // 显示时淡入
                    isDragging = true
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    scheduleHide() // 拖动时重置隐藏计时
                    scrollRecyclerView(event.y)
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                scheduleHide() // 结束操作后开始隐藏计时
            }
        }
        return super.onTouchEvent(event)
    }


    /*private fun updateThumbPosition() {
        recyclerView?.let { rv ->
            val layoutManager = rv.layoutManager as? LinearLayoutManager ?: return
            val adapter = rv.adapter ?: return
            
            if (adapter.itemCount == 0) return
            
            val firstVisible = layoutManager.findFirstVisibleItemPosition()
            val lastVisible = layoutManager.findLastVisibleItemPosition()
            val visibleRange = lastVisible - firstVisible

            val top = (firstVisible.toFloat() / adapter.itemCount) * height
            thumbRect.set(
                width - thumbWidth - thumbPadding,
                top,
                width - thumbPadding,
                top + thumbHeight
            )
            
            trackRect.set(
                width - thumbWidth - thumbPadding - 4,
                0f,
                width - thumbPadding + 4,
                height.toFloat()
            )
            
            invalidate()
        }
    }


    private fun updateThumbPosition() {
        recyclerView?.let { rv ->
            val visibleHeight = rv.height - rv.paddingTop - rv.paddingBottom

            val layoutManager = rv.layoutManager as? LinearLayoutManager ?: return
            val adapter = rv.adapter ?: return

            if (adapter.itemCount == 0) return

            // 动态计算滑块高度（根据数据量在500f-800f之间）
            currentThumbHeight = when {
                adapter.itemCount <= 10 -> maxThumbHeight
                adapter.itemCount >= 100 -> minThumbHeight
                else -> {
                    // 在10-100条数据之间线性插值
                    val progress = (adapter.itemCount - 10) / 90f
                    maxThumbHeight - (maxThumbHeight - minThumbHeight) * progress
                }
            }

            val firstVisible = layoutManager.findFirstVisibleItemPosition()
            val lastVisible = layoutManager.findLastVisibleItemPosition()
            val visibleRange = lastVisible - firstVisible

            // 计算滑块位置（考虑当前动态高度）
            val thumbPosition = (firstVisible.toFloat() / adapter.itemCount) * visibleHeight

            thumbRect.set(
                width - thumbWidth - thumbPadding,
                thumbPosition,
                width - thumbPadding,
                thumbPosition + currentThumbHeight
            )

            trackRect.set(
                width - thumbWidth - thumbPadding - 4,
                0f,
                width - thumbPadding + 4,
                visibleHeight.toFloat()
            )

            invalidate()
        }
    }

    private fun scrollRecyclerView(y: Float) {
        recyclerView?.let { rv ->
            val adapter = rv.adapter ?: return
            if (adapter.itemCount == 0) return

            // 使用固定高度计算位置（解决高度变化问题）
            val position = ((y / height) * adapter.itemCount).toInt().coerceIn(0, adapter.itemCount - 1)
            (rv.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(position, 0)

            // 使用currentThumbHeight保持高度不变
            thumbRect.top = y - currentThumbHeight / 2
            thumbRect.bottom = y + currentThumbHeight / 2
            invalidate()
        }
    }
*/

    private fun updateThumbPosition() {
        recyclerView?.let { rv ->
            val visibleHeight = rv.height - rv.paddingTop - rv.paddingBottom

            val layoutManager = rv.layoutManager as? LinearLayoutManager ?: return
            val adapter = rv.adapter ?: return

            if (adapter.itemCount == 0) return

            // 动态计算滑块高度
            currentThumbHeight = when {
                adapter.itemCount <= 10 -> maxThumbHeight
                adapter.itemCount >= 100 -> minThumbHeight
                else -> {
                    val progress = (adapter.itemCount - 10) / 90f
                    maxThumbHeight - (maxThumbHeight - minThumbHeight) * progress
                }
            }

            // 计算滑块位置（考虑边界情况）
            val firstVisible = layoutManager.findFirstVisibleItemPosition()
            val lastVisible = layoutManager.findLastVisibleItemPosition()
            val totalItems = adapter.itemCount

            // 计算滑块顶部位置（基于可见项比例）
            val thumbTop = if (totalItems > 0) {
                val visibleFraction = firstVisible.toFloat() / totalItems
                visibleFraction * (visibleHeight - currentThumbHeight)
            } else {
                0f
            }

            // 确保滑块不会超出轨道范围
            val clampedThumbTop = thumbTop.coerceIn(0f, visibleHeight - currentThumbHeight)

            // 设置滑块位置
            thumbRect.set(
                width - thumbWidth - thumbPadding,
                clampedThumbTop,
                width - thumbPadding,
                clampedThumbTop + currentThumbHeight
            )

            // 设置轨道位置
            trackRect.set(
                width - thumbWidth - thumbPadding - 4,
                0f,
                width - thumbPadding + 4,
                visibleHeight.toFloat()
            )

            invalidate()
        }
    }

    private fun scrollRecyclerView(y: Float) {
        recyclerView?.let { rv ->
            val visibleHeight = rv.height - rv.paddingTop - rv.paddingBottom
            val adapter = rv.adapter ?: return
            if (adapter.itemCount == 0) return

            // 计算滑块中心位置（考虑边界情况）
            val thumbCenter = y.coerceIn(currentThumbHeight / 2, visibleHeight - currentThumbHeight / 2)

            // 计算目标位置（基于滑块中心位置的比例）
            val position = ((thumbCenter - currentThumbHeight / 2) / (visibleHeight - currentThumbHeight) * adapter.itemCount)
                .toInt()
                .coerceIn(0, adapter.itemCount - 1)

            // 滚动到目标位置
            (rv.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(position, 0)

            // 更新滑块位置（保持高度不变）
            thumbRect.set(
                width - thumbWidth - thumbPadding,
                thumbCenter - currentThumbHeight / 2,
                width - thumbPadding,
                thumbCenter + currentThumbHeight / 2
            )

            invalidate()
        }
    }

}
