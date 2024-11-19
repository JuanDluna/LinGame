package com.example.lingame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.initial_screen)

        // Referencias a los elementos de la vista
        val btnComienza: Button = findViewById(R.id.btnComienza)
        val btnIniciarSesion: Button = findViewById(R.id.btnIniciarSesion)
        val btnSettings: ImageButton = findViewById(R.id.btnSettings)

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

        btnSettings.setOnClickListener {
            // Navegar a la pantalla de configuración
//            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }


}
