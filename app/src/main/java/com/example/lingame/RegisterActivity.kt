package com.example.lingame

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Website.URL
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class RegisterActivity : AppCompatActivity() {

    // Variables de layout
    private lateinit var bnAvatar: ImageView
    private lateinit var etNombre: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var etConfirmarContrasena: EditText
    private lateinit var btnComencemos: Button
    private lateinit var btnRegresar: ImageButton
    private lateinit var btnFacebook: ImageButton

    // API Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    // API Facebook
    private lateinit var callbackManager: CallbackManager

    // Datos locales
    private lateinit var db: DBSQLite
    private lateinit var sharedPreferences: SharedPreferences

    // Launcher para seleccionar imagen

    //Launchers
    private lateinit var selectImageFromGalleryLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestStoragePermissionLauncher: ActivityResultLauncher<String>
    private var cameraImageUri: Uri? = null
    private var bitmapPhoto: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicialización de vistas y Firebase
        initializeComponents()

        // Configurar los Launchers para seleccionar o tomar fotos
        configureActivityResultLaunchers()

        // Configurar eventos
        configureEventListeners()
    }

    private fun initializeComponents() {
        etNombre = findViewById(R.id.etNombre)
        etCorreo = findViewById(R.id.etCorreo)
        etContrasena = findViewById(R.id.etContrasena)
        etConfirmarContrasena = findViewById(R.id.etConfirmarContrasena)
        btnComencemos = findViewById(R.id.btnComencemos)
        btnRegresar = findViewById(R.id.btnRegresar)
        btnFacebook = findViewById(R.id.btnFacebook)
        bnAvatar = findViewById(R.id.ivAvatar)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = firebaseStorage.reference

        callbackManager = CallbackManager.Factory.create()

        db = DBSQLite(this)
        sharedPreferences = getSharedPreferences(R.string.sharedPreferencesName.toString(), Context.MODE_PRIVATE)

    }

    private fun configureEventListeners() {
        bnAvatar.setOnClickListener { showImagePickerDialog() }

        btnComencemos.setOnClickListener { registerUser() }

        btnFacebook.setOnClickListener { loginWithFacebook() }

        btnRegresar.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }


    private fun registerUser() {
        val nombre = etNombre.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val contrasena = etContrasena.text.toString().trim()
        val confirmarContrasena = etConfirmarContrasena.text.toString().trim()

        if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty() || confirmarContrasena.isEmpty()) {
            showToast("Todos los campos son obligatorios")
            return
        }

        if (contrasena != confirmarContrasena) {
            showToast("Las contraseñas no coinciden")
            return
        }

        firebaseAuth.createUserWithEmailAndPassword(correo, contrasena)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    val filePath = saveImageToLocalStorage(bitmapPhoto)
                    Log.d("RegisterActivity", "File path: $filePath")
                    saveUserToDatabase(nombre, correo, filePath)
                    saveUserToFirestore(user)
                    uploadImageToFirebaseStorage(user)
                    userIsLogged()
                } else {
                    showToast("Error al registrar: ${task.exception?.message}")
                }
            }
    }

    private fun loginWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(
            this, listOf("email", "public_profile")
        )
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    handleFacebookAccessToken(result.accessToken)
                }

                override fun onCancel() {
                    showToast("Inicio cancelado")
                }

                override fun onError(error: FacebookException) {
                    showToast("Error: ${error.message}")
                }
            })
    }

    private fun handleFacebookAccessToken(accessToken: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val request = GraphRequest.newMeRequest(accessToken) { obj, _ ->
                        val name = obj?.getString("name") ?: ""
                        val email = obj?.getString("email") ?: ""
                        val pictureUrl = obj?.getJSONObject("picture")
                            ?.getJSONObject("data")?.getString("url") ?: ""

                        saveFacebookUserToLocalDatabase(name, email, pictureUrl)

                        downloadAndSaveProfilePicture(pictureUrl)

                        userIsLogged()
                    }
                    request.executeAsync()
                } else {
                    showToast("Error de autenticación: ${task.exception?.message}")
                }
            }
    }

    private fun downloadAndSaveProfilePicture(pictureUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(pictureUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)
                saveImageToLocalStorage(bitmap)
            } catch (e: Exception) {
                Log.e("RegisterActivity", "Error al descargar imagen", e)
                withContext(Dispatchers.Main) {
                    showToast("Error al descargar la imagen")
                }
            }
        }
    }


    private fun saveFacebookUserToLocalDatabase(name: String, email: String, pictureUrl: String) {
        db.newUser(UUID.randomUUID().toString(), name, email, pictureUrl)
        showToast("Usuario registrado con Facebook")
        navigateToLanguageSelection()
    }


    private fun resizeImage(uri: Uri): Bitmap? {
        val inputStream = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        return Bitmap.createScaledBitmap(bitmap, 200, 200, false)
    }

    private fun getRoundedBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val size = Math.min(width, height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply { isAntiAlias = true }
        val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())
        canvas.drawOval(rect, paint)
        val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.shader = shader
        canvas.drawOval(rect, paint)
        return output
    }

    private fun saveImageToLocalStorage(bitmap: Bitmap?) : String {
        val file = File(getExternalFilesDir(null), "${firebaseAuth.currentUser?.uid}.jpg")
        bitmap?.let {
            val outputStream = FileOutputStream(file)
            it.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
            showToast("Imagen guardada localmente")
        }
        return file.absolutePath;
    }

    private fun uploadImageToFirebaseStorage(user: FirebaseUser?) {
        val bitmap = (bnAvatar.drawable as BitmapDrawable).bitmap
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val data = byteArrayOutputStream.toByteArray()

        val imageRef = storageReference.child("avatars/${user?.uid}.jpg")
        val uploadTask = imageRef.putBytes(data)

        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                saveUserImageUriToDatabase(user, uri.toString())
            }
        }.addOnFailureListener {
            showToast("Error al subir la imagen: ${it.message}")
        }
    }

    private fun userIsLogged() {
        sharedPreferences.edit().putBoolean(R.string.isLoggedInPreferences.toString(), true).apply()
        sharedPreferences.edit().putString(R.string.UID_Preferences.toString(), firebaseAuth.currentUser?.uid).apply()
    }

    private fun saveUserImageUriToDatabase(user: FirebaseUser?, imageUrl: String) {
        val dbRef = FirebaseFirestore.getInstance()
        val userMap = hashMapOf("avatarUrl" to imageUrl)

        user?.let {
            dbRef.collection("users").document(it.uid).update(userMap as Map<String, Any>)
                .addOnFailureListener {
                    showToast("Error al guardar URL de imagen: ${it.message}")
                }
        }
    }

    private fun navigateToLanguageSelection() {
        startActivity(Intent(this, LanguageSelectionActivity::class.java))
        finish()
    }

    private fun saveUserToFirestore(user: FirebaseUser?){
        val name = etNombre.text.toString().trim()
        val email = etCorreo.text.toString().trim()
        val userMap = hashMapOf(
            "name" to name,
            "email" to email,
            "isLanguagesSelected" to false
        )
        firebaseFirestore.collection("users").document(user!!.uid).set(userMap)
    }

    private fun saveUserToDatabase(name: String, email: String, photo_path: String) {
        db.newUser(firebaseAuth.currentUser?.uid ?: email, name, email, photo_path)
        navigateToLanguageSelection()
    }

    private fun configureActivityResultLaunchers() {
        selectImageFromGalleryLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    val resizedBitmap = resizeImage(it)
                    bitmapPhoto = resizedBitmap?.let { getRoundedBitmap(it) }
                    bnAvatar.setImageBitmap(bitmapPhoto)
                }
            }

        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    cameraImageUri?.let {
                        val resizedBitmap = resizeImage(it)
                        bitmapPhoto = resizedBitmap?.let { getRoundedBitmap(it) }
                        bnAvatar.setImageBitmap(bitmapPhoto)
                    }
                }
            }

        requestCameraPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.TITLE, "Nueva Imagen")
                        put(MediaStore.Images.Media.DESCRIPTION, "Tomada desde la cámara")
                    }
                    cameraImageUri = contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                    )
                    takePictureLauncher.launch(cameraImageUri!!)
                } else {
                    showToast("Permiso de cámara denegado")
                }
            }

        requestStoragePermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    selectImageFromGalleryLauncher.launch("image/*")
                } else {
                    showToast("Permiso de almacenamiento denegado")
                }
            }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Elegir de galería", "Tomar una foto")
        AlertDialog.Builder(this)
            .setTitle("Selecciona una opción")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> selectImageFromGalleryLauncher.launch("image/*")
                    1 -> {
                        if (checkCameraPermission()) {
                            val values = ContentValues().apply {
                                put(MediaStore.Images.Media.TITLE, "Nueva Imagen")
                                put(MediaStore.Images.Media.DESCRIPTION, "Tomada desde la cámara")
                            }
                            cameraImageUri = contentResolver.insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                values
                            )
                            takePictureLauncher.launch(cameraImageUri!!)
                        } else {
                            requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    }
                }
            }
            .show()
    }

    private fun checkCameraPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
