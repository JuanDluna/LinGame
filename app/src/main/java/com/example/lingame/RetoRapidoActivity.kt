package com.example.lingame

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.google.firebase.database.*
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.lingame.QuestionRRFragment.Question
import com.example.lingame.QuestionRRFragment.Answer
import com.google.firebase.database.DatabaseReference

class RetoRapidoActivity : FragmentActivity() {

    private lateinit var scoreBar: scoreBar
    private lateinit var timer: TextView
    private var seconds = 60
    private lateinit var database: DatabaseReference
    private var questionsList = mutableListOf<Question>()
    private var currentQuestionIndex = 0 // Control del índice de la pregunta actual

    // Base de datos
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dbHelper : DBSQLite

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reto_rapido)

        // Inicializar base de datos
        dbHelper = DBSQLite(this)
        sharedPreferences = getSharedPreferences(
            getString(R.string.sharedPreferencesName),
            MODE_PRIVATE
        )

        // Inicializar Firebase Database
        database = FirebaseDatabase.getInstance().reference.child("languages").child("reto_rapido")

        // Inicializar la UI
        timer = findViewById(R.id.timerRR)
        scoreBar = findViewById(R.id.scoreBarRR)
        scoreBar.setMaxScore(5000)
        startTimer()

        // Cargar preguntas al iniciar
        loadQuestions()

        // Mostrar la primera pregunta si no hay estado guardado
        if (savedInstanceState == null) {
            showNextQuestion()
        }
    }

    private fun loadQuestions() {
        database.get().addOnSuccessListener { dataSnapshot ->
            val allQuestions = mutableListOf<Question>()

            dataSnapshot.children.forEach { questionSnapshot ->
                val questionMap = questionSnapshot.child("question").value as? Map<String, String>
                val answersMap = questionSnapshot.child("answers").value as? Map<String, List<Boolean>>

                if (questionMap != null && answersMap != null) {
                    val question = Question(
                        question = questionMap,
                        answers = answersMap.map { (key, value) ->
                            Answer(key, value.firstOrNull() ?: false)
                        }
                    )
                    allQuestions.add(question)
                }
            }

            if (allQuestions.isNotEmpty()) {
                allQuestions.shuffle()
                questionsList.clear()
                questionsList.addAll(allQuestions.take(10))
                questionsList.forEach{question ->
                    question.answers.shuffled()
                }
                showNextQuestion()
            } else {
                Toast.makeText(this, "No se encontraron preguntas", Toast.LENGTH_SHORT).show()
                var intent = Intent()
                setResult(RESULT_CANCELED, intent)
                finish()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error al cargar las preguntas: ${exception.message}", Toast.LENGTH_SHORT).show()
            var intent = Intent()
            setResult(RESULT_CANCELED, intent)
            finish()
        }
    }

    fun showNextQuestion() {
        if( questionsList ==  null || questionsList.isEmpty()){
            return
        }
        if (currentQuestionIndex < questionsList.size) {
            val currentQuestion = questionsList[currentQuestionIndex]
            val fragment = QuestionRRFragment.newInstance(currentQuestion)

            supportFragmentManager.beginTransaction()
                .replace(R.id.questionFragmentContainer, fragment)
                .commit()

            currentQuestionIndex++
        } else if (currentQuestionIndex == questionsList.size  ) {
            Toast.makeText(this, "¡Fin del juego!", Toast.LENGTH_SHORT).show()
            var intent = Intent()
            intent.putExtra("win", scoreBar.isFirstStarReached())
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun startTimer() {
        lifecycleScope.launch(Dispatchers.Main) {
            while (seconds > 0) {
                delay(1000)
                seconds--
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                timer.text = String.format("%02d:%02d", minutes, remainingSeconds)
            }
            if (seconds == 0){

                val UID = sharedPreferences.getString(getString(R.string.UID_Preferences), null)
                val actualLanguage = sharedPreferences.getString(getString(R.string.selectedLanguagePreferences), null)
                val actualCategory = getString(R.string.retoRapido)
                val increment = scoreBar.getScore() / 10000F

                // Actualizar la base de datos
                dbHelper.updateLevelsByCategory(UID!!, actualLanguage!!, actualCategory, increment);

                var intent = Intent()
                intent.putExtra("win", scoreBar.isFirstStarReached())
                setResult(RESULT_OK, intent)
                GameLogicaActivity().initHudElements()
                finish()
            }
        }
    }

    fun onQuestionAnswered(isCorrect: Boolean) {
        if (isCorrect) {
        // Aumentar el puntaje si es correcto
            scoreBar.incrementScore( seconds * 10)
        }else{
            seconds -= 10
        }
        showNextQuestion()
    }
}
