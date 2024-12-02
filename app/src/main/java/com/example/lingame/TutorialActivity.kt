package com.example.lingame

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

class TutorialActivity : AppCompatActivity() {

    private lateinit var guideText: Button
    private lateinit var continueText: TextView
    private val platformControls = listOf(R.id.platform1, R.id.platform2, R.id.platform3, R.id.platform4)
    private val guideMessages = listOf(
        "Crea tu historia: Elige las palabras correctas para completar historias. ",
        "Reto rápido: Responde rápido y gana puntos, pero cuidado si hay errores.",
        "Tradúcelo: Selecciona la traducción correcta y aprende nuevas palabras fácilmente.",
        "Para-frasea: Forma oraciones con las palabras en el orden correcto."
    )

    private var currentStep = 0
    private val platformMargin = 32 // Margen entre las plataformas en píxeles

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        guideText = findViewById(R.id.guideText)
        continueText = findViewById(R.id.continueText)

        // Inicialmente, oculta las plataformas adicionales
        platformControls.forEach { findViewById<PlatformControl>(it).visibility = View.GONE }

        val platformControl = findViewById<PlatformControl>(R.id.platformControl)
        platformControl.setOnClickListener {
            if (currentStep < platformControls.size) {
                // Muestra la siguiente plataforma y agrega un margen superior
                val nextPlatform = findViewById<PlatformControl>(platformControls[currentStep])
                nextPlatform.visibility = View.VISIBLE
                nextPlatform.startLevitationAnimationWithDelay()

                // Configurar margen entre plataformas
                val layoutParams = nextPlatform.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.topMargin = platformMargin
                nextPlatform.layoutParams = layoutParams

                // Actualizar el texto de guía
                guideText.text = guideMessages[currentStep]
                currentStep++
            } else {
                // Inicia el juego principal cuando llega a la última plataforma
                val intent = Intent(this, GameLogicaActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
