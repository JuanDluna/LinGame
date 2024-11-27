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
    var UID : String? = null
    var isLoggedIn : Boolean? = null
    var selectedLanguage : String? = null
    var isLanguagesSelected : Boolean? = null
    var sharedPreferences : SharedPreferences? = null

    // Variable para usar en pruebas
    private var debbugerMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (debbugerMode){
            startActivity(Intent(this, RetoRapidoActivity::class.java))
            return
        }

        // Verificar si el usuario ya está logueado
        sharedPreferences = getSharedPreferences(R.string.sharedPreferencesName.toString(), MODE_PRIVATE)

        // TODO: Borrar cuando se acabe el momento de pruebas
//        sharedPreferences.edit().clear().apply()

        UID  = sharedPreferences!!.getString(R.string.UID_Preferences.toString(), null)
        isLoggedIn = sharedPreferences!!.getBoolean(R.string.isLoggedInPreferences.toString(), false)
        selectedLanguage = sharedPreferences!!.getString(R.string.selectedLanguagePreferences.toString(), null)



        Log.d("MainActivity", "Shared Preferences : ${sharedPreferences!!.all}")

        try {
            fireStore.collection("users").document(UID.toString()).get().
            addOnSuccessListener { document ->
                var data = document.data
                if (data != null) {
                    isLanguagesSelected = data["isLanguagesSelected"] as? Boolean ?: false
                    checkWhereToNavigate()
                }
            }.addOnFailureListener{
                Log.d("MainActivity", "Error al obtener datos del usuario: ${it.message}")
                checkWhereToNavigate()
            }
        }catch (e: Exception){
                Log.d("MainActivity", "Error al obtener datos del usuario: ${e.message}")
        }


        // Si no está logueado, mostrar la pantalla inicial
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

    private fun checkWhereToNavigate(){
        if (isLoggedIn!!) {
            Log.d("MainActivity", "Is Loggued and checking info")
            if(isLanguagesSelected!! == true){
//                 Redirigir directamente a la actividad del juego si ya está logueado
                val intent = Intent(this, GameLogicaActivity::class.java)
                startActivity(intent)
                finish() // Finalizar esta actividad para evitar volver con el botón "atrás"
                return
            }else{
                val intent = Intent(this, LanguageSelectionActivity::class.java)
                startActivity(intent)
                finish() // Finalizar esta actividad para evitar volver con el botón "atrás"
                return
            }
        }
    }
}
