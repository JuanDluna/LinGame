package com.example.lingame

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    // Definir las vistas
    private lateinit var etNombre: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var etConfirmarContrasena: EditText
    private lateinit var btnComencemos: Button
    private lateinit var btnRegresar: ImageButton
    private lateinit var btnFacebook: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializar las vistas
        etNombre = findViewById(R.id.etNombre)
        etCorreo = findViewById(R.id.etCorreo)
        etContrasena = findViewById(R.id.etContrasena)
        etConfirmarContrasena = findViewById(R.id.etConfirmarContrasena)
        btnComencemos = findViewById(R.id.btnComencemos)
        btnRegresar = findViewById(R.id.btnRegresar)
        btnFacebook = findViewById(R.id.btnFacebook)

        // Evento para el botón "¡Comencemos!"
        btnComencemos.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()
            val confirmarContrasena = etConfirmarContrasena.text.toString().trim()

            // Validar los campos (ejemplo básico)
            if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty() || confirmarContrasena.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            } else if (contrasena != confirmarContrasena) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            } else {
                // Aquí agregarías la lógica de registro del usuario
                // Por ejemplo, podrías pasar a la siguiente actividad
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                // Intent para cambiar de actividad o realizar otro proceso
            }
        }

        // Evento para el botón "Regresar" (Imagen)
        btnRegresar.setOnClickListener {
            // Regresar a la actividad anterior
            onBackPressed()
        }

        // Evento para el botón de Facebook (Imagen)
        btnFacebook.setOnClickListener {
            // Implementar acción para logueo con Facebook o lo que sea necesario
            Toast.makeText(this, "Login con Facebook no implementado aún", Toast.LENGTH_SHORT).show()
        }
    }
}
