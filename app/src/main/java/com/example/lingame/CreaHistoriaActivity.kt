package com.example.lingame

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.google.firebase.database.*
import com.example.lingame.DecisionFragment.QuestionData

class CreaHistoriaActivity : FragmentActivity() {

    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var background : ImageView

    private var currentQuestion : Int = 0
    private var currentLevel: Int = 1
    private var currentLanguage: String = "es"
    private lateinit var levelQuestions: List<QuestionData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cntrls_crea_historia)

        // Inicializar Firebase y SharedPreferences
        firebaseDatabase = FirebaseDatabase.getInstance().reference
        sharedPreferences = getSharedPreferences(getString(R.string.sharedPreferencesName), Context.MODE_PRIVATE)

        // Obtener idioma actual desde SharedPreferences
        currentLanguage = sharedPreferences.getString(getString(R.string.selectedLanguagePreferences), "es") ?: "es"

        // Obtener nivel actual desde SQLite
        currentLevel = getLevelFromSQLite()

        if (currentLevel >= 3 ) currentLevel = 3

        // Configurar botón de controles
        val button = findViewById<Button>(R.id.btnNextCntrls)

        button.setOnClickListener {
            // Cambiar a la vista de la historia
            setContentView(R.layout.activity_crea_historia)
            // Configurar fondo de pantalla
            background = findViewById(R.id.backgroundImageCH)

            when (currentLevel) {
                1 -> background.setImageResource(R.drawable.story_creation_1_background)
                2 -> background.setImageResource(R.drawable.story_creation_2_background)
                3 -> background.setImageResource(R.drawable.story_creation_3_background)
            }

            loadLevelData()
        }
    }

    private fun getLevelFromSQLite(): Int {
        // Implementar lógica para leer el nivel desde SQLite (ejemplo)
        val dbHelper = DBSQLite(this)
        val UID = sharedPreferences.getString(getString(R.string.UID_Preferences), null)

        return dbHelper.getLevelCreateStoryByLanguage(UID!!, currentLanguage)
    }

    private fun loadLevelData() {
        // Determinar el idioma actual según las preferencias compartidas
        val currentLanguageKey = when (sharedPreferences.getString(getString(R.string.selectedLanguagePreferences), "es")) {
            getString(R.string.englishValuePreferences) -> "en"
            getString(R.string.frenchValuePreferences) -> "fr"
            getString(R.string.portugueseValuePreferences) -> "pr"
            else -> "es"
        }

        // Log para identificar el nivel actual
        Log.i("CreaHistoriaActivity", "Cargando datos del nivel $currentLevel")

        // Obtener datos desde Firebase
        firebaseDatabase.child("languages/story_creation/$currentLevel").get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val levelData = mutableListOf<QuestionData>()

                    for (blockSnapshot in dataSnapshot.children) {
                        // Extraer la pregunta en el idioma actual
                        val question = blockSnapshot.child("question").child(currentLanguageKey).value as? String
                        Log.i("CreaHistoriaActivity", "Pregunta encontrada: $question")


                        // Extraer las respuestas en el idioma actual
                        val answersSnapshot = blockSnapshot.child("answers").child(currentLanguageKey)
                        Log.i("CreaHistoriaActivity", "Respuestas encontradas: ${answersSnapshot}")


                        val answers = mutableMapOf<String, Boolean>()
                        for (answerSnapshot in answersSnapshot.children) {
                            val answerText = answerSnapshot.key
                            val isCorrect = answerSnapshot.value as? Boolean ?: false
                            if (answerText != null) {

                                answers[answerText] = isCorrect
                                Log.i("CreaHistoriaActivity", "Respuesta a mandar : ${answerText} : ${isCorrect}")
                            }

                        }

                        // Extraer el mensaje de error en el idioma actual
                        val wrongAnswerMessage = blockSnapshot.child("wrongAnswer").child(currentLanguageKey).value as? String

                        // Validar que todos los datos existan antes de agregarlos
                        if (question != null && answers.isNotEmpty() && wrongAnswerMessage != null) {
                            levelData.add(
                                QuestionData(
                                    level = currentLevel,
                                    question = question,
                                    answers = answers,
                                    wrongAnswerMessage = wrongAnswerMessage
                                )
                            )
                        }
                    }

                    // Aquí almacenas los datos obtenidos (en una lista, variable global o similar)
                    this.levelQuestions = levelData
                    Log.i("CreaHistoriaActivity", "Datos cargados exitosamente: ${levelData.size} bloques encontrados")

                    // Iniciar el primer fragmento o cualquier acción adicional
                    showNextQuestion()
                } else {
                    Log.e("CreaHistoriaActivity", "No se encontraron datos para el nivel $currentLevel")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("CreaHistoriaActivity", "Error al cargar datos del nivel $currentLevel", exception)
            }
    }


    private fun showNextQuestion() {
        if (currentQuestion < levelQuestions.size) {
            val currentQuestionData = levelQuestions[currentQuestion]
            val fragment = DecisionFragment.newInstance(
                level = currentLevel,
                question = currentQuestionData.question,
                options = currentQuestionData.answers,
                errorMessage = currentQuestionData.wrongAnswerMessage
            )
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        } else {
            Log.i("CreaHistoriaActivity", "Todas las preguntas han sido respondidas")
            showLevelCompleted()
        }
    }



    fun onQuestionAnswered(correct: Boolean) {
        if (correct) {
            currentQuestion++
            if (currentQuestion < levelQuestions.size) {
                showNextQuestion()
            } else {
                showLevelCompleted()
            }
        } else {
            showErrorScreen()
        }
    }


    private fun showErrorScreen() {
        clearCurrentFragment()
        val errorMessage = levelQuestions[currentQuestion].wrongAnswerMessage

        setContentView(R.layout.error_screen_ch) // Pantalla de error
        val errorMessageTextView : TextView = findViewById(R.id.tvErrorMessageCH)
        val characterImage : ImageView = findViewById(R.id.ivCharacterCH_ErrorMessage)
        val backgroundImage : ImageView = findViewById(R.id.ivBackgroundCH_ErrorMessage)

        val retryButton : Button = findViewById(R.id.btnRetryCH)
        val mainMenuButton : Button = findViewById(R.id.btnMainMenuCH)


        when (currentLevel) {
            1 -> {
                characterImage.setImageResource(R.drawable.story_creation_1_girl)
                backgroundImage.setImageResource(R.drawable.story_creation_1_background)
            }
            2 -> {
                characterImage.setImageResource(R.drawable.story_creation_2_friend)
                backgroundImage.setImageResource(R.drawable.story_creation_2_background)
            }
            3 -> {
                characterImage.setImageResource(R.drawable.story_creation_3_angryboss)
                backgroundImage.setImageResource(R.drawable.story_creation_3_background)
            }
        }

        errorMessageTextView.text = errorMessage

        retryButton.setOnClickListener {
            currentQuestion = 0 // Reinicia el progreso de las preguntas
            setContentView(R.layout.activity_crea_historia) // Vuelve al diseño del nivel
            background = findViewById(R.id.backgroundImageCH)

            // Configura el fondo dependiendo del nivel
            when (currentLevel) {
                1 -> background.setImageResource(R.drawable.story_creation_1_background)
                2 -> background.setImageResource(R.drawable.story_creation_2_background)
                3 -> background.setImageResource(R.drawable.story_creation_3_background)
            }

            loadLevelData() // Recarga los datos del nivel actual
        }


        mainMenuButton.setOnClickListener { finish() } // Volver al menú principal
    }

    private fun showLevelCompleted() {
        clearCurrentFragment()

        setContentView(R.layout.lvl_completed_ch) // Pantalla de nivel completado
        var character : ImageView = findViewById(R.id.ivCharacterCH_levelSuccess)
        var  backgroundImage : ImageView = findViewById(R.id.ivBackgroundCH_levelSuccess)
        val nextLevelButton : Button= findViewById(R.id.btnNextLevelCH)

        when (currentLevel) {
            1 -> {
                character.setImageResource(R.drawable.story_creation_1_happygirl)
                backgroundImage.setImageResource(R.drawable.story_creation_1_background)
            }
            2 -> {
                character.setImageResource(R.drawable.story_creation_2_happyfriend)
                backgroundImage.setImageResource(R.drawable.story_creation_2_background)
            }
            3 -> {
                character.setImageResource(R.drawable.story_creation_3_happyboss)
                backgroundImage.setImageResource(R.drawable.story_creation_3_background)
            }
        }


        nextLevelButton.setOnClickListener {
            if (currentLevel < 3){
                currentLevel++ // Incrementa el nivel
            }else{
                currentLevel = 3 // Reinicia el nivel
            }

            currentQuestion = 0 // Reinicia el progreso de las preguntas
            updateLevelInSQLite() // Actualiza el nivel en SQLite
            setContentView(R.layout.activity_crea_historia) // Cambia a la vista principal del nivel

            // Configura el fondo del nuevo nivel
            background = findViewById(R.id.backgroundImageCH)
            when (currentLevel) {
                1 -> background.setImageResource(R.drawable.story_creation_1_background)
                2 -> background.setImageResource(R.drawable.story_creation_2_background)
                3 -> background.setImageResource(R.drawable.story_creation_3_background)
            }

            loadLevelData() // Carga los datos del nuevo nivel
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


    private fun updateLevelInSQLite() {
        val dbHelper = DBSQLite(this)
        val UID = sharedPreferences.getString(getString(R.string.UID_Preferences), null)
        dbHelper.updateLevelCreateStory(UID = UID!!, newLevel = currentLevel, language = currentLanguage)
    }
}
