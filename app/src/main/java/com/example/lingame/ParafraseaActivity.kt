package com.example.lingame

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.firebase.database.FirebaseDatabase
import com.example.lingame.PhraseFragment.Phrases

class ParafraseaActivity : FragmentActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var phrasesList: Phrases
    private lateinit var scoreBar: scoreBar
    private lateinit var dbHelper : DBSQLite

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parafrasea)

        //Inicializar la base de datos
        dbHelper = DBSQLite(this)

        // Inicializar vistas
        scoreBar = findViewById(R.id.scoreBarParafrasea)
        scoreBar.setMaxScore(5000)

        // Inicializar sharedPreferences
        sharedPreferences = getSharedPreferences(
            getString(R.string.sharedPreferencesName),
            MODE_PRIVATE
        )

        // Obtener idioma seleccionado
        var selectedLanguage = sharedPreferences.getString(
            getString(R.string.selectedLanguagePreferences),
            null
        )

        selectedLanguage = when (selectedLanguage) {
            getString(R.string.englishValuePreferences) -> "en"
            getString(R.string.frenchValuePreferences) -> "fr"
            getString(R.string.portugueseValuePreferences) -> "pr"
            else -> "es"
        }

        // Obtener frases de Firebase y configurar el juego
        fetchPhrasesFromDatabase(selectedLanguage!!)
    }

    private fun fetchPhrasesFromDatabase(language: String) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("languages/phrases")
            .get()
            .addOnSuccessListener { dataSnapshot ->
                val phrasesListDB = dataSnapshot.value

                if (phrasesListDB is ArrayList<*>) {
                    val phrases = mutableListOf<String>()
                    phrasesListDB.forEach { phrase ->
                        if (phrase is HashMap<*, *>) {
                            phrases.add((phrase[language] ?: "").toString())
                        }
                    }

                    phrasesList = Phrases(phrases.shuffled().take(5))

                    showPhraseFragment()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar frases", Toast.LENGTH_SHORT).show()
                var intent = Intent()
                setResult(RESULT_CANCELED, intent)
                finish()
            }
    }

    private fun showPhraseFragment() {
        if (phrasesList.isEndOfList() && !phrasesList.isEmpty()) {
            val UID = sharedPreferences.getString(getString(R.string.UID_Preferences), null)
            val actualLanguage = sharedPreferences.getString(getString(R.string.selectedLanguagePreferences), null)
            val actualCategory = getString(R.string.parafrasea)
            val increment = scoreBar.getScore() / 10000F

            // Actualizar la base de datos
            dbHelper.updateLevelsByCategory(UID!!, actualLanguage!!, actualCategory, increment);

            var intent = Intent()
            intent.putExtra("win", scoreBar.isFirstStarReached())
            setResult(RESULT_OK, intent)
            GameLogicaActivity().initHudElements()
            finish()
        } else {
            val nextPhrase = phrasesList.nextPhrase()
            val fragment = PhraseFragment.newInstance(nextPhrase)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }

    fun onPhraseCompleted(isCorrect: Boolean) {
        if (isCorrect) {
            scoreBar.incrementScore( phrasesList.getActualPhrase().length * 50)
            Toast.makeText(this, "Respuesta correcta", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Respuesta incorrecta", Toast.LENGTH_SHORT).show()
        }

        // Mostrar la siguiente frase
        showPhraseFragment()
    }
}
