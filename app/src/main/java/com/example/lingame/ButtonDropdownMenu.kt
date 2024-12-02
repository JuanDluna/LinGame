package com.example.lingame

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.LinearLayoutCompat
class ButtonDropdownMenu : AppCompatImageButton {

    private var isDropdownOpen = false
    private var popupWindow: PopupWindow? = null
    private var dropdownContainer: LinearLayout? = null
    private var onOptionClick: ((String) -> Unit)? = null


    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs, 0){
        Log.d("ButtonDropdownMenu", "Constructor con parámetros llamado")
    }

    init {
        try {
            // Configuración inicial del botón
            this.setOnClickListener {
                toggleDropdown()
            }
        } catch (e: Exception) {
            Log.e("ButtonDropdownMenu", "Error al inicializar el botón: ${e.message}")
        }
    }

    fun setDropdownOptions(options: Map<String, Drawable>) {
        try {
            // Crear contenedor de las opciones (Dropdown)
            dropdownContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
            }

            options.forEach { (key, drawable) ->
                val button = ImageView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        50.dpToPx(), // Tamaño fijo
                        50.dpToPx()
                    )
                    setImageDrawable(drawable)
                    contentDescription = key
                    setBackgroundResource(android.R.color.transparent)
                    setOnClickListener {
                        onOptionClick?.invoke(key)
                        popupWindow?.dismiss()
                        isDropdownOpen = false
                    }
                }
                dropdownContainer?.addView(button)
            }
        } catch (e: Exception) {
            Log.e("ButtonDropdownMenu", "Error al configurar las opciones: ${e.message}")
        }
    }

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
                    setBackgroundDrawable(context.getDrawable(R.drawable.dropdown_background))
                    isOutsideTouchable = true
                    showAsDropDown(this@ButtonDropdownMenu, 0, 0, Gravity.BOTTOM)
                }
            }
        }
        isDropdownOpen = !isDropdownOpen
    }

    fun setOnOptionClickListener(listener: (String) -> Unit) {
        onOptionClick = listener
    }

    // Extensión para convertir dp a px
    fun Int.dpToPx(): Int = (this * context.resources.displayMetrics.density).toInt()
}
