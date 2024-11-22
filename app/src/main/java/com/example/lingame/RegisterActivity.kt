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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Arrays


class RegisterActivity : AppCompatActivity() {


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

            // Validar los campos
            if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty() || confirmarContrasena.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            } else if (contrasena != confirmarContrasena) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            } else {
                // Crear un nuevo usuario con Firebase Authentication
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(correo, contrasena)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Obtener el UID del usuario registrado
                            val userId = task.result?.user?.uid

                            // Guardar información adicional del usuario en Firestore
                            if (userId != null) {
                                val db = FirebaseFirestore.getInstance()
                                db.collection("users").document(userId).set(
                                    hashMapOf(
                                        "name" to nombre,
                                        "email" to correo
                                    )
                                ).addOnSuccessListener {
                                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                    // Navegar a la siguiente actividad
                                    val intent = Intent(this, LanguageSelectionActivity::class.java)
                                    startActivity(intent)
                                }.addOnFailureListener { e ->
                                    Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            // Mostrar mensaje de error si el registro falla
                            Toast.makeText(this, "Error al registrar: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
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
