package com.example.lingame

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.firebase.database.*

class CreaHistoriaActivity : FragmentActivity() {

    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences

    private var currentLevel: Int = 1
    private var currentLanguage: String = "es"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cntrls_crea_historia)

        // Inicializar Firebase y SharedPreferences
        firebaseDatabase = FirebaseDatabase.getInstance().reference
        sharedPreferences = getSharedPreferences(getString(R.string.sharedPreferencesName), Context.MODE_PRIVATE)

        // Obtener idioma actual desde SharedPreferences
        currentLanguage = sharedPreferences.getString(getString(R.string.selectedLanguagePreferences), "es") ?: "es"

        // Obtener nivel actual desde SQLite
        currentLevel = getLevelFromSQLite().toInt()

        // Configurar botón de controles
        val button = findViewById<Button>(R.id.btnNextCntrls)
        button.setOnClickListener {
            // Cambiar a la vista de la historia
            setContentView(R.layout.activity_crea_historia)
            loadLevelData()
        }
    }

    private fun getLevelFromSQLite(): Float {
        // Implementar lógica para leer el nivel desde SQLite (ejemplo)
        val dbHelper = DBSQLite(this)
        val UID = sharedPreferences.getString(getString(R.string.UID_Preferences), null)
        return dbHelper.getLevelByCategoryAndLanguage(category = DBSQLite.COLUMN_LEVEL_CREA_HISTORIA, language = currentLanguage, UID = UID!!)
    }

    private fun loadLevelData() {
        // Obtener datos desde Firebase
        firebaseDatabase.child("languages/story_creation/$currentLevel")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val question = snapshot.child("question").value.toString()
                        val options = snapshot.child("options").children.map { it.value.toString() }
                        val errorMessage = snapshot.child("error_message").value.toString()

                        // Mostrar el fragmento con datos
                        showDecisionFragment(question, options, errorMessage)
                    } else {
                        Toast.makeText(this@CreaHistoriaActivity, "Nivel no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CreaHistoriaActivity", "Error Firebase: ${error.message}")
                }
            })
    }

    private fun showDecisionFragment(question: String, options: List<String>, errorMessage: String) {
        val fragment = DecisionFragment.newInstance(question, options, errorMessage)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun onQuestionAnswered(correct: Boolean) {
        if (correct) {
            showLevelCompleted()
        } else {
            showErrorScreen()
        }
    }

    private fun showErrorScreen() {
        setContentView(R.layout.error_screen_ch) // Pantalla de error
        
        val retryButton : Button = findViewById(R.id.btnRetryCH)
        retryButton.setOnClickListener { loadLevelData() }

        val mainMenuButton : Button = findViewById(R.id.btnMainMenuCH)
        mainMenuButton.setOnClickListener { finish() } // Volver al menú principal
    }

    private fun showLevelCompleted() {
        setContentView(R.layout.lvl_completed_ch) // Pantalla de nivel completado
        val nextLevelButton : Button= findViewById(R.id.btnNextLevelCH)
        nextLevelButton.setOnClickListener {
            currentLevel++
            updateLevelInSQLite()
            loadLevelData()
        }
    }

    private fun updateLevelInSQLite() {
        val dbHelper = DBSQLite(this)
    }
}
