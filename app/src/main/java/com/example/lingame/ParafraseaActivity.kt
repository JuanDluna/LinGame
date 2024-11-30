package com.example.lingame

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.firebase.database.FirebaseDatabase
import com.example.lingame.PhraseFragment.Word
import com.example.lingame.PhraseFragment.Phrases

class ParafraseaActivity : FragmentActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var phrasesList: Phrases

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parafrasea)

        // Inicializar sharedPreferences
        sharedPreferences = getSharedPreferences(
            getString(R.string.sharedPreferencesName),
            MODE_PRIVATE)

        // Obtener idioma seleccionado
        val selectedLanguage = sharedPreferences.getString(
            getString(R.string.selectedLanguagePreferences),
            null)

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
                    phrases.shuffle()

                    phrasesList = Phrases( phrases.take(5))

                    Log.d("ParafraseaActivity", "Frases cargadas: ${phrasesList.getPhrases()}")
                    showPhraseFragment()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar frases", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showPhraseFragment() {
        val fragment = PhraseFragment.newInstance(phrasesList.nextPhrase())
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun onPhraseCompleted(isCorrect: Boolean) {
        if (isCorrect) {
            // Aumentar el puntaje
            Toast.makeText(this, "Respuesta correcta", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Respuesta incorrecta", Toast.LENGTH_SHORT).show()
        }

        // Pasar a la siguiente frase o terminar el juego
        if (wordList.isEmpty()) {
            Toast.makeText(this, "¡Juego terminado!", Toast.LENGTH_LONG).show()
            finish()
        } else {
            showPhraseFragment()
        }
    }
}
