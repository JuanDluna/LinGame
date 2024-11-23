package com.example.lingame

import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class GameLogicaActivity : AppCompatActivity() {

    // Elementos del HUD
    private lateinit var playerAvatar: ImageView
    private lateinit var playerLevel: TextView
    private lateinit var experienceBar: ProgressBar
    private lateinit var menuButton: ButtonDropdownMenu
    private lateinit var languageSelector: ButtonDropdownMenu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gamelogica)

        // Inicializar vistas del HUD
        initHudElements()

        // Configurar botones del HUD
        setupMenuButton()
        setupLanguageSelector()
    }

    /**
     * Inicializa las vistas del HUD.
     */
    private fun initHudElements() {
        playerAvatar = findViewById(R.id.playerAvatar)
        playerLevel = findViewById(R.id.playerLevel)
        experienceBar = findViewById(R.id.experienceBar)
        menuButton = findViewById(R.id.menuButton)
        languageSelector = findViewById(R.id.languageSelector)
    }

    /**
     * Configura el botón del menú desplegable.
     */
    private fun setupMenuButton() {
        val menuOptions = mapOf(
            "Opciones" to getDrawable(R.drawable.baseline_settings_24)!!,
            "Ajustes" to getDrawable(R.drawable.baseline_settings_24)!!,
            "Salir" to getDrawable(R.drawable.baseline_settings_24)!!
        )

        menuButton.setDropdownOptions(menuOptions)
        menuButton.setOnOptionClickListener { option ->
            when (option) {
                "Opciones" -> showOptionsDialog()
                "Ajustes" -> showSettingsDialog()
                "Salir" -> exitGame()
                else -> Toast.makeText(this, "Opción no reconocida", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Configura el selector de idioma.
     */
    private fun setupLanguageSelector() {
        val languageOptions = mapOf(
            "Inglés" to getDrawable(R.drawable.banderausa)!!,
            "Portugués" to getDrawable(R.drawable.banderabrasil)!!,
            "Francés" to getDrawable(R.drawable.banderafrancia)!!
        )

        languageSelector.setDropdownOptions(languageOptions)
        languageSelector.setOnOptionClickListener { language ->
            when (language) {
                "Español" -> changeLanguage("es")
                "Inglés" -> changeLanguage("en")
                "Francés" -> changeLanguage("fr")
                else -> Toast.makeText(this, "Idioma no reconocido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Lógica para cambiar el idioma.
     */
    private fun changeLanguage(languageCode: String) {
        Toast.makeText(this, "Idioma cambiado a $languageCode", Toast.LENGTH_SHORT).show()
        // Implementar cambio de idioma en el juego
    }

    /**
     * Muestra un diálogo de opciones.
     */
    private fun showOptionsDialog() {
        Toast.makeText(this, "Opciones abiertas", Toast.LENGTH_SHORT).show()
        // Implementar lógica para mostrar opciones personalizadas
    }

    /**
     * Muestra un diálogo de configuración.
     */
    private fun showSettingsDialog() {
        Toast.makeText(this, "Ajustes abiertos", Toast.LENGTH_SHORT).show()
        // Implementar lógica para ajustes personalizados
    }

    /**
     * Sale del juego.
     */
    private fun exitGame() {
        Toast.makeText(this, "Saliendo del juego", Toast.LENGTH_SHORT).show()
        finish()
    }
}
