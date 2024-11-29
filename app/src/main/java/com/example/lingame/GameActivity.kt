package com.example.lingame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment

class GameActivity : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout del fragmento
        val view = inflater.inflate(R.layout.activity_game, container, false)

        // Mostrar las plataformas iniciales
        showPlatforms(view, 1)  // Set 1 de plataformas

        return view
    }

    private fun showPlatforms(view: View, setNumber: Int) {
        val constraintLayout = view.findViewById<ConstraintLayout>(R.id.constraintLayout)

        // Crear y configurar las plataformas
        val northPlatform = createPlatform(View.generateViewId(), "norte")
        val southPlatform = createPlatform(View.generateViewId(), "sur")
        val eastPlatform = createPlatform(View.generateViewId(), "este")
        val westPlatform = createPlatform(View.generateViewId(), "oeste")

        // Añadir las plataformas al ConstraintLayout
        constraintLayout.addView(northPlatform)
        constraintLayout.addView(southPlatform)
        constraintLayout.addView(eastPlatform)
        constraintLayout.addView(westPlatform)

        // Configurar las restricciones para las plataformas
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        // Id de la plataforma central (en este caso se asume que ya existe en el layout con id centralPlatform)
        val centralPlatformId = view.findViewById<View>(R.id.centralPlatform).id

        // Posicionar las plataformas alrededor de la plataforma central
        constraintSet.connect(northPlatform.id, ConstraintSet.BOTTOM, centralPlatformId, ConstraintSet.TOP)
        constraintSet.connect(northPlatform.id, ConstraintSet.START, centralPlatformId, ConstraintSet.START)
        constraintSet.connect(northPlatform.id, ConstraintSet.END, centralPlatformId, ConstraintSet.END)

        constraintSet.connect(southPlatform.id, ConstraintSet.TOP, centralPlatformId, ConstraintSet.BOTTOM)
        constraintSet.connect(southPlatform.id, ConstraintSet.START, centralPlatformId, ConstraintSet.START)
        constraintSet.connect(southPlatform.id, ConstraintSet.END, centralPlatformId, ConstraintSet.END)

        constraintSet.connect(eastPlatform.id, ConstraintSet.START, centralPlatformId, ConstraintSet.END)
        constraintSet.connect(eastPlatform.id, ConstraintSet.TOP, centralPlatformId, ConstraintSet.TOP)
        constraintSet.connect(eastPlatform.id, ConstraintSet.BOTTOM, centralPlatformId, ConstraintSet.BOTTOM)

        constraintSet.connect(westPlatform.id, ConstraintSet.END, centralPlatformId, ConstraintSet.START)
        constraintSet.connect(westPlatform.id, ConstraintSet.TOP, centralPlatformId, ConstraintSet.TOP)
        constraintSet.connect(westPlatform.id, ConstraintSet.BOTTOM, centralPlatformId, ConstraintSet.BOTTOM)

        // Aplicar las restricciones
        constraintSet.applyTo(constraintLayout)
    }

    private fun createPlatform(id: Int, category: String): PlatformControl {
        val platform = PlatformControl(requireContext()).apply {
            this.id = id
            this.category = category  // Asignar la categoría a la plataforma (norte, sur, este, oeste)
        }
        val params = ConstraintLayout.LayoutParams(250, 250)
        platform.layoutParams = params
        return platform
    }
}
