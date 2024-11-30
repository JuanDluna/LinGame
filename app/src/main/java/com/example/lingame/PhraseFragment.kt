package com.example.lingame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton


class PhraseFragment : Fragment() {

    data class Word(val original: String, val shuffled: String)  // Data class para la palabra
    data class Phrases(val phrases: List<String>){
        private val phrasesList = phrases
        private var index = 0
        fun getPhrases(): List<String> {
            return phrasesList
        }
        fun shufflePhrases(): List<String> {
            return phrasesList.shuffled()
        }

        fun nextPhrase(): String {
            index++
            return phrasesList.
        }

        fun isEndOfList(): Boolean {
            return index >= phrasesList.size - 1
        }

    }

    private var wordsOfPhrase = MutableList<String>()   // Lista de palabras mezcladas

    private var currentIndex = 0  // Índice para el seguimiento de la frase actual

    companion object {
        // Pasamos las palabras desde la actividad a este fragmento
        fun newInstance(wordList: List<Word>): PhraseFragment {
            return PhraseFragment().apply {
                this.wordList = wordList
            }
        }
    }

    private lateinit var wordContainer: LinearLayout
    private lateinit var answerContainer: LinearLayout
    private val selectedWords = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_phrase, container, false)

        // Inicializar contenedores
        wordContainer = rootView.findViewById(R.id.word_container)
        answerContainer = rootView.findViewById(R.id.answer_container)

        // Obtener palabras mezcladas
        val shuffledWords = wordList[currentIndex].shuffled.split(" ")
        if (shuffledWords.isEmpty()) {
            Toast.makeText(requireContext(), "No hay palabras para mostrar", Toast.LENGTH_SHORT).show()
            return rootView
        }

        configureShuffledWords(shuffledWords)
        rootView.findViewById<View>(R.id.next_button).setOnClickListener { onNextButtonClicked() }

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
        }
    }

    private fun onWordSelected(button: MaterialButton) {
        val word = button.text.toString()
        selectedWords.add(word)

        // Mover palabra al contenedor de respuestas
        wordContainer.removeView(button)
        answerContainer.addView(createWordButton(word))
    }

    private fun onNextButtonClicked() {
        val userAnswer = selectedWords.joinToString(" ")
        val isCorrect = isCorrectAnswer(userAnswer)
        (activity as? ParafraseaActivity)?.onPhraseCompleted(isCorrect)
    }

    private fun isCorrectAnswer(userAnswer: String): Boolean {
        return userAnswer.trim() == wordList[currentIndex].original
    }

    private fun moveToNextPhrase(): Boolean {
        return if (currentIndex < wordList.size - 1) {
            currentIndex++
            true
        } else {
            false
        }
    }
}
