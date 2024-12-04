package com.example.lingame

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class CategoryInfoDialog : DialogFragment() {

    private lateinit var ivCloseOverlayInfo: ImageView
    private lateinit var txtOverlayTitle: TextView
    private lateinit var btnCreaTuHistoria: LinearLayout
    private lateinit var btnRetoRapido: LinearLayout
    private lateinit var btnTraducelo: LinearLayout
    private lateinit var btnParafrasea: LinearLayout

    private lateinit var txtCategory: TextView
    private lateinit var txtDescription: TextView
    private lateinit var ivCloseCategory: ImageView

    private val categories: List<Pair<String, String>> = listOf(
        "Crea tu historia" to "¡Explora conversaciones fascinantes en el idioma que estás aprendiendo! Elige las mejores respuestas usando tu dispositivo, mejora tu habilidad para tomar decisiones y diviértete mientras aprendes",
        "Reto rápido" to "¡Es un reto rápido! Responde lo más rápido que puedas a las preguntas. ¡Gana puntos y demuestra lo mucho que sabes, pero cuidado que si respondes mal, pierdes tiempo!. ¡Vamos, tú puedes!",
        "Tradúcelo" to "¡Es hora de aprender nuevas palabras! Mira la palabra que aparece y elige la traducción correcta. ¡No te preocupes, es muy fácil!",
        "Para-frasea" to "Aquí vas a formar oraciones. Selecciona las palabras en el orden correcto para hacer una frase. ¡Verás que es muy divertido aprender a hablar en otro idioma!"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla el diseño principal del Dialog
        return inflater.inflate(R.layout.overlay_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa las vistas principales
        ivCloseOverlayInfo = view.findViewById(R.id.ivCloseOverlayInfo)
        txtOverlayTitle = view.findViewById(R.id.txtOverlayTitle)
        btnCreaTuHistoria = view.findViewById(R.id.btnCreateStoryOV)
        btnRetoRapido = view.findViewById(R.id.btnRetoRapidoOV)
        btnTraducelo = view.findViewById(R.id.btnTraduceloOV)
        btnParafrasea = view.findViewById(R.id.btnParafraseaOV)

        // Configura el botón para cerrar el Dialog
        ivCloseOverlayInfo.setOnClickListener {
            dismiss() // Cierra el diálogo
        }

        // Configura los botones de las categorías
        btnCreaTuHistoria.setOnClickListener {
            showCategoryInfo(0)
        }

        btnRetoRapido.setOnClickListener {
            showCategoryInfo(1)
        }

        btnTraducelo.setOnClickListener {
            showCategoryInfo(2)
        }

        btnParafrasea.setOnClickListener {
            showCategoryInfo(3)
        }
    }

    private fun showCategoryInfo(index: Int) {
        // Infla el diseño secundario
        val dialogView = layoutInflater.inflate(R.layout.overlay_category_info, null)

        val dialog = Dialog(requireContext()).apply {
            setContentView(dialogView)
            setCancelable(true)
        }

        // Inicializa las vistas de la categoría
        txtCategory = dialogView.findViewById(R.id.txtCategoryTitle)
        txtDescription = dialogView.findViewById(R.id.txtCategoryDescription)
        ivCloseCategory = dialogView.findViewById(R.id.ivCloseCategory)

        // Configura la información de la categoría
        txtCategory.text = categories[index].first
        txtDescription.text = categories[index].second

        // Configura el botón para cerrar el diálogo de categoría
        ivCloseCategory.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
