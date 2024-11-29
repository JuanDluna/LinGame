package com.example.lingame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton

class PhraseFragment : Fragment() {

    // ViewModel definido como subclase
    class ParafraseaViewModel : ViewModel() {
        private val _phrases = MutableLiveData<List<String>>()
        val phrases: LiveData<List<String>> get() = _phrases

        fun setPhrases(phrases: List<String>) {
            _phrases.value = phrases
        }

        // Método para obtener las palabras mezcladas de una frase
        fun getShuffledWords(index: Int): List<String> {
            val phrases = _phrases.value ?: return emptyList()
            return phrases.getOrNull(index)?.split(" ")?.shuffled() ?: emptyList()
        }
    }

    private lateinit var viewModel: ParafraseaViewModel
    private var phraseIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Obtener ViewModel desde la actividad
        viewModel = ViewModelProvider(requireActivity()).get(ParafraseaViewModel::class.java)
        // Obtener el índice de la frase desde los argumentos
        phraseIndex = arguments?.getInt(ARG_PHRASE_INDEX) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_phrase, container, false)

        // Configurar las palabras mezcladas
        val shuffledWords = viewModel.getShuffledWords(phraseIndex)
        if (shuffledWords.isEmpty()) {
            Toast.makeText(requireContext(), "No hay palabras para mostrar", Toast.LENGTH_SHORT).show()
            return rootView
        }

        val wordContainer = rootView.findViewById<LinearLayout>(R.id.word_container)

        // Agregar botones para cada palabra
        shuffledWords.forEach { word ->
            val button = MaterialButton(requireContext()).apply {
                text = word
                setOnClickListener { onWordClicked(this) }
            }
            wordContainer.addView(button)
        }

        return rootView
    }

    private fun onWordClicked(button: MaterialButton) {
        // Implementar lógica para manejar el clic en las palabras
        Toast.makeText(requireContext(), "Palabra seleccionada: ${button.text}", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val ARG_PHRASE_INDEX = "phrase_index"

        fun newInstance(index: Int): PhraseFragment {
            return PhraseFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PHRASE_INDEX, index)
                }
            }
        }
    }
}
