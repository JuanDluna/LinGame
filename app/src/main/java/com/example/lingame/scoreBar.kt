package com.example.lingame

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.widget.ProgressBar

class scoreBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ProgressBar(context, attrs, defStyleAttr) {

    private val starPositions = listOf(0.33f, 0.66f, 1.0f) // Posiciones relativas de las estrellas
    private val stars = mutableListOf<Star>()

    // Colores y configuraciones gráficas
    private val barPaint = Paint().apply {
        color = Color.parseColor("#D6D6D6") // Fondo activo pero sin progreso
        style = Paint.Style.FILL
    }

    private val progressPaint = Paint().apply {
        color = Color.BLUE // Color de progreso
        style = Paint.Style.FILL
    }

    init {
        // Inicializar las estrellas
        starPositions.forEach { positionRatio ->
            stars.add(Star(context, positionRatio))
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val barHeight = height.toFloat()
        val barWidth = width.toFloat()
        val progressWidth = (progress.coerceAtMost(max) / max.toFloat()) * barWidth
        val cornerRadius = barHeight / 2 // Redondear las esquinas

        // Dibujar barra base con bordes redondeados
        canvas.drawRoundRect(
            0f,
            height / 2f - barHeight / 2f,
            barWidth,
            height / 2f + barHeight / 2f,
            cornerRadius,
            cornerRadius,
            barPaint
        )

        // Dibujar barra de progreso
        canvas.drawRoundRect(
            0f,
            height / 2f - barHeight / 2f,
            progressWidth,
            height / 2f + barHeight / 2f,
            cornerRadius,
            cornerRadius,
            progressPaint
        )

        // Dibujar estrellas
        stars.forEach { star ->
            star.isReached = progress >= (star.positionRatio * max).toInt()
            star.draw(canvas, barHeight)
        }
    }

    /**
     * Clase interna para manejar las estrellas
     */
    inner class Star(
        context: Context,
        val positionRatio: Float
    ) {
        var isReached: Boolean = false
        private val starDrawable = context.getDrawable(R.drawable.ic_star_rate)!!

        private val paint: Paint = Paint().apply {
            style = Paint.Style.FILL
        }

        fun draw(canvas: Canvas, barHeight: Float) {
            val starSize = barHeight// Tamaño proporcional
            val starX = width * ( if (positionRatio == 1.0f) 0.95f else positionRatio) - starSize / 2
            val starY = 0F // Centrar estrellas arriba de la barra

            // Cambiar el color según si está alcanzada
            paint.color = if (isReached) Color.YELLOW else Color.BLACK
            paint.style = if (isReached) Paint.Style.FILL_AND_STROKE else Paint.Style.STROKE

            // Dibujar estrella con el color actualizado
            val bitmap = Bitmap.createBitmap(starSize.toInt(), starSize.toInt(), Bitmap.Config.ARGB_8888)
            val bitmapCanvas = Canvas(bitmap)
            starDrawable.setTint(paint.color)
            starDrawable.setBounds(0, 0, starSize.toInt(), starSize.toInt())
            starDrawable.draw(bitmapCanvas)
            canvas.drawBitmap(bitmap, starX, starY, null)
        }
    }

    // Métodos públicos para actualizar la barra
    fun setMaxScore(score: Int) {
        max = score
        invalidate()
    }

    fun incrementScore(increment: Int) {
        progress += increment
        invalidate()
    }

    fun getScore(): Int {
        return progress
    }
}
