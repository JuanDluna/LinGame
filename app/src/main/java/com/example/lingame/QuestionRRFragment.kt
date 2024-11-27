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

    data class Question(
        val answers: List<String> = emptyList(),  // Lista de respuestas posibles
        val question: Map<String, String> = emptyMap()  // Mapa con las traducciones de la pregunta
    ) : Parcelable {

        constructor(parcel: Parcel) : this(
            parcel.createStringArrayList() ?: emptyList(),
            parcel.readHashMap(String::class.java.classLoader) as Map<String, String>
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeStringList(answers)
            parcel.writeMap(question)
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

    }
}
