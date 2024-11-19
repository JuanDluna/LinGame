package com.example.lingame

import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton

class LanguageSelectionActivity : AppCompatActivity() {

    private lateinit var btnRegresar: AppCompatImageButton
    private lateinit var btnSiguiente: AppCompatImageButton
    private lateinit var checkboxIngles: CheckBox
    private lateinit var checkboxFrances: CheckBox
    private lateinit var checkboxPortugues: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_language)  // Asegúrate de tener este layout

        // Inicialización de vistas
        btnRegresar = findViewById(R.id.btnRegresar)
        btnSiguiente = findViewById(R.id.btnSiguiente)
        checkboxIngles = findViewById(R.id.checkboxIngles)
        checkboxFrances = findViewById(R.id.checkboxFrances)
        checkboxPortugues = findViewById(R.id.checkboxPortugues)

        // Configuración del botón "Regresar"
        btnRegresar.setOnClickListener {
            finish()  // Cierra la actividad
        }

        // Configuración del botón "Siguiente"
        btnSiguiente.setOnClickListener {
            val selectedLanguages = getSelectedLanguages()
            if (selectedLanguages.isNotEmpty()) {
                val languages = selectedLanguages.joinToString(", ")
                Toast.makeText(this, "Idiomas seleccionados: $languages", Toast.LENGTH_SHORT).show()
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
        return selectedLanguages
    }
}
