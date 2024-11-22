package com.example.lingame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class PlatformControl : View {
    private var platformPaint: Paint? = null
    private var shadowPaint: Paint? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        platformPaint = Paint()
        platformPaint!!.color = Color.BLUE
        platformPaint!!.style = Paint.Style.FILL

        shadowPaint = Paint()
        shadowPaint!!.color = Color.GRAY
        shadowPaint!!.style = Paint.Style.FILL
        shadowPaint!!.alpha = 120
    }

    private fun drawPlatform(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        shadowOffset: Float,
        platformPaint: Paint
    ) {
        val platformRect = RectF(x, y, x + width, y + height)

        canvas.drawRect(
            platformRect.left + shadowOffset,
            platformRect.top + shadowOffset,
            platformRect.right + shadowOffset,
            platformRect.bottom + shadowOffset,
            shadowPaint!!
        )

        canvas.drawRect(platformRect, platformPaint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawPlatform(canvas, 200f, 300f, 250f, 125f, 20f, platformPaint!!)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        return true
    }
}