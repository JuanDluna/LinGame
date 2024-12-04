package com.example.lingame

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity // Cambio aquí
import com.google.firebase.database.*

class TraduceloActivity : AppCompatActivity() {

    private lateinit var scoreBar: scoreBar
    private lateinit var database: DatabaseReference
    private val wordsToTranslate = mutableMapOf<String, String>()
    private val allWords = mutableListOf<Map<String, String>>()
    private var currentWordIndex = 0

    private lateinit var dbHelper : DBSQLite
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_traducelo)

        // Inicializar base de datos
        dbHelper = DBSQLite(this)
        sharedPreferences = getSharedPreferences(
            getString(R.string.sharedPreferencesName),
            MODE_PRIVATE
        )

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
        var selectedLanguage =  preferences.getString(getString(R.string.selectedLanguagePreferences), null)// Cambiar esto según la lógica de selección de idioma

        return when (selectedLanguage) {
            getString(R.string.englishValuePreferences) -> "en"
            getString(R.string.frenchValuePreferences) -> "fr"
            getString(R.string.portugueseValuePreferences) -> "pr"
            else -> "es"
        }
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
            winnerView()
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

    private fun winnerView(){
        clearCurrentFragment()
        setContentView(R.layout.level_complete)
        val button : Button = this.findViewById(R.id.btnContinuarLevelComplete)
        var star1 : ImageView = this.findViewById(R.id.star1)
        var star2 : ImageView = this.findViewById(R.id.star2)
        var star3 : ImageView = this.findViewById(R.id.star3)
        var score : TextView = this.findViewById(R.id.tvPuntaje)

        score.text = "Puntaje: ${scoreBar.getScore()}"

        if (scoreBar.isFirstStarReached()){
            star1.drawable.setTint(Color.YELLOW)
        }else{
            star1.drawable.setTint(Color.GRAY)
        }
        if (scoreBar.isSecondStarReached()){
            star2.drawable.setTint(Color.YELLOW)
        }else{
            star2.drawable.setTint(Color.GRAY)
        }
        if (scoreBar.isThirdStarReached()){
            star3.drawable.setTint(Color.YELLOW)
        }else{
            star3.drawable.setTint(Color.GRAY)
        }

        if (scoreBar.isFirstStarReached()){
            val UID = sharedPreferences.getString(getString(R.string.UID_Preferences), null)
            val actualLanguage = sharedPreferences.getString(getString(R.string.selectedLanguagePreferences), null)
            val actualCategory = getString(R.string.traducelo)
            val increment = scoreBar.getScore() / 10000F
            // Actualizar la base de datos
            dbHelper.updateLevelsByCategory(UID!!, actualLanguage!!, actualCategory, increment);
        }

        button.setOnClickListener {
            var intent = Intent()
            intent.putExtra("win", scoreBar.isFirstStarReached())
            setResult(RESULT_OK, intent)
            finish()
        }

    }

    private fun clearCurrentFragment() {
        val fragmentManager = supportFragmentManager
        val currentFragment = fragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment != null) {
            fragmentManager.beginTransaction()
                .remove(currentFragment)
                .commit()
        }
    }
}
