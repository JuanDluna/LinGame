package com.example.lingame

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.LinearLayoutCompat

class ButtonDropdownMenu(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageButton(context, attrs, defStyleAttr) {

    private var isDropdownOpen = false
    private var popupWindow: PopupWindow? = null
    private var dropdownContainer: LinearLayout? = null
    private var onOptionClick: ((String) -> Unit)? = null

    init {
        // Configuración inicial del botón
        this.setOnClickListener {
            toggleDropdown()
        }
    }

    /**
     * Configura las opciones del dropdown.
     * @param options Mapa de opciones con un nombre y un drawable asociado.
     */
    fun setDropdownOptions(options: Map<String, Drawable>) {
        // Crear contenedor de las opciones (Dropdown)
        dropdownContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        options.forEach { (key, drawable) ->
            val button = ImageButton(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setImageDrawable(drawable)
                contentDescription = key
                setBackgroundResource(android.R.color.transparent)
                setOnClickListener {
                    // Invoca el callback registrado para la opción seleccionada
                    onOptionClick?.invoke(key)
                    popupWindow?.dismiss()
                    isDropdownOpen = false
                }
            }
            dropdownContainer?.addView(button)
        }
    }

    /**
     * Abre o cierra el menú desplegable.
     */
    private fun toggleDropdown() {
        if (isDropdownOpen) {
            popupWindow?.dismiss()
        } else {
            if (dropdownContainer != null) {
                popupWindow = PopupWindow(
                    dropdownContainer,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                    true
                ).apply {
                    elevation = 10f
                    showAsDropDown(this@ButtonDropdownMenu, 0, 0, Gravity.BOTTOM)
                }
            }
        }
        isDropdownOpen = !isDropdownOpen
    }

    /**
     * Establece un listener para manejar los clics en las opciones.
     * @param listener Callback que se invocará cuando una opción sea seleccionada.
     */
    fun setOnOptionClickListener(listener: (String) -> Unit) {
        onOptionClick = listener
    }
}
