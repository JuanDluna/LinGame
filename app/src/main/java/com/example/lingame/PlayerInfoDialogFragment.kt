package com.example.lingame

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import java.io.File
import kotlin.math.roundToInt

class PlayerInfoDialogFragment : DialogFragment() {

    private lateinit var dbHelper: DBSQLite
    private lateinit var preferences: SharedPreferences

    private var playerName = ""
    private var levelProgress = 0F
    private var currentLevel = 0
    private var photoProfilePath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = requireContext().getSharedPreferences(
            getString(R.string.sharedPreferencesName),
            Context.MODE_PRIVATE
        )
        dbHelper = DBSQLite(requireContext())
        val UID = preferences.getString(getString(R.string.UID_Preferences), null)
        val cursorDB = dbHelper.getUserData(UID!!)

        if (cursorDB != null && cursorDB.moveToFirst()) {
            try {
                playerName = cursorDB.getString(cursorDB.getColumnIndexOrThrow(DBSQLite.COLUMN_NAME))
                levelProgress = cursorDB.getFloat(cursorDB.getColumnIndexOrThrow(DBSQLite.COLUMN_GENERAL_LEVEL))
                currentLevel = levelProgress.roundToInt()
                levelProgress = (levelProgress % 1) * 100
                photoProfilePath = cursorDB.getString(cursorDB.getColumnIndexOrThrow(DBSQLite.COLUMN_PHOTO_URL))

                Log.i("PlayerInfoDialogFragment", "Nombre del jugador: $playerName")
                Log.i("PlayerInfoDialogFragment", "Nivel del jugador: $levelProgress")
                Log.i("PlayerInfoDialogFragment", "Nivel actual del jugador: $currentLevel")
                Log.i("PlayerInfoDialogFragment", "Foto de perfil del jugador: $photoProfilePath")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_player_info, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_player_info, null)

        val ivProfile = view.findViewById<ImageView>(R.id.ivProfileFragmentPI)
        val tvName = view.findViewById<TextView>(R.id.tvNameFragmentPI)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBarFragmentPI)
        val tvProgress = view.findViewById<TextView>(R.id.tvProgressFragmentPI)
        val ivClose = view.findViewById<ImageView>(R.id.ivCloseFragmentPI)

        // Configurar la imagen del perfil después de inflar la vista
        if (photoProfilePath.isNotEmpty()) {
            val photoFile = File(photoProfilePath)
            if (photoFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                ivProfile.setImageBitmap(bitmap)
            } else {
                Log.e("PlayerInfoDialogFragment", "El archivo de foto no existe: $photoProfilePath")
            }
        } else {
            Log.e("PlayerInfoDialogFragment", "photoProfilePath está vacío")
        }

        // Configurar otros elementos de la vista
        tvName.text = playerName
        progressBar.progress = levelProgress.toInt()
        tvProgress.text = "Nivel $currentLevel - ${levelProgress.toInt()}% completado"

        ivClose.setOnClickListener { this.dismiss() }

        builder.setView(view)
        return builder.create()
    }
}
