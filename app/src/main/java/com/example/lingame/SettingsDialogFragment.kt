package com.example.lingame

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AppCompatDelegate

class SettingsDialogFragment : DialogFragment() {


    private lateinit var cbUSA: CheckBox
    private lateinit var cbFrance: CheckBox
    private lateinit var cbBrazil: CheckBox

    private lateinit var btnSaveSettings: Button
    private lateinit var btnCancelSettings: Button

    private lateinit var preferences: SharedPreferences
    private lateinit var dbHelper: DBSQLite
    private lateinit var UID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = requireContext().getSharedPreferences(getString(R.string.sharedPreferencesName), Context.MODE_PRIVATE)
        dbHelper = DBSQLite(requireContext())
        UID = preferences.getString(getString(R.string.UID_Preferences), null) ?: ""

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflamos el diseño del overlay
        return inflater.inflate(R.layout.overlay_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar vistas
        cbUSA = view.findViewById(R.id.cbUSA)
        cbFrance = view.findViewById(R.id.cbFrance)
        cbBrazil = view.findViewById(R.id.cbBrazil)
        btnSaveSettings = view.findViewById(R.id.btnSaveSettings)
        btnCancelSettings = view.findViewById(R.id.btnCancelSettings)

        // Cargar valores guardados
        loadSettings()

        // Configurar botones de acción
        btnSaveSettings.setOnClickListener {
            saveSettings()
            dismiss()
        }

        btnCancelSettings.setOnClickListener {
            dismiss()
        }
    }

    private fun loadSettings() {


        // Cargar idiomas seleccionados
        val selectedLanguagesList = preferences.getStringSet(getString(R.string.listOfLanguagesPreferences), emptySet())
        selectedLanguagesList!!.add(preferences.getString(getString(R.string.selectedLanguagePreferences), null))

        selectedLanguagesList!!.forEach {
            when (it) {
                getString(R.string.englishValuePreferences) -> cbUSA.isChecked = true
                getString(R.string.frenchValuePreferences) -> cbFrance.isChecked = true
                getString(R.string.portugueseValuePreferences) -> cbBrazil.isChecked = true
            }
        }
    }

    private fun saveSettings() {

        // Guardar idiomas seleccionados
        val selectedLanguages = mutableSetOf<String>()
        var selectedLanguage = preferences.getString(getString(R.string.selectedLanguagePreferences), null)
        if (cbUSA.isChecked) {
            selectedLanguages.add(getString(R.string.englishValuePreferences))
            dbHelper.createTableLanguageIfDoestnExists(UID, getString(R.string.englishValuePreferences))
        }
        if (cbFrance.isChecked){
            selectedLanguages.add(getString(R.string.frenchValuePreferences))
            dbHelper.createTableLanguageIfDoestnExists(UID, getString(R.string.frenchValuePreferences))
        }
        if (cbBrazil.isChecked){
            selectedLanguages.add(getString(R.string.portugueseValuePreferences))
            dbHelper.createTableLanguageIfDoestnExists(UID, getString(R.string.portugueseValuePreferences))
        }

        if (!selectedLanguages.contains(selectedLanguage)){
            selectedLanguage = selectedLanguages.firstOrNull()
        }

        // Guardar todos los valores en SharedPreferences
        preferences.edit()
            .putString(getString(R.string.selectedLanguagePreferences), selectedLanguage)

            .putStringSet(getString(R.string.listOfLanguagesPreferences), selectedLanguages)
            .apply()

        (activity as GameLogicaActivity)?.setupLanguageSelector()


        // Log para depuración
        Log.i("SettingsDialogFragment", "Settings saved: Languages=$selectedLanguages")
    }

}
