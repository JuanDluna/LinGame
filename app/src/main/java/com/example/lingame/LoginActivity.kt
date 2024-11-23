package com.example.lingame

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FacebookAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnIniciarSesion: Button
    private lateinit var btnRegresar: ImageButton
    private lateinit var btnFacebook: ImageButton
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar las vistas
        etCorreo = findViewById(R.id.etCorreo)
        etContrasena = findViewById(R.id.etContrasena)
        btnIniciarSesion = findViewById(R.id.btnIniciar)
        btnRegresar = findViewById(R.id.btnRegresar)
        btnFacebook = findViewById(R.id.ibFacebook)

        // Inicializar Firebase Auth y CallbackManager
        firebaseAuth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

        // Manejo de inicio de sesión por correo y contraseña
        btnIniciarSesion.setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            if (correo.isEmpty() || contrasena.isEmpty()) {
                showToast("Por favor, llena todos los campos")
            } else {
                Log.d("LoginActivity", "Intentando iniciar sesión con correo: $correo")
                firebaseAuth.signInWithEmailAndPassword(correo, contrasena)
                    .addOnCompleteListener { task ->
                        Log.d("LoginActivity", "signInWithEmailAndPassword:onComplete:${task.isSuccessful}")
                        if (task.isSuccessful) {
                            navigateToMain(firebaseAuth.currentUser)
                        } else {
                            showToast("Error: ${task.exception?.message}")
                        }
                    }
            }
        }

        // Manejo de inicio de sesión con Facebook
        btnFacebook.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(
                this,
                listOf("email", "public_profile")
            )
            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult) {
                        val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                        firebaseAuth.signInWithCredential(credential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    navigateToMain(firebaseAuth.currentUser)
                                } else {
                                    showToast("Error de autenticación: ${task.exception?.message}")
                                }
                            }
                    }

                    override fun onCancel() {
                        showToast("Inicio cancelado")
                    }

                    override fun onError(error: FacebookException) {
                        showToast("Error: ${error.message}")
                    }
                }
            )
        }

        btnRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun navigateToMain(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, GameLogicaActivity::class.java))
            finish()
        } else {
            showToast("Error al obtener la información del usuario")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}
