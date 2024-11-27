package com.example.lingame

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.TextView

class QuestionRRFragment : Fragment() {
    // Clase "Question" para el manejo de preguntas y respuestas haciendo traspaso entre elementos.
    data class Question(val question: String = "", val answers: Map<String, Boolean> = mapOf() ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readHashMap(String::class.java.classLoader) as Map<String, Boolean>
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(question)
            parcel.writeMap(answers)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<Question> {
            override fun createFromParcel(parcel: Parcel): Question {
                return Question(parcel)
            }

            override fun newArray(size: Int): Array<Question?> {
                return arrayOfNulls(size)
            }
        }
    }

    companion object {
        private const val ARG_QUESTION = "question"

        fun newInstance(question: Question): QuestionRRFragment {
            val fragment = QuestionRRFragment()
            val args = Bundle()
            args.putParcelable(ARG_QUESTION, question)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var question: Question

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_question_rr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener la pregunta desde los argumentos
        question = arguments?.getParcelable(ARG_QUESTION) ?: return

        val questionText = view.findViewById<TextView>(R.id.question_text)
        val answersContainer = view.findViewById<ViewGroup>(R.id.answers_container)

        questionText.text = question.text

        // Mostrar las respuestas
        question.answers.forEach { answer ->
            val button = Button(context).apply {
                text = answer.text
                setOnClickListener {
                    val isCorrect = answer.isCorrect
                    (activity as? RetoRapidoActivity)?.onQuestionAnswered(isCorrect)
                }
            }
            answersContainer.addView(button)
        }
    }
}
