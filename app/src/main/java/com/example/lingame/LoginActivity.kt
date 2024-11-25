package com.example.lingame

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import kotlin.concurrent.thread

class LoginActivity : AppCompatActivity() {

    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnIniciarSesion: Button
    private lateinit var btnRegresar: ImageButton
    private lateinit var btnFacebook: ImageButton
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var callbackManager: CallbackManager
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dbsqLite: DBSQLite

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
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        callbackManager = CallbackManager.Factory.create()

        // Inicializar la base de datos SQLite y variables locales
        dbsqLite = DBSQLite(this)
        sharedPreferences = getSharedPreferences(R.string.sharedPreferencesName.toString(), Context.MODE_PRIVATE)

        // Manejo de inicio de sesión por correo y contraseña
        btnIniciarSesion.setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            if (correo.isEmpty() || contrasena.isEmpty()) {
                showToast("Por favor, llena todos los campos")
            } else {
                firebaseAuth.signInWithEmailAndPassword(correo, contrasena)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            handleUserLogin(firebaseAuth.currentUser)
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
                                    handleUserLogin(firebaseAuth.currentUser)
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

    private fun handleUserLogin(user: FirebaseUser?) {
        if (user != null) {
            val userId = user.uid
            val email = user.email ?: ""

            // Guardar información en base de datos local
            firebaseFirestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val data = document.data ?: return@addOnSuccessListener
                    val name = user.displayName ?: data["name"] as? String ?: "Usuario"
                    val photoUrl = user.photoUrl?.toString() ?: data["avatarUrl"] as? String ?: ""
                    val generalLevel = data["generalLevel"] as? Double ?: 0.0
                    val englishLevel = data["englishLevel"] as? Map<String, Int> ?: emptyMap()
                    val frenchLevel = data["frenchLevel"] as? Map<String, Int> ?: emptyMap()
                    val portugueseLevel = data["portugueseLevel"] as? Map<String, Int> ?: emptyMap()
                    val isLanguageSelected = data["isLanguagesSelected"] as? Boolean ?: false

                    Log.d("LoginActivity", "Datos obtenidos: $data")
                    Log.d("LoginActivity", "Nombre: $name, Email: $email, Photo URL: $photoUrl")
                    Log.d("LoginActivity", "General Level: $generalLevel")
                    Log.d("LoginActivity", "English Level: $englishLevel")
                    Log.d("LoginActivity", "French Level: $frenchLevel")
                    Log.d("LoginActivity", "Portuguese Level: $portugueseLevel")
                    Log.d("LoginActivity", "isLanguageSelected: $isLanguageSelected")

                    saveUserToLocalDatabase(userId, name, email, photoUrl,generalLevel, englishLevel, frenchLevel, portugueseLevel)

                    // Marcar como logueado
                    sharedPreferences.edit().apply {
                        putBoolean(R.string.isLoggedInPreferences.toString(), true)
                        putString(R.string.UID_Preferences.toString(), userId)
                        apply()
                    }

                    if (isLanguageSelected){
                        navigateToMain()
                    }else{
                        navigateToLanguageSelector()
                    }
                }
        } else {
            showToast("Error al obtener la información del usuario")
        }
    }

    private fun saveUserToLocalDatabase(
        userId: String,
        name: String,
        email: String,
        photoUrl: String,
        generalLevel: Double,
        englishLevel: Map<String, Int>,
        frenchLevel: Map<String, Int>,
        portugueseLevel: Map<String, Int>) {


        // Descargar imagen de perfil
        val photoFile = File(filesDir, "$userId.jpg")
        downloadImage(photoUrl, photoFile)

        // Guardar en base de datos SQLite
        if(dbsqLite.isUserExists(userId)){
            dbsqLite.setUserData(
                UID = userId,
                name = name,
                email = email,
                generalLevel = generalLevel.toFloat(),
                photoUrl = photoFile.absolutePath,
                englishLevel = englishLevel,
                frenchLevel = frenchLevel,
                portugueseLevel = portugueseLevel
            )
        }else{
            dbsqLite.newUser(
                UID = userId,
                name = name,
                email = email,
                photo_url = photoFile.absolutePath,
            )
        }
    }

    private fun downloadImage(urlString: String, destinationFile: File) {
        if (urlString.isBlank()) {
            Log.e("LoginActivity", "URL vacía o inválida")
            return
        }

        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)
                saveBitmapToFile(bitmap, destinationFile)
                Log.d("LoginActivity", "Imagen descargada y guardada")
                inputStream.close()
            } else {
                Log.e("LoginActivity", "Error al descargar la imagen: Código de respuesta $responseCode")
            }
        } catch (e: MalformedURLException) {
            Log.e("LoginActivity", "URL mal formada: ${e.message}")
        } catch (e: IOException) {
            Log.e("LoginActivity", "Error de red: ${e.message}")
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error desconocido", e)
        }
    }


    private fun saveBitmapToFile(bitmap: Bitmap, file: File) {
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error al guardar la imagen: ${e.message}")
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, GameLogicaActivity::class.java))
        finish()
    }

    private fun navigateToLanguageSelector() {
        startActivity(Intent(this, LanguageSelectionActivity::class.java))
        finish()
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}
