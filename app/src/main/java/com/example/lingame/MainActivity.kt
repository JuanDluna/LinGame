package com.example.lingame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Verificar si el usuario ya está logueado
        val sharedPreferences = getSharedPreferences("LingamePreferences", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            // Redirigir directamente a la actividad del juego si ya está logueado
            val intent = Intent(this, GameLogicaActivity::class.java)
            startActivity(intent)
            finish() // Finalizar esta actividad para evitar volver con el botón "atrás"
            return
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
}
