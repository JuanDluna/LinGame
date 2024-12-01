package com.example.lingame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.core.view.marginEnd
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import com.google.android.flexbox.FlexboxLayout

class TranslateItFragment : Fragment() {

    companion object {
        fun newInstance(wordToTranslate: String, correctAnswer: String, options: List<String>): TranslateItFragment {
            return TranslateItFragment().apply {
                arguments = Bundle().apply {
                    putString("wordToTranslate", wordToTranslate)
                    putString("correctAnswer", correctAnswer)
                    putStringArrayList("options", ArrayList(options))
                }
            }
        }
    }

    private lateinit var wordTextView: TextView
    private lateinit var optionsContainer: FlexboxLayout
    private lateinit var nextButton: Button

    private var correctAnswer: String = ""
    private var isAnswered: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_translate_it, container, false)

        wordTextView = rootView.findViewById(R.id.word_to_translate)
        optionsContainer = rootView.findViewById(R.id.options_container)
        nextButton = rootView.findViewById(R.id.next_button)

        // Obtener los datos enviados desde la actividad
        val wordToTranslate = arguments?.getString("wordToTranslate") ?: ""
        correctAnswer = arguments?.getString("correctAnswer") ?: ""
        val options = arguments?.getStringArrayList("options") ?: arrayListOf()

        // Configurar la palabra y opciones
        wordTextView.text = wordToTranslate
        configureOptions(options)

        nextButton.setOnClickListener {
            if (isAnswered) {
                (activity as? TraduceloActivity)?.onNextWord()
            } else {
                Toast.makeText(requireContext(), "Selecciona una respuesta primero", Toast.LENGTH_SHORT).show()
            }
        }

        return rootView
    }

    private fun configureOptions(options: List<String>) {
        optionsContainer.removeAllViews()
        options.forEach { option ->
            val button = createOptionButton(option)
            optionsContainer.addView(button)
        }
    }

    private fun createOptionButton(option: String): Button {
        val button = Button(requireContext()).apply {
            text = option
            setBackgroundResource(R.drawable.rounded_background) // Asignar el fondo redondeado
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
            setOnClickListener { onOptionSelected(this) }
        }

        // Configurar márgenes usando LayoutParams
        val params = FlexboxLayout.LayoutParams(
            FlexboxLayout.LayoutParams.WRAP_CONTENT,
            FlexboxLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(
                dpToPx(0), // Margen superior
                dpToPx(24), // Margen derecho
                dpToPx(0), // Margen inferior
                dpToPx(24)  // Margen izquierdo
            )
        }
        button.layoutParams = params
        return button
    }

    /**
     * Función de utilidad para convertir dp a px.
     */
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }


    private fun onOptionSelected(button: Button) {
        if (isAnswered) return // Evitar interacciones después de responder

        val selectedAnswer = button.text.toString()
        isAnswered = true

        if (selectedAnswer == correctAnswer) {
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_light))
            (activity as? TraduceloActivity)?.incrementScore()
        } else {
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light))

            // Mostrar la respuesta correcta
            optionsContainer.forEach { child ->
                if ((child as? Button)?.text.toString() == correctAnswer) {
                    child.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_light))
                }
            }
        }

        nextButton.visibility = View.VISIBLE // Mostrar el botón de "Siguiente"
    }
}
