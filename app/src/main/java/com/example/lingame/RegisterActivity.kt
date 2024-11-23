package com.example.lingame

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.BitmapShader
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
import java.io.ByteArrayOutputStream
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var bnAvatar: ImageView
    private lateinit var etNombre: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var etConfirmarContrasena: EditText
    private lateinit var btnComencemos: Button
    private lateinit var btnRegresar: ImageButton
    private lateinit var btnFacebook: ImageButton
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager
    private lateinit var db: DBSQLite

    // Launchers para Activity Result API
    private lateinit var selectImageFromGalleryLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestStoragePermissionLauncher: ActivityResultLauncher<String>
    private var cameraImageUri: Uri? = null

    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombre)
        etCorreo = findViewById(R.id.etCorreo)
        etContrasena = findViewById(R.id.etContrasena)
        etConfirmarContrasena = findViewById(R.id.etConfirmarContrasena)
        btnComencemos = findViewById(R.id.btnComencemos)
        btnRegresar = findViewById(R.id.btnRegresar)
        btnFacebook = findViewById(R.id.btnFacebook)
        bnAvatar = findViewById(R.id.ivAvatar)

        firebaseAuth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()
        db = DBSQLite(this)

        // Inicializar Firebase Storage
        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = firebaseStorage.reference

        // Configurar Launchers
        configureActivityResultLaunchers()

        // Eventos
        bnAvatar.setOnClickListener {
            showImagePickerDialog()
        }

        btnComencemos.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()
            val confirmarContrasena = etConfirmarContrasena.text.toString().trim()

            if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty() || confirmarContrasena.isEmpty()) {
                showToast("Todos los campos son obligatorios")
            } else if (contrasena != confirmarContrasena) {
                showToast("Las contraseñas no coinciden")
            } else {
                firebaseAuth.createUserWithEmailAndPassword(correo, contrasena)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            saveUserToFirestore(nombre, correo, firebaseAuth.currentUser)
                            uploadImageToFirebaseStorage()
                        } else {
                            showToast("Error al registrar: ${task.exception?.message}")
                        }
                    }
            }
        }

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
                                    navigateToLanguageSelection()
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

    private fun saveUserToFirestore(nombre: String, correo: String, user: FirebaseUser?) {
        // Referencia a la colección de usuarios en Firestore
        val db = FirebaseFirestore.getInstance()
        val userMap = hashMapOf(
            "name" to nombre,
            "email" to correo,
            "avatarUrl" to "" // Inicialmente dejamos el URL vacío, ya que la imagen se subirá después
        )

        user?.let {
            // Guardar los datos del usuario con su UID como documento
            db.collection("users").document(it.uid)
                .set(userMap)
                .addOnSuccessListener {
                    // Si se guarda correctamente en Firestore, navegar a la selección de idiomas
                    showToast("Registro exitoso")
                    navigateToLanguageSelection()
                }
                .addOnFailureListener { e ->
                    // Si ocurre un error al guardar los datos en Firestore
                    showToast("Error al guardar los datos: ${e.message}")
                }
        }
    }


    private fun uploadImageToFirebaseStorage() {
        bnAvatar.isDrawingCacheEnabled = true
        bnAvatar.buildDrawingCache()
        val bitmap = (bnAvatar.drawable as BitmapDrawable).bitmap
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val data = byteArrayOutputStream.toByteArray()

        val imageRef = storageReference.child("avatars/${UUID.randomUUID()}.jpg")
        val uploadTask = imageRef.putBytes(data)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                saveImageUrlToDatabase(imageUrl)
            }
        }.addOnFailureListener { exception ->
            showToast("Error al subir la imagen: ${exception.message}")
        }
    }

    private fun saveImageUrlToDatabase(imageUrl: String) {
        val nombre = etNombre.text.toString().trim()
        val correo = etCorreo.text.toString().trim()

        // Guardar URL en Firestore
        val FireDB = FirebaseFirestore.getInstance()
        val userMap = hashMapOf("name" to nombre, "email" to correo, "avatarUrl" to imageUrl)
        firebaseAuth.currentUser?.let { user ->
            FireDB.collection("users").document(user.uid).set(userMap)
                .addOnSuccessListener {
                    showToast("Registro exitoso")
                    db.newUser(firebaseAuth.currentUser!!.uid, nombre, correo, imageUrl)
                    navigateToLanguageSelection()
                }
                .addOnFailureListener { e ->
                    showToast("Error al guardar datos: ${e.message}")
                }
        }
    }

    private fun navigateToLanguageSelection() {
        startActivity(Intent(this, LanguageSelectionActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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

    private fun configureActivityResultLaunchers() {
        selectImageFromGalleryLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    val resizedBitmap = resizeImage(it)
                    val roundedBitmap = resizedBitmap?.let { getRoundedBitmap(it) }
                    bnAvatar.setImageBitmap(roundedBitmap)
                }
            }

        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    cameraImageUri?.let {
                        val resizedBitmap = resizeImage(it)
                        val roundedBitmap = resizedBitmap?.let { getRoundedBitmap(it) }
                        bnAvatar.setImageBitmap(roundedBitmap)
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

    private fun resizeImage(uri: Uri): Bitmap? {
        val inputStream = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val width = 500
        val height = (bitmap.height * (width / bitmap.width.toFloat())).toInt()
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    private fun getRoundedBitmap(bitmap: Bitmap): Bitmap {
        val size = Math.min(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()
        val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.shader = shader
        paint.isAntiAlias = true
        val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())
        canvas.drawRoundRect(rect, size / 2f, size / 2f, paint)
        return output
    }
}
