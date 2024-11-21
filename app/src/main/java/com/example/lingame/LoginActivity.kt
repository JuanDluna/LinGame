package com.example.lingame

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.facebook.login.widget.LoginButton

class LoginActivity : AppCompatActivity() {

    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnIniciarSesion: Button
    private lateinit var btnRegresar: ImageButton
    private lateinit var btnFacebook: LoginButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar las vistas
        etCorreo = findViewById(R.id.etCorreo)
        etContrasena = findViewById(R.id.etContrasena)
        btnIniciarSesion = findViewById(R.id.btnIniciar)
        btnRegresar = findViewById(R.id.btnRegresar)
        btnFacebook = findViewById(R.id.btnFacebook)

        // Evento para el botón "Iniciar Sesión"
        btnIniciarSesion.setOnClickListener {
            // Lógica para iniciar sesión
        }

        // Evento para el botón "Regresar   "
        btnRegresar.setOnClickListener {
            // Regresar a la actividad anterior
            onBackPressedDispatcher.onBackPressed()
        }

        // Evento para el botón de Facebook

    }
}