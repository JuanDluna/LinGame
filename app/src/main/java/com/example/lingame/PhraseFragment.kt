package com.example.lingame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.contains
import androidx.fragment.app.Fragment
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.button.MaterialButton

class PhraseFragment : Fragment() {

    data class Phrases(val phrases: List<String>){
        private val phrasesList = phrases
        private var index = 0

        fun getActualPhrase(): String {
            return phrasesList.get(index)
        }

        fun nextPhrase(): String {
            index++
            return phrasesList.get(index)
        }

        fun isEndOfList(): Boolean {
            return index >= phrasesList.size - 1
        }

        fun isEmpty(): Boolean {
            return phrasesList.isEmpty()
        }
    }

    companion object {
        // Pasamos las palabras desde la actividad a este fragmento
        fun newInstance(phrase: String): PhraseFragment {
            return PhraseFragment().apply {
                arguments = Bundle().apply {
                    putString("phrase", phrase)
                }
            }
        }
    }
    private lateinit var wordContainer: FlexboxLayout
    private lateinit var answerContainer: FlexboxLayout
    private lateinit var nextButton: MaterialButton

    private val selectedWords = mutableListOf<String>()
    private var wordsOfPhrase = mutableListOf<String>()
    private var isCorrect : Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_phrase, container, false)

        // Inicializar vistas
        wordContainer = rootView.findViewById(R.id.word_container)
        answerContainer = rootView.findViewById(R.id.answer_container)
        nextButton = rootView.findViewById(R.id.next_button)

        // Obtener la frase y dividirla en palabras mezcladas
        val phrase = arguments?.getString("phrase") ?: ""
        wordsOfPhrase = phrase.split(" ").toMutableList()
        wordsOfPhrase.shuffle()

        if (wordsOfPhrase.isEmpty()) {
            Toast.makeText(requireContext(), "No hay palabras para mostrar", Toast.LENGTH_SHORT).show()
            return rootView
        }

        configureShuffledWords(wordsOfPhrase)

        // Configurar el botón
        nextButton.text = "Revisar frase" // Texto inicial
        nextButton.setOnClickListener { onNextButtonClicked(phrase) }

        return rootView
    }

    private fun configureShuffledWords(words: List<String>) {
        wordContainer.removeAllViews()
        answerContainer.removeAllViews()
        selectedWords.clear()

        words.forEach { word ->
            val button = createWordButton(word)
            wordContainer.addView(button)
        }
    }

    private fun createWordButton(word: String): MaterialButton {
        return MaterialButton(requireContext()).apply {
            text = word
            setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
            setOnClickListener { onWordSelected(this) }
            textSize = 18f // Aumentar el tamaño de la fuente
        }
    }

    private fun onWordSelected(button: MaterialButton) {
        val word = button.text.toString()
        if (answerContainer.contains(button)) {
            // Mover palabra de regreso al contenedor de palabras
            answerContainer.removeView(button)
            wordContainer.addView(button)
            selectedWords.remove(word)
        } else {
            // Mover palabra al contenedor de respuestas
            wordContainer.removeView(button)
            answerContainer.addView(button)
            selectedWords.add(word)
        }
    }

    private fun onNextButtonClicked(correctPhrase: String) {
        if (nextButton.text == "Revisar frase") {
            // Revisar la frase y mostrar retroalimentación
            val userAnswer = selectedWords.joinToString(" ")
            checkPhrase(userAnswer, correctPhrase)

            // Cambiar el texto del botón
            nextButton.text = "Siguiente frase"
        } else {
            // Pasar a la siguiente frase
            (activity as? ParafraseaActivity)?.onPhraseCompleted(isCorrect)
        }
    }

    private fun checkPhrase(userAnswer: String, correctPhrase: String) {
        wordContainer.removeAllViews()

        // Crear el mensaje "Respuesta correcta:" y agregarlo al contenedor
        val correctLabel = createLabel("Respuesta correcta:")
        wordContainer.addView(correctLabel)

        val userWords = userAnswer.split(" ")
        val correctWords = correctPhrase.split(" ")

        correctWords.forEachIndexed { index, word ->
            val button = MaterialButton(requireContext()).apply {
                text = word
                textSize = 18f
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (index < userWords.size && userWords[index] == word) {
                            android.R.color.holo_green_dark
                        } else {
                            android.R.color.holo_red_dark;
                        }
                    )
                )
            }
            // Si una sola palabra esta mal la respuesta esta incorrecta, en caso contrario se mantiene el true de que todo esta correcto
            if (userWords[index] != word ) isCorrect = false
            wordContainer.addView(button)
        }
    }

    private fun createLabel(text: String): MaterialButton {
        return MaterialButton(requireContext()).apply {
            this.text = text
            textSize = 16f
            isClickable = false // No se puede hacer clic en el texto
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark))
            setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
        }
    }
}