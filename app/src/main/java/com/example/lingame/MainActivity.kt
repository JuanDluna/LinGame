package com.example.lingame

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    var fireStore = FirebaseFirestore.getInstance()
    var UID: String? = null
    var isLoggedIn: Boolean? = null
    var selectedLanguage: String? = null
    var isLanguagesSelected: Boolean? = null
    lateinit var sharedPreferences: SharedPreferences

    // Variable para usar en pruebas
    private var debbugerMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (debbugerMode) {
            startActivity(Intent(this, TutorialActivity::class.java))
            return
        }

        sharedPreferences = getSharedPreferences(getString(R.string.sharedPreferencesName), MODE_PRIVATE)

        UID = sharedPreferences.getString(getString(R.string.UID_Preferences), null)
        isLoggedIn = sharedPreferences.getBoolean(getString(R.string.isLoggedInPreferences), false)
        selectedLanguage = sharedPreferences.getString(getString(R.string.selectedLanguagePreferences), null)

        Log.d("MainActivity", "Shared Preferences : ${sharedPreferences.all}")

        // **Paso 1: Mostrar la pantalla de carga**
        setContentView(R.layout.loading_screen)

        // **Paso 2: Ejecutar la petición a Firebase**
        try {
            fireStore.collection("users").document(UID.toString()).get()
                .addOnSuccessListener { document ->
                    val data = document.data
                    if (data != null) {
                        isLanguagesSelected = data["isLanguagesSelected"] as? Boolean ?: false
                        checkWhereToNavigate()
                    } else {
                        showInitialScreen()
                    }
                }
                .addOnFailureListener {
                    Log.d("MainActivity", "Error al obtener datos del usuario: ${it.message}")
                    showInitialScreen()
                }
        } catch (e: Exception) {
            Log.d("MainActivity", "Error al obtener datos del usuario: ${e.message}")
            showInitialScreen()
        }
    }

    // **Paso 3: Verificar la navegación**
    private fun checkWhereToNavigate() {
        if (isLoggedIn == true) {
            Log.d("MainActivity", "Is Logged In and checking info")
            if (isLanguagesSelected == true) {
                // Redirigir directamente a la actividad del juego si ya está logueado
                val intent = Intent(this, GameLogicaActivity::class.java)
                startActivity(intent)
                finish() // Finalizar esta actividad para evitar volver con el botón "atrás"
            } else {
                val intent = Intent(this, LanguageSelectionActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            showInitialScreen()
        }
    }

    // **Paso 4: Cambiar a la pantalla inicial**
    private fun showInitialScreen() {
        // Cambiar la vista a la pantalla inicial
        setContentView(R.layout.initial_screen)

        // Referencias a los elementos de la vista
        val btnComienza: Button = findViewById(R.id.btnComienza)
        val btnIniciarSesion: Button = findViewById(R.id.btnIniciarSesion)

        // Configurar listeners para los botones
        btnComienza.setOnClickListener {
            // Navegar a la pantalla de registro
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnIniciarSesion.setOnClickListener {
            // Navegar a la pantalla de inicio de sesión
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
