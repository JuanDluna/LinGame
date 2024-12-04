package com.example.lingame

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.ImageView

class DecisionFragment : Fragment(), SensorEventListener {

    data class QuestionData(
        val level: Int,
        val question: String,
        val answers: Map<String, Boolean>,
        val wrongAnswerMessage: String
    )

    private var questionData: QuestionData? = null
    private var parentActivity: CreaHistoriaActivity? = null
    private lateinit var sensorManager: SensorManager
    private var hasNotifiedSelection = false  // Variable para controlar la notificación
    private var accelerometer: Sensor? = null

    private lateinit var characterImage : ImageView
    private lateinit var questionText: TextView
    private lateinit var answer1: TextView
    private lateinit var answer2: TextView
    private var textOptionSelected: String = ""

    companion object {
        fun newInstance( level: Int,question: String, options: Map<String, Boolean>, errorMessage: String): DecisionFragment {
            val fragment = DecisionFragment()
            val args = Bundle()
            args.putInt("level", level)
            args.putString("question", question)
            args.putStringArrayList("optionKeys", ArrayList(options.keys))
            args.putBooleanArray("optionValues", options.values.toBooleanArray())
            args.putString("errorMessage", errorMessage)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        parentActivity = context as? CreaHistoriaActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val level = it.getInt("level")
            val question = it.getString("question")
            val optionKeys = it.getStringArrayList("optionKeys")
            val optionValues = it.getBooleanArray("optionValues")
            val options = if (optionKeys != null && optionValues != null) {
                optionKeys.zip(optionValues.toList()).toMap()
            } else {
                mapOf()
            }
            val errorMessage = it.getString("errorMessage")
            questionData = QuestionData(level,question ?: "", options, errorMessage ?: "")
        }


        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_decision, container, false)

        characterImage = view.findViewById(R.id.ivCharacterCH)
        questionText = view.findViewById(R.id.tvQuestion)
        answer1 = view.findViewById(R.id.tvAnswer1)
        answer2 = view.findViewById(R.id.tvAnswer2)

        questionText.text = questionData!!.question

        when (questionData!!.level) {
            1 -> {
                characterImage.setImageResource(R.drawable.story_creation_1_happygirl)
            }
            2 -> {
                characterImage.setImageResource(R.drawable.story_creation_2_happyfriend)
                }
            3 -> {
                characterImage.setImageResource(R.drawable.story_creation_3_happyboss)
            }
        }

        val shuffledAnswers = questionData!!.answers.keys.shuffled()
        answer1.text = shuffledAnswers[0]
        answer2.text = shuffledAnswers[1]

        return view
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { acc ->
            sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private var lastUpdateTime = 0L  // Almacena el tiempo de la última detección

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastUpdateTime > 10 && !hasNotifiedSelection) {
                lastUpdateTime = currentTime  // Actualizamos el tiempo de la última detección

                val x = event.values[0]  // Eje X: izquierda (-) o derecha (+)
                val y = event.values[1]  // Eje Y: arriba (+) o abajo (-)

                val angleX = Math.abs(Math.atan2(y.toDouble(), x.toDouble()) * (180 / Math.PI))

                when {
                    angleX <= 85.0 -> {  // Lado izquierdo (selección completa)
                        val normalizedAngle = (angleX - 85) * -1 / 30
                        val scaleFactor = 1.0f + 0.5f * smoothStep(0f, 1f, normalizedAngle.toFloat())
                        if (scaleFactor >= 1.5f && textOptionSelected != answer1.text.toString()) {
                            textOptionSelected = answer1.text.toString()
                            sensorManager.unregisterListener(this) // Desactivamos el sensor

                            vibrateAnswer(answer1) {
                                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
                                handleAnswerSelection(answer1.text.toString()) // Notificar al padre
                            }
                        }
                        scaleAnswer(answer1, scaleFactor)
                        selectAnswer(answer1)
                        deselectAnswer(answer2)
                    }
                    angleX >= 100.0 -> {  // Lado derecho (selección completa)
                        val normalizedAngle = (angleX - 100) / 30
                        val scaleFactor = 1.0f + 0.5f * smoothStep(0f, 1f, normalizedAngle.toFloat())
                        if (scaleFactor >= 1.5f && textOptionSelected != answer2.text.toString()) {
                            textOptionSelected = answer2.text.toString()
                            sensorManager.unregisterListener(this) // Desactivamos el sensor

                            vibrateAnswer(answer2) {
                                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
                                handleAnswerSelection(answer2.text.toString()) // Notificar al padre
                            }
                        }
                        scaleAnswer(answer2, scaleFactor)
                        selectAnswer(answer2)
                        deselectAnswer(answer1)
                    }
                    else -> {
                        deselectAnswer(answer1)
                        deselectAnswer(answer2)
                    }
                }
            }
        }
    }


    // Función para suavizar el escalado
    private fun smoothStep(edge0: Float, edge1: Float, x: Float): Float {
        val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)  // Normaliza x entre 0 y 1
        return t * t * (3 - 2 * t)  // Suaviza usando la función de interpolación cúbica
    }

    private fun scaleAnswer(answerTextView: TextView, scaleFactor: Float) {
        answerTextView.scaleX = scaleFactor  // Escalar en el eje X
        answerTextView.scaleY = scaleFactor  // Escalar en el eje Y
    }

    private fun selectAnswer(answerTextView: TextView) {
        answerTextView.setBackgroundResource(R.drawable.rounded_background)
        answerTextView.setBackgroundColor(Color.parseColor("#32CD32"))  // Resaltar con verde
        answerTextView.setTextColor(Color.WHITE)  // Cambiar color de texto a blanco
    }

    private fun deselectAnswer(answerTextView: TextView) {
        answerTextView.setBackgroundResource(R.drawable.rounded_background)
        answerTextView.setTextColor(Color.BLACK)
        answerTextView.scaleX = 1.0f  // Volver al tamaño original en el eje X
        answerTextView.scaleY = 1.0f  // Volver al tamaño original en el eje Y
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No se necesita implementación
    }

    private fun handleAnswerSelection(selectedAnswer: String) {
        val isCorrect = questionData!!.answers[selectedAnswer] ?: false

        // Notificar a la actividad principal
        parentActivity?.onQuestionAnswered(isCorrect)
    }

    private fun vibrateAnswer(answerTextView: TextView, onAnimationEnd: () -> Unit) {
        // Crear la animación de sacudida
        val animator = ObjectAnimator.ofFloat(
            answerTextView,
            "translationX",
            0f, 20f, -20f, 15f, -15f, 10f, -10f, 5f, -5f, 0f
        )
        animator.duration = 2000 // Duración de la animación en milisegundos

        // Listener para reactivar el sensor después de la animación
        animator.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                onAnimationEnd()
            }
        })

        animator.start()
    }

}
