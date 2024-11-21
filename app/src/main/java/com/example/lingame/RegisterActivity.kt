package com.example.lingame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Arrays


class RegisterActivity : AppCompatActivity() {

    // Variable para Base de datos de Firebase

    private val db = FirebaseFirestore.getInstance()

    // Variable para Facebook
    private val callbackManager = CallbackManager.Factory.create()

    // Definir las vistas
    private lateinit var etNombre: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var etConfirmarContrasena: EditText
    private lateinit var btnComencemos: Button
    private lateinit var btnRegresar: ImageButton
    private lateinit var btnFacebook: LoginButton

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
        btnFacebook = findViewById(R.id.btnFacebook);

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
                // Registro exitoso

                // Guardar los datos en la base de datos
                db.collection("users").document(correo).set(
                    hashMapOf(
                        "name" to nombre,
                        "email" to correo,
                        "password" to contrasena
                    )
                )
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                // Intent para cambiar de actividad o realizar otro proceso
                var intent = Intent(this, LanguageSelectionActivity::class.java)
                startActivity(intent)
            }
        }

        // Evento para el botón "Regresar" (Imagen)
        btnRegresar.setOnClickListener {
            // Regresar a la actividad anterior
            onBackPressedDispatcher.onBackPressed()
        }

        // Evento para el botón de Facebook (Imagen)
        btnFacebook.setReadPermissions(Arrays.asList("email", "public_profile"))


        // If you are using in a fragment, call loginButton.setFragment(this);

        // Callback registration
        btnFacebook.registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
            override fun onSuccess(loginResult: LoginResult?) {
                // App code
            }

            override fun onCancel() {
                // App code
            }

            override fun onError(exception: FacebookException) {
                // App code
            }
        })
    }
}
