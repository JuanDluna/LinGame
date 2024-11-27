package com.example.lingame

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.ProgressBar

class scoreBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ProgressBar(context, attrs, defStyleAttr) {

    // Configuración básica
    private val maxScore: Int = 100 // Puntaje máximo predeterminado (puede modificarse dinámicamente)
    private val starPositions = listOf(0.5f, 0.75f, 1.0f) // Posiciones relativas de las estrellas (2/4, 3/4 y 4/4)
    private val stars = mutableListOf<Star>()

    // Pintura para la barra de progreso personalizada
    private val barPaint = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.FILL
    }

    private val progressPaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
    }

    init {
        // Inicializar las estrellas en las posiciones definidas
        starPositions.forEach { positionRatio ->
            stars.add(Star(context, positionRatio))
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Dimensiones de la barra
        val barHeight = height * 0.2f // Grosor del 20% de la altura
        val barWidth = width.toFloat()
        val progressWidth = (progress / maxScore.toFloat()) * barWidth

        // Dibujar barra base
        canvas.drawRect(0f, height / 2f - barHeight / 2f, barWidth, height / 2f + barHeight / 2f, barPaint)

        // Dibujar progreso
        canvas.drawRect(0f, height / 2f - barHeight / 2f, progressWidth, height / 2f + barHeight / 2f, progressPaint)

        // Dibujar estrellas
        stars.forEach { star ->
            star.isReached = progress >= (star.positionRatio * maxScore).toInt()
            star.draw(canvas, barHeight)
        }
    }

    /**
     * Subclase para manejar las estrellas
     */
    inner class Star(
        context: Context,
        val positionRatio: Float
    ) {
        var isReached: Boolean = false
        private val starDrawable = context.getDrawable(R.drawable.ic_star_rate)!! // Reemplaza con tu recurso vectorial

        private val paint: Paint = Paint().apply {
            style = Paint.Style.FILL
        }

        fun draw(canvas: Canvas, barHeight: Float) {
            val starSize = barHeight * 2 // Tamaño proporcional a la barra
            val starX = width * positionRatio - starSize / 2
            val starY = height / 2f - starSize

            // Cambiar color según el estado
            paint.color = if (isReached) Color.YELLOW else Color.LTGRAY

            // Dibujar la estrella con color actualizado
            val bitmap = Bitmap.createBitmap(starSize.toInt(), starSize.toInt(), Bitmap.Config.ARGB_8888)
            val bitmapCanvas = Canvas(bitmap)
            starDrawable.setTint(paint.color)
            starDrawable.setBounds(0, 0, starSize.toInt(), starSize.toInt())
            starDrawable.draw(bitmapCanvas)
            canvas.drawBitmap(bitmap, starX, starY, null)
        }
    }

    /**
     * Método para establecer el puntaje máximo dinámicamente
     */
    fun setMaxScore(score: Int) {
        max = score
    }
}
