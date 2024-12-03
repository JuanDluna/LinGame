package com.example.lingame

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlin.random.Random

class DecisionFragment : Fragment() {

    private var question: String? = null
    private var options: List<String> = listOf()
    private var errorMessage: String? = null
    private var parentActivity: CreaHistoriaActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        parentActivity = context as? CreaHistoriaActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            question = it.getString("question")
            options = it.getStringArrayList("options") ?: listOf()
            errorMessage = it.getString("errorMessage")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_decision, container, false)
        val questionText = view.findViewById<TextView>(R.id.txtQuestion)
        questionText.text = question

        // Lógica de gestos del celular
        setupGestureRecognition(view)

        return view
    }

    private fun setupGestureRecognition(view: View) {
        // Implementar detector de movimientos (por ejemplo, inclinación del celular)
        // y asociar respuestas a movimientos específicos
    }

    fun notifyAnswer(correct: Boolean) {
        parentActivity?.onQuestionAnswered(correct)
    }

    companion object {
        fun newInstance(question: String, options: List<String>, errorMessage: String): DecisionFragment {
            val fragment = DecisionFragment()
            val args = Bundle()
            args.putString("question", question)
            args.putStringArrayList("options", ArrayList(options))
            args.putString("errorMessage", errorMessage)
            fragment.arguments = args
            return fragment
        }
    }
}
