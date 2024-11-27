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

class RetoRapidoActivity : FragmentActivity() {

    private lateinit var scoreBar: scoreBar
    private lateinit var timer: TextView

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database: DatabaseReference
    private var questionsList = mutableListOf<Question>()
    // Lista de preguntas
    private var currentQuestionIndex = 0  // Para llevar el control de la pregunta actual

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reto_rapido)

        // Inicializador shared preferences

        // Inicializar Firebase Realtime Database
        database = FirebaseDatabase.getInstance().reference.child("languages").child("reto_rapido")

        // Inicializar la UI
        timer = findViewById(R.id.timerRR)
        scoreBar = findViewById(R.id.scoreBarRR)
        scoreBar.setMaxScore(5000)
        startTimer()

        // Cargar preguntas al iniciar
        loadQuestions()

        // Cargar el primer fragmento con la primera pregunta
        if (savedInstanceState == null) {
            showNextQuestion()
        }
    }

    private fun loadQuestions() {
        // Obtener todas las preguntas de Firebase
        database.get().addOnSuccessListener { dataSnapshot ->
            val allQuestions = mutableListOf<Question>()

            // Recorrer los datos obtenidos
            dataSnapshot.children.forEach { questionSnapshot ->
                val question = questionSnapshot.getValue(Question::class.java)
                if (question != null) {
                    allQuestions.add(question)
                }
            }

            if (allQuestions.isNotEmpty()) {
                // Mezclar preguntas aleatoriamente
                allQuestions.shuffle()

                // Tomar las primeras 10 preguntas después de mezclar
                questionsList.clear()
                questionsList.addAll(allQuestions.take(10))

                Log.i("RetoRapidoActivity", "Preguntas obtenidas: $questionsList")
                showNextQuestion()
            } else {
                Toast.makeText(this, "No se encontraron preguntas", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error al cargar las preguntas: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNextQuestion() {
        if (currentQuestionIndex < questionsList.size) {
            val currentQuestion = questionsList[currentQuestionIndex]
            // Aquí puedes mostrar la pregunta en la UI, usando los valores del mapa según el idioma
            // Ejemplo de cómo obtener la pregunta en español:
            val questionText = currentQuestion.question[]
            Log.i("RetoRapidoActivity", "Pregunta actual: $questionText")

            // También mostrar las respuestas
            currentQuestion.answers.forEach {
                Log.i("RetoRapidoActivity", "Opción: $it")
            }

            currentQuestionIndex++  // Incrementar el índice para la siguiente pregunta
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
        // Aquí puedes agregar lógica para actualizar el puntaje basado en si la respuesta es correcta
    }
}
