package com.example.lingame

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.card.MaterialCardView

class PlatformControl @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    var category: String? = null // Atributo para la categoría (norte, sur, este, oeste)
    var targetActivity: Class<out FragmentActivity>? = null // Atributo para targetActivity
    var targetFragment: Class<out Fragment>? = null // Atributo para targetFragment

    private var animator: ValueAnimator? = null

    init {
        // Configuración básica del MaterialCardView para hacerlo más pequeño y redondeado
        val platformDiameter = 150f // Diámetro de la plataforma (ajustado para que sea más pequeña)
        radius = platformDiameter / 2 // Radio para hacer la forma circular
        cardElevation = 8f // Elevación constante
        setCardBackgroundColor(Color.parseColor("#FF6200EE")) // Color de fondo (puedes cambiarlo)
        isClickable = true
        isFocusable = true

        layoutParams = LayoutParams(platformDiameter.toInt(), platformDiameter.toInt()) // Establece el tamaño de la plataforma

        setOnClickListener {
            try {
                performClickAction()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Iniciar la animación pasiva con un retraso
        startLevitationAnimationWithDelay()
    }

    // Método para iniciar la animación de levitación con un pequeño retraso
    private fun startLevitationAnimationWithDelay() {
        val randomDelay = (100..1000).random().toLong() // Retraso aleatorio entre 100ms y 1 segundo

        // Usamos Handler para crear el retraso de la animación
        Handler(Looper.getMainLooper()).postDelayed({
            // Iniciar animación en el hilo principal
            startLevitationAnimation()
        }, randomDelay)
    }

    private fun startLevitationAnimation() {
        // Animator que cambia la posición vertical (Y) de la plataforma
        animator = ValueAnimator.ofFloat(0f, 30f) // Movimiento vertical (puedes ajustar el rango)
        animator?.apply {
            duration = 1000 // Duración del ciclo (1 segundo)
            repeatCount = ValueAnimator.INFINITE // Repite indefinidamente
            repeatMode = ValueAnimator.REVERSE // Hace que suba y baje
            interpolator = LinearInterpolator() // Hace que el movimiento sea suave y lineal

            // Este `addUpdateListener` se ejecutará en el hilo principal
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                translationY = value // Actualiza la posición vertical de la plataforma en la UI principal
            }
            start()
        }
    }

    private fun stopLevitationAnimation() {
        animator?.cancel() // Detiene la animación si es necesario
    }

    private fun performClickAction() {
        if (targetActivity != null) {
            // Si hay una actividad objetivo, lanzar esa actividad
            val intent = Intent(context, targetActivity)
            context.startActivity(intent)
        } else if (targetFragment != null && context is FragmentActivity) {
            // Si hay un fragmento objetivo, reemplazar el fragmento actual
            val fragment = targetFragment!!.newInstance()
            (context as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        } else {
            // Si no hay una actividad o fragmento objetivo, manejar según la categoría
            when (category) {
                "norte" -> {
                    val intent = Intent(context, RetoRapidoActivity::class.java)
                    context.startActivity(intent)
                }
                "este" -> {
                    val intent = Intent(context, ParafraseaActivity::class.java)
                    context.startActivity(intent)
                }
                else -> {
                    // Manejo de categorías no asignadas
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startLevitationAnimationWithDelay() // Inicia la animación con un retraso cuando el control es agregado a la vista
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopLevitationAnimation() // Detiene la animación si el control es removido
    }
}