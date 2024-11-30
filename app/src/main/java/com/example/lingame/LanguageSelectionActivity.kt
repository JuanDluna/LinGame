package com.example.lingame

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LanguageSelectionActivity : AppCompatActivity() {

    private lateinit var btnRegresar: AppCompatImageButton
    private lateinit var btnSiguiente: AppCompatImageButton
    private lateinit var checkboxIngles: CheckBox
    private lateinit var checkboxFrances: CheckBox
    private lateinit var checkboxPortugues: CheckBox

    private lateinit var dbHelper: DBSQLite
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_language)  // Asegúrate de tener este layout

        // Inicialización de vistas
        btnRegresar = findViewById(R.id.btnRegresar)
        btnSiguiente = findViewById(R.id.btnSiguiente)
        checkboxIngles = findViewById(R.id.checkboxIngles)
        checkboxFrances = findViewById(R.id.checkboxFrances)
        checkboxPortugues = findViewById(R.id.checkboxPortugues)

        dbHelper = DBSQLite(this)
        firestore = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("LingamePreferences", MODE_PRIVATE)

        // Configuración del botón "Regresar"
        btnRegresar.setOnClickListener {
            finish()  // Cierra la actividad
        }

        // Configuración del botón "Siguiente"
        btnSiguiente.setOnClickListener {
            val selectedLanguages = getSelectedLanguages()
            if (selectedLanguages.isNotEmpty()) {
                val languages = selectedLanguages.joinToString(", ")
                val UID = FirebaseAuth.getInstance().currentUser?.uid ?: sharedPreferences.getString(getString(R.string.UID_Preferences), null)
                Toast.makeText(this, "Idiomas seleccionados: $languages", Toast.LENGTH_SHORT).show()

                selectedLanguages.forEach { language ->
                    var languagesMap = mapOf( language.toString() to mapOf(
                        "levelCreaHistoria" to 0,
                        "levelRR" to 0,
                        "levelTraducelo" to 0,
                        "levelParafrasea" to 0
                    ) )

                    // Guardar los idiomas en Firestore
                    firestore.collection("users").
                    document(UID.toString()).
                    collection("nivelIdiomas").document(language).set(languagesMap).
                    addOnFailureListener(
                        {
                            Toast.makeText(this, "Error al guardar los idiomas", Toast.LENGTH_SHORT).show()
                        }
                    );
                    // Crear tablas default en SQLite
                    when (language) {
                        "Inglés" -> dbHelper.createEnglishTable(UID.toString())
                        "Francés" -> dbHelper.createFrenchTable(UID.toString())
                        "Portugués" -> dbHelper.createPortugueseTable(UID.toString())
                    }

                }

                // Modificar el valor de languagesSelected en Firestore
                firestore.collection("users").document(UID.toString()).update("isLanguagesSelected", true)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Idiomas guardados correctamente", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Error al guardar los idiomas", Toast.LENGTH_SHORT).show()
                    }

                firestore.collection("users").document(UID.toString()).update("selectedLanguages", selectedLanguages)

                val intent = Intent(this, TutorialActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No has seleccionado ningún idioma", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para obtener los idiomas seleccionados
    private fun getSelectedLanguages(): List<String> {
        val selectedLanguages = mutableListOf<String>()
        if (checkboxIngles.isChecked) selectedLanguages.add("Inglés")
        if (checkboxFrances.isChecked) selectedLanguages.add("Francés")
        if (checkboxPortugues.isChecked) selectedLanguages.add("Portugués")
        sharedPreferences.edit().putStringSet(getString(R.string.listOfLanguagesPreferences), selectedLanguages.toSet()).apply()
        return selectedLanguages
    }
}
