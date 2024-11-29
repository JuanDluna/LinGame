package com.example.lingame

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat

class QuestionRRFragment : Fragment() {

    // Clase Answer para representar respuestas individuales
    data class Answer(
        val text: String,  // Texto de la respuesta
        val isCorrect: Boolean // Indicador de si es correcta
    ) : Parcelable {
        companion object {
            lateinit var CREATOR: Parcelable.Creator<Answer>
        }

        constructor(parcel: Parcel) : this(
            parcel.readString().toString(),
            parcel.readByte() != 0.toByte()
        ) {
        }

        override fun describeContents(): Int {
            TODO("Not yet implemented")
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            TODO("Not yet implemented")
        }

        object CREATOR : Parcelable.Creator<Answer> {
            override fun createFromParcel(parcel: Parcel): Answer {
                return Answer(parcel)
            }

            override fun newArray(size: Int): Array<Answer?> {
                return arrayOfNulls(size)
            }
        }
    }

    // Clase Question que sigue la estructura de la base de datos
    data class Question(
        val question: Map<String, String>, // Traducciones de la pregunta (es, en, fr, pr)
        val answers: List<Answer> // Lista de respuestas con su estado
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readHashMap(String::class.java.classLoader) as Map<String, String>,
            parcel.createTypedArrayList(Answer.CREATOR) ?: emptyList()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeMap(question)
            parcel.writeTypedList(answers)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<Question> {
            override fun createFromParcel(parcel: Parcel): Question = Question(parcel)
            override fun newArray(size: Int): Array<Question?> = arrayOfNulls(size)
        }

        // Método auxiliar para obtener la pregunta en el idioma seleccionado
        fun getQuestionInLanguage(languageCode: String): String? = question[languageCode]
    }

    companion object {
        private const val ARG_QUESTION = "question"

        // Método para crear una nueva instancia del fragmento con una pregunta
        fun newInstance(question: Question): QuestionRRFragment {
            return QuestionRRFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_QUESTION, question)
                }
            }
        }
    }

    // Variables de instancia
    private lateinit var question: Question
    private lateinit var sharedPreferences : SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_question_rr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences(R.string.sharedPreferencesName.toString(), MODE_PRIVATE)

        question = arguments?.getParcelable(ARG_QUESTION) ?: return

        val questionTextView: TextView = view.findViewById(R.id.questionTextView)
        val answerButtons = listOf<Button>(
            view.findViewById(R.id.answerButton1),
            view.findViewById(R.id.answerButton2),
            view.findViewById(R.id.answerButton3),
            view.findViewById(R.id.answerButton4)
        )

        val selectedLanguage =  sharedPreferences.getString(R.string.selectedLanguagePreferences.toString(), null)// Cambiar esto según la lógica de selección de idioma
        questionTextView.text = question.getQuestionInLanguage(selectedLanguage!!)

        val answers = question.answers
        for ((index, button) in answerButtons.withIndex()) {
            if (index < answers.size) {
                val (answerText, isCorrect) = answers[index]
                button.text = answerText
                button.visibility = View.VISIBLE

                button.setOnClickListener {
                    handleAnswerSelected(button, isCorrect, answerButtons)
                }
            } else {
                button.visibility = View.GONE
            }
        }
    }


    private fun handleAnswerSelected(
        selectedButton: Button,
        isCorrect: Boolean,
        answerButtons: List<Button>
    ) {
        // Crear animación de escalado
        animateButtonScale(selectedButton)

        val correctColor = ContextCompat.getColor(requireContext(), R.color.green)
        val incorrectColor = ContextCompat.getColor(requireContext(), R.color.red)

        // Cambiar el color del botón seleccionado
        selectedButton.backgroundTintList = ColorStateList.valueOf(if (isCorrect) correctColor else incorrectColor)

        if (!isCorrect) {
            // Buscar y resaltar el botón con la respuesta correcta
            val correctButton = answerButtons.firstOrNull { button ->
                question.answers.any { it.text == button.text && it.isCorrect }
            }
            correctButton?.backgroundTintList = ColorStateList.valueOf(correctColor)
        }

        // Desactivar todos los botones
        answerButtons.forEach { it.isEnabled = false }

        // Retardo antes de pasar a la siguiente pregunta
        Handler(Looper.getMainLooper()).postDelayed({
            // Restablecer los colores originales
            answerButtons.forEach { button ->
                button.backgroundTintList = null
                button.isEnabled = true
            }

            // Pasar a la siguiente pregunta
            (activity as? RetoRapidoActivity)?.onQuestionAnswered(isCorrect)
        }, 1500) // 1.5 segundos de retardo
    }

    // Animar el escalado del botón seleccionado
    private fun animateButtonScale(button: Button) {
        val scaleUpX = ObjectAnimator.ofFloat(button, "scaleX", 1.0f, 1.1f)
        val scaleUpY = ObjectAnimator.ofFloat(button, "scaleY", 1.0f, 1.1f)
        val scaleDownX = ObjectAnimator.ofFloat(button, "scaleX", 1.1f, 1.0f)
        val scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", 1.1f, 1.0f)

        val scaleUp = AnimatorSet().apply {
            playTogether(scaleUpX, scaleUpY)
            duration = 150 // Duración de la animación de escalado hacia arriba
        }

        val scaleDown = AnimatorSet().apply {
            playTogether(scaleDownX, scaleDownY)
            duration = 150 // Duración de la animación de escalado hacia abajo
        }

        // Combinar las animaciones en una secuencia
        val scaleAnimation = AnimatorSet().apply {
            playSequentially(scaleUp, scaleDown)
        }

        scaleAnimation.start()
    }


}
