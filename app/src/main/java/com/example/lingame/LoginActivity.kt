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

    private var isLanguageSelected: Boolean = false
    private var pendingTasksCount = 0
    private val lock = Object()

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
        sharedPreferences = getSharedPreferences(getString(R.string.sharedPreferencesName), Context.MODE_PRIVATE)

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
            Log.d("LoginActivity", "Usuario a actualizar en la base de datos: ${userId}")
            val email = user.email ?: ""

            // Incrementa el contador por la consulta a firestore
            incrementPendingTasks("handleUserLogin")
            // Guardar información en base de datos local
            firebaseFirestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val data = document.data ?: return@addOnSuccessListener
                    val name = user.displayName ?: data["name"] as? String ?: "Usuario"
                    val photoUrl = user.photoUrl?.toString() ?: data["avatarUrl"] as? String ?: ""
                    val generalLevel = data["generalLevel"] as? Double ?: 0.0
                    val idiomas = data["idiomas"] as? Map<*,*> ?: emptyMap<Any, Any>()
                    val portugueseLevel = data["portugueseLevel"] as? Map<String, Int> ?: emptyMap()
                    val languagesSelected = data["selectedLanguages"] as List<String>
                    isLanguageSelected = data["isLanguagesSelected"] as? Boolean ?: false

                    Log.d("LoginActivity", "Datos obtenidos: $data")

                    val englishLevel = idiomas.get("Inglés") as? Map<String, Int> ?: emptyMap()
                    val frenchLevel = idiomas.get("Francés") as? Map<String, Int> ?: emptyMap()

                    saveUserToLocalDatabase(userId, name, email, photoUrl,generalLevel, englishLevel, frenchLevel, portugueseLevel)

                    // Marcar como logueado
                    sharedPreferences.edit().apply {
                        putBoolean(getString(R.string.isLoggedInPreferences), true)
                        putString(getString(R.string.UID_Preferences), userId)
                        putStringSet(getString(R.string.listOfLanguagesPreferences), languagesSelected.toSet())
                        apply()
                    }


                    decrementPendingTasks()
                }.addOnFailureListener{
                    showToast("Error al obtener los datos del usuario. Intente de nuevo")
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
        downloadImage(userId, photoFile)

        // Guardar en base de datos SQLite
        if(dbsqLite.isUserExists(userId)){
            Log.d("LoginActivity", "El usuario ya estaba en base de datos, actualizando propiedades.")
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
            Log.d("LoginActivity","Usuario no existente, creando campo de usuario.")
            dbsqLite.newUser(
                UID = userId,
                name = name,
                email = email,
                photo_url = photoFile.absolutePath,
            )
        }
    }

    private fun downloadImage(userID: String, destinationFile: File) {
        if (userID.isBlank()) {
            Log.e("LoginActivity", "El userID está vacío o es inválido")
            return
        }
        Log.d("LoginActivity" ,"UserID (downloadImage) : ${userID}")

        incrementPendingTasks("DownloadImage")

        val storageReference = firebaseStorage.reference.child("avatars/$userID.jpg")


        try {
            // Usa el archivo temporal para guardar la imagen
            val localFile = destinationFile
            if(File(destinationFile.absolutePath).exists()){
                Log.d("LoginActivity", "Archivo actualmente existiendo localmente")
                decrementPendingTasks()
                return
            }
            // Descarga el archivo desde Firebase Storage
            storageReference.getFile(localFile).addOnSuccessListener {
                Log.d("LoginActivity", "Imagen descargada exitosamente, ruta: ${localFile.absolutePath}")
                Log.d("LoginActivity", "Exitencia de fotografia localmente:${File(localFile.absolutePath).exists()} ")
                decrementPendingTasks()
            }.addOnFailureListener { exception ->
                Log.e("LoginActivity", "Error al descargar la imagen: ${exception.message}")
                decrementPendingTasks()
            }
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error desconocido al configurar la descarga de Firebase", e)
            decrementPendingTasks()
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

    private fun incrementPendingTasks(taskName : String) {
        synchronized(lock) {
            Log.i("LoginActivity", "Tarea añadida:${taskName}" )
            pendingTasksCount++
        }
    }

    private fun decrementPendingTasks() {
        synchronized(lock) {
            pendingTasksCount--
            Log.i("LoginActivity", "Tarea pendiente realizada, tareas pendientes: ${pendingTasksCount}")
            if (pendingTasksCount <= 0) {
                runOnUiThread {
                    Log.i("LoginActivity", "Tareas terminadas, ejecutando cambio de pantalla")
                    if (isLanguageSelected)
                        navigateToMain()
                    else
                        navigateToLanguageSelector()
                }
            }
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
