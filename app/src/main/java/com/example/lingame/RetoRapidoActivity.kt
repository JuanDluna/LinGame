package com.example.lingame

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.google.firebase.database.*
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.lingame.QuestionRRFragment.Question

class RetoRapidoActivity : FragmentActivity() {
    private lateinit var database: DatabaseReference
    private var questionsList = mutableListOf<Question>()
    // Lista de preguntas

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Inicializar Firebase Realtime Database
        database = FirebaseDatabase.getInstance().reference.child("reto_rapido")

        database.get().addOnSuccessListener { dataSnapshot ->

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reto_rapido)


        // Cargar preguntas desde Firebase Realtime Database
        loadQuestions()

        // Cargar el primer fragmento con la primera pregunta
        if (savedInstanceState == null) {
            showNextQuestion()
        }
    }

    private fun loadQuestions() {
        lifecycleScope.launch(Dispatchers.Main) {
            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Supongamos que las preguntas están en el nodo "questions" de Firebase
                    snapshot.children.forEach { questionSnapshot ->
                        val question = questionSnapshot.getValue(Question::class.java)
                        if (question != null) {
                            questionsList.add(question)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RetoRapidoActivity, "Error al cargar preguntas", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun showNextQuestion() {
        if (questionsList.isNotEmpty()) {
            val currentQuestion = questionsList.removeAt(0)

            // Crear el fragmento de la pregunta
            val questionFragment = QuestionRRFragment.newInstance(currentQuestion)

            // Reemplazar el fragmento actual con el siguiente
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, questionFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
        } else {
            Toast.makeText(this, "Has completado el juego", Toast.LENGTH_SHORT).show()
        }
    }

    fun onQuestionAnswered(isCorrect: Boolean) {
        // Manejar el puntaje o la lógica después de una respuesta
        if (isCorrect) {
            // Incrementar puntaje
            Toast.makeText(this, "¡Respuesta correcta!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Respuesta incorrecta", Toast.LENGTH_SHORT).show()
        }

        // Mostrar siguiente pregunta
        showNextQuestion()
    }
}
