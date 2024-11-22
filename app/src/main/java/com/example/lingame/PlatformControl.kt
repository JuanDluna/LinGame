package com.example.lingame

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class PlatformControl @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val platformPaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
    }

    private val shadowPaint = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.FILL
        alpha = 120
    }

    private val platformRect = RectF(0f, 0f, 250f, 125f) // Example rectangle
    var targetActivity: Class<out AppCompatActivity>? = null

    init {
        // Asegúrate de que el control sea "clickeable"
        isClickable = true

        // Leer los atributos XML personalizados
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.PlatformControl, 0, 0)
            val targetActivityName = typedArray.getString(R.styleable.PlatformControl_targetActivity)
            typedArray.recycle()

            // Si se ha proporcionado el nombre de la actividad, obtener la clase
            targetActivityName?.let { activityName ->
                targetActivity = try {
                    Class.forName(activityName) as Class<out AppCompatActivity>
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace() // Manejar el error si no se encuentra la clase
                    null
                }
            }
        }
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = (platformRect.width() + 20).toInt() // Add shadow offset
        val desiredHeight = (platformRect.height() + 20).toInt() // Add shadow offset

        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val shadowOffset = 20f

        // Draw shadow
        canvas.drawRect(
            platformRect.left + shadowOffset,
            platformRect.top + shadowOffset,
            platformRect.right + shadowOffset,
            platformRect.bottom + shadowOffset,
            shadowPaint
        )

        // Draw platform
        canvas.drawRect(platformRect, platformPaint)
    }

    override fun performClick(): Boolean {
        super.performClick()

        // Abrir la actividad correspondiente cuando se haga clic
        targetActivity?.let { activity ->
            val intent = Intent(context, activity)
            context.startActivity(intent)
        }

        return true
    }
}
