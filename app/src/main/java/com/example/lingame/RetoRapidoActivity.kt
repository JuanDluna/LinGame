package com.example.lingame

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

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database: DatabaseReference
    private var questionsList = mutableListOf<Question>()
    private var currentQuestionIndex = 0 // Control del índice de la pregunta actual

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reto_rapido)

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences(R.string.sharedPreferencesName.toString(), MODE_PRIVATE)

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

            Log.i("RetoRapidoActivity", "Cantidad de preguntas cargadas: ${dataSnapshot.childrenCount}")
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
                Log.i("RetoRapidoActivity", "Cantidad de preguntas mezcladas: ${allQuestions.size}")
                questionsList.clear()
                questionsList.addAll(allQuestions.take(10))
                questionsList.forEach{question ->
                    question.answers.shuffled()
                }
                showNextQuestion()
            } else {
                Toast.makeText(this, "No se encontraron preguntas", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error al cargar las preguntas: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun showNextQuestion() {
        if (currentQuestionIndex < questionsList.size) {
            val currentQuestion = questionsList[currentQuestionIndex]
            val fragment = QuestionRRFragment.newInstance(currentQuestion)

            supportFragmentManager.beginTransaction()
                .replace(R.id.questionFragmentContainer, fragment)
                .commit()

            currentQuestionIndex++
        } else {
            Toast.makeText(this, "¡Fin del juego!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startTimer() {
        var seconds = 0
        lifecycleScope.launch(Dispatchers.Main) {
            while (seconds < 60) {
                delay(1000)
                seconds++
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                timer.text = String.format("%02d:%02d", minutes, remainingSeconds)
            }
        }
    }

    fun onQuestionAnswered(isCorrect: Boolean) {
        if (isCorrect) {
        // Aumentar el puntaje si es correcto
            scoreBar.incrementScore(500)
        }
        showNextQuestion()
    }
}
