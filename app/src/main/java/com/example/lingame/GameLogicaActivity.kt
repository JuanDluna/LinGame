package com.example.lingame

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import java.io.File
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
        preferences = getSharedPreferences(getString(R.string.sharedPreferencesName), Context.MODE_PRIVATE)
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
    fun initHudElements() {
        playerAvatar = findViewById(R.id.playerAvatar)
        playerLevel = findViewById(R.id.playerLevel)
        experienceBar = findViewById(R.id.experienceBar)
        menuButton = findViewById(R.id.menuButton)
        languageSelector = findViewById(R.id.languageSelector)
//        surfaceView = findViewById<SurfaceView>(R.id.gameSurfaceView)

        val UID = preferences.getString(getString(R.string.UID_Preferences), null)
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
                level = cursorDb.getFloat(cursorDb.getColumnIndexOrThrow(DBSQLite.COLUMN_GENERAL_LEVEL))
                playerPhotoURI = cursorDb.getString(cursorDb.getColumnIndexOrThrow(DBSQLite.COLUMN_PHOTO_URL))
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
            experienceBar.progress = ((level!! % 1) * 100).toInt()
        } else {
            Log.d("GameLogicaActivity", "Cursor nulo o vacío")
        }


        playerAvatar.setOnClickListener {
            PlayerInfoDialogFragment().show(supportFragmentManager, "PlayerInfoDialogFragment")
        }

    }

    /**
     * Configura el botón del menú desplegable.
     */
    private fun setupMenuButton() {

        menuButton.setDropdownOptions(
            mapOf(
                "Informacion" to getDrawable(R.drawable.info_64)!!,
                "Ajustes" to getDrawable(R.drawable.baseline_settings_24)!!,
                "Salir" to getDrawable(R.drawable.exit_24)!!)
            )
        menuButton.setOnOptionClickListener { option ->
            when (option) {
                "Informacion" -> showOptionsDialog()
                "Ajustes" -> showSettingsDialog()
                "Salir" -> exitGame()
                else -> Toast.makeText(this, "Opción no reconocida", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Configura el selector de idioma.
     */
     fun setupLanguageSelector() {
        var listOfLanguages = preferences.getStringSet(getString(R.string.listOfLanguagesPreferences), null)
        var selectedLanguage = preferences.getString(getString(R.string.selectedLanguagePreferences), null)
        Log.i("GameLogicaActivity", "Idiomas seleccionados: ${listOfLanguages}")

        if (selectedLanguage == null || selectedLanguage.isEmpty()) {
            // Obtener el primer idioma disponible de la lista
            selectedLanguage = listOfLanguages!!.firstOrNull() // Por si la lista está vacía
            listOfLanguages.remove(selectedLanguage)

            // Guardar los datos obtenidos
            preferences.edit().putStringSet(getString(R.string.listOfLanguagesPreferences), listOfLanguages).apply()
            preferences.edit().putString(getString(R.string.selectedLanguagePreferences), selectedLanguage).apply()

            // Actualizar el selector visual
            setDrawableOfSelector()
        }else if(listOfLanguages!!.contains(selectedLanguage)) listOfLanguages.remove(selectedLanguage)

        setDrawableOfSelector()

        setDropdownOptions( listOfLanguages, selectedLanguage)



        languageSelector.setOnOptionClickListener { language ->
            when (language) {
                getString(R.string.portugueseValuePreferences) -> changeLanguage(getString(R.string.portugueseValuePreferences))
                getString(R.string.englishValuePreferences) -> changeLanguage(getString(R.string.englishValuePreferences))
                getString(R.string.frenchValuePreferences) -> changeLanguage(getString(R.string.frenchValuePreferences))
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
        var edit = preferences.edit()

        // Logica para manejar los idiomas que estan en lista y el seleccionado
        var listLanguages = preferences.getStringSet(getString(R.string.listOfLanguagesPreferences), null)
        var selectedLanguage = preferences.getString(getString(R.string.selectedLanguagePreferences), null)

        listLanguages!!.add(selectedLanguage)
        listLanguages!!.remove(languageCode)
        edit.putStringSet(getString(R.string.listOfLanguagesPreferences), listLanguages)
        edit.putString(getString(R.string.selectedLanguagePreferences), languageCode)
        edit.apply()

        setDrawableOfSelector();
        setDropdownOptions( listOfLanguages = preferences.getStringSet(getString(R.string.listOfLanguagesPreferences), null), selectedLanguage = languageCode )
    }

    private fun setDropdownOptions(listOfLanguages : Set<String>?, selectedLanguage : String?){
        var DropdownOptions = mutableMapOf<String, Drawable>()

        listOfLanguages!!.forEach { language ->

            when(language){
                getString(R.string.portugueseValuePreferences) ->{
                        DropdownOptions.put(getString(R.string.portugueseValuePreferences), getDrawable(R.drawable.banderabrasil)!!)
                }
                getString(R.string.englishValuePreferences) ->{
                        DropdownOptions.put(getString(R.string.englishValuePreferences), getDrawable(R.drawable.banderausa)!!)
                }
                getString(R.string.frenchValuePreferences) ->{
                        DropdownOptions.put(getString(R.string.frenchValuePreferences), getDrawable(R.drawable.banderafrancia)!!)
                }
            }
        }

        languageSelector.setDropdownOptions(DropdownOptions)

    }

    private fun setDrawableOfSelector(){
        val selectedLanguage = preferences.getString(getString(R.string.selectedLanguagePreferences), null)

        when(selectedLanguage){
            getString(R.string.portugueseValuePreferences) -> languageSelector.setImageDrawable(getDrawable(R.drawable.banderabrasil))
            getString(R.string.englishValuePreferences) -> languageSelector.setImageDrawable(getDrawable(R.drawable.banderausa))
            getString(R.string.frenchValuePreferences) -> languageSelector.setImageDrawable(getDrawable(R.drawable.banderafrancia))
        }
    }

    /**
     * Muestra un diálogo de opciones.
     */
    private fun showOptionsDialog() {
        CategoryInfoDialog().show(supportFragmentManager, "CategoryInfoDialog")
    }

    /**
     * Muestra un diálogo de configuración.
     */
    private fun showSettingsDialog() {
        SettingsDialogFragment().show(supportFragmentManager, "SettingsDialogFragment")
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
