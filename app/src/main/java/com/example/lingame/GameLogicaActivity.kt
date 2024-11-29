package com.example.lingame

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Picture
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.firestore.v1.Cursor
import java.io.File
import java.net.URI
import kotlin.math.exp
import kotlin.math.roundToInt

class GameLogicaActivity : FragmentActivity() {

    // Elementos del HUD
    private lateinit var playerAvatar: ImageView
    private lateinit var playerLevel: TextView
    private lateinit var experienceBar: ProgressBar
    private lateinit var menuButton: ButtonDropdownMenu
    private lateinit var languageSelector: ButtonDropdownMenu

    private lateinit var dbHelper: DBSQLite
    private lateinit var preferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dbHelper = DBSQLite(this)
        preferences = getSharedPreferences(R.string.sharedPreferencesName.toString(), Context.MODE_PRIVATE)
        Log.d("GameLogicaActivity", "Shared Preferences : ${preferences.all}")

        setContentView(R.layout.activity_gamelogica)

        // Incluir el componente de juego
        if(savedInstanceState == null){
            supportFragmentManager.
            beginTransaction().
            replace(R.id.fragment_game_container, GameActivity()).commit()
        }

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
//        surfaceView = findViewById<SurfaceView>(R.id.gameSurfaceView)

        val UID = preferences.getString(R.string.UID_Preferences.toString(), null)
        Log.d("GameLogicaActivity", "Usuario logueado: ${UID}")

        var cursorDb: android.database.Cursor? = null
        try {
            cursorDb = dbHelper.getUserData(UID!!)
            Log.d("GameLogicaActivity", "Cursor de la base de datos: ${cursorDb!!}")
        }catch (e: Exception){
            Log.d("GameLogicaActivity", "Error al obtener datos del usuario: ${e.message}")
        }

        if (cursorDb != null && cursorDb.moveToFirst()) {
            var level: Float? = null
            var playerPhotoURI: String? = null

            try {
                level = cursorDb.getFloat(cursorDb.getColumnIndexOrThrow("generalLevel"))
                playerPhotoURI = cursorDb.getString(cursorDb.getColumnIndexOrThrow("photo_url"))
            } catch (e: Exception) {
                Log.d("GameLogicaActivity", "Error al obtener datos del usuario: ${e.message}")
            }

            Log.d("GameLogicaActivity", "Level: $level")
            Log.d("GameLogicaActivity", "PlayerPhotoFile: $playerPhotoURI")

            if (!playerPhotoURI.isNullOrEmpty()) {
                val photoFile = File(playerPhotoURI)

                if (photoFile.exists()) {
                    try {
                        val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                        playerAvatar.setImageBitmap(bitmap)
                        playerAvatar.setBackgroundColor(Color.WHITE)
                        Log.d("GameLogicaActivity", "Imagen cargada correctamente en el avatar.")
                    } catch (e: Exception) {
                        Log.e("GameLogicaActivity", "Error al cargar la imagen: ${e.message}")
                    }
                } else {
                    Log.d("GameLogicaActivity", "El archivo de imagen no existe en la ruta proporcionada.")
                }
            } else {
                Log.d("GameLogicaActivity", "playerPhotoURI está vacío o nulo.")
            }

            // Configuración de nivel y barra de experiencia
            playerLevel.text = "Nivel: ${level?.roundToInt() ?: "Desconocido"}"
            experienceBar.progress = ((level ?: 0f - (level ?: 0f).toInt()) * 100).toInt()
        } else {
            Log.d("GameLogicaActivity", "Cursor nulo o vacío")
        }


    }

    /**
     * Configura el botón del menú desplegable.
     */
    private fun setupMenuButton() {

        menuButton.setDropdownOptions(
            mapOf(
                "Opciones" to getDrawable(R.drawable.baseline_settings_24)!!,
                "Ajustes" to getDrawable(R.drawable.baseline_settings_24)!!,
                "Salir" to getDrawable(R.drawable.baseline_settings_24)!!)
            )
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

        languageSelector.setDropdownOptions(mapOf(
            "Inglés" to getDrawable(R.drawable.banderausa)!!,
            "Portugués" to getDrawable(R.drawable.banderabrasil)!!,
            "Francés" to getDrawable(R.drawable.banderafrancia)!!
        ))

        languageSelector.setOnOptionClickListener { language ->
            when (language) {
                "Portugués" -> changeLanguage("pr")
                "Inglés" -> changeLanguage("en")
                "Francés" -> changeLanguage("fr")
                else -> Toast.makeText(this, "Idioma no reconocido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showToast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Lógica para cambiar el idioma.
     */
    private fun changeLanguage(languageCode: String) {
        preferences.edit().putString(R.string.selectedLanguagePreferences.toString(), languageCode).apply()

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
        preferences.edit().clear().apply()
        startActivity(Intent(this, MainActivity::class.java))
    }
}
