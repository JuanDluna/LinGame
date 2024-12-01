package com.example.lingame

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity // Cambio aquí
import com.google.firebase.database.*

class TraduceloActivity : AppCompatActivity() {

    private lateinit var scoreBar: scoreBar
    private lateinit var database: DatabaseReference
    private val wordsToTranslate = mutableMapOf<String, String>()
    private val allWords = mutableListOf<Map<String, String>>()
    private var currentWordIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_traducelo)

        // Asegúrate de que `score_bar` exista en tu layout
        scoreBar = findViewById(R.id.score_bar)
        database = FirebaseDatabase.getInstance().getReference("languages/words")

        scoreBar.setMaxScore(5000)

        loadWordsFromDatabase()
    }

    private fun loadWordsFromDatabase() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val wordMap = child.value as? Map<String, String>
                        if (wordMap != null) {
                            allWords.add(wordMap)
                        }
                    }
                    if (allWords.size >= 40) {
                        selectWordsForGame()
                    } else {
                        showErrorAndExit("No hay suficientes palabras para el juego.")
                    }
                } else {
                    showErrorAndExit("No se encontraron palabras en la base de datos.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showErrorAndExit("Error al cargar las palabras: ${error.message}")
            }
        })
    }

    private fun selectWordsForGame() {
        val selectedWords = allWords.shuffled().take(10)

        selectedWords.forEach { wordMap ->
            val originalWord = wordMap["es"] ?: return@forEach
            val translation = wordMap[getLearningLanguage()] ?: return@forEach
            wordsToTranslate[originalWord] = translation
        }

        if (wordsToTranslate.isNotEmpty()) {
            loadNextFragment()
        }
    }

    private fun getLearningLanguage(): String {
        val preferences = getSharedPreferences(getString(R.string.sharedPreferencesName), MODE_PRIVATE)
        return preferences.getString(getString(R.string.selectedLanguagePreferences), null ) ?: "es"
    }

    private fun loadNextFragment() {
        if (currentWordIndex < wordsToTranslate.size) {
            val currentWord = wordsToTranslate.values.elementAt(currentWordIndex)
            val correctTranslation = wordsToTranslate.keys.elementAt(currentWordIndex)

            val options = generateAnswerOptions(correctTranslation)

            val fragment = TranslateItFragment.newInstance(
                wordToTranslate = currentWord,
                correctAnswer = correctTranslation ?: "",
                options = options
            )

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        } else {
            showGameEnd()
        }
    }

    private fun generateAnswerOptions(correctTranslation: String?): List<String> {
        val incorrectOptions = allWords.mapNotNull { it["es"] }
            .filter { it != correctTranslation }
            .shuffled()
            .shuffled()
            .take(3)

        return (listOf(correctTranslation) + incorrectOptions).shuffled() as List<String>
    }

    private fun showGameEnd() {
        val intent = Intent().apply {
            putExtra("win", scoreBar.isFirstStarReached())
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun showErrorAndExit(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        val intent = Intent()
        setResult(RESULT_CANCELED, intent)
        finish()
    }

    fun onNextWord() {
        currentWordIndex++
        loadNextFragment()
    }

    fun incrementScore() {
        scoreBar.incrementScore(wordsToTranslate.values.elementAt(currentWordIndex).length * 100)
    }
}
