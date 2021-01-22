package com.cninct.calendarviewdemo.calendar

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.FrameLayout


class SquareLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
    private var backType = 0
    private var backColor = Color.BLUE
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

    /**
     * 绘制背景
     * @param type:0圆，-1，左半圆，-2右半圆，-3圆，-4矩形
     */
    fun setBackGround(type: Int = 0, color: Int = Color.parseColor("#E2E8EE")) {
        backType = type
        backColor = color
        paint.color = backColor
        postInvalidate()
    }

    override fun dispatchDraw(canvas: Canvas?) {
        drawBackGround(canvas)
        super.dispatchDraw(canvas)
    }

    private fun drawBackGround(canvas: Canvas?) {
        val radius = height / 2f
        canvas?.apply {
            drawColor(Color.TRANSPARENT)
            when (backType) {
                -1 -> {//左半圆
                    drawPath(getLeftPath(radius), paint)
                }
                -2 -> {//2右半圆
                    drawPath(getRightPath(radius), paint)
                }
                -3 -> {//3圆
                    drawCircle(radius, radius, radius * 0.9f, paint)
                }
                -4 -> {
                    drawRect(RectF(0f, radius * 0.1f, radius * 2, radius * 1.9f), paint)
                }
            }
        }
    }

    private fun getLeftPath(radius: Float): Path {
        val path = Path()
        path.moveTo(radius, radius)
        path.arcTo(
            RectF(0f, radius * 0.1f, radius * 2, radius * 1.9f),
            -90f,
            -180f
        )
        path.lineTo(radius * 2, radius * 1.9f)
        path.lineTo(radius * 2, radius * 0.1f)
        path.lineTo(radius, radius * 0.1f)
        path.close()
        return path
    }

    private fun getRightPath(radius: Float): Path {
        val path = Path()
        path.moveTo(radius, radius)
        path.arcTo(
            RectF(0f, radius * 0.1f, radius * 2, radius * 1.9f),
            90f,
            -180f
        )
        path.lineTo(0f, radius * 0.1f)
        path.lineTo(0f, radius * 1.9f)
        path.lineTo(radius, radius * 1.9f)
        path.close()
        return path
    }
}