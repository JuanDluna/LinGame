package com.example.lingame

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.example.lingame.PhraseFragment.ParafraseaViewModel

class ParafraseaActivity : FragmentActivity() {

    private lateinit var viewModel: ParafraseaViewModel
    private lateinit var firebaseRTDB: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parafrasea)

        // Inicializar referencia de Firebase
        firebaseRTDB = FirebaseDatabase.getInstance().reference.child("languages").child("phrases")

        // Inicializar ViewModel
        viewModel = ViewModelProvider(this).get(ParafraseaViewModel::class.java)

        // Observar cambios en las frases
        viewModel.phrases.observe(this, Observer { phrases ->
            if (phrases.isNotEmpty()) {
                // Cargar el primer fragmento cuando las frases estén listas
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, PhraseFragment.newInstance(0))
                    .commit()
            }
        })

        // Obtener las frases desde Firebase
        fetchPhrasesFromFirebase()
    }

    private fun fetchPhrasesFromFirebase() {
        firebaseRTDB.get().addOnSuccessListener { dataSnapshot ->
            val phrases = mutableListOf<String>()

            dataSnapshot.children.forEach { phraseSnapshot ->
                val phrase = phraseSnapshot.value as? String
                if (phrase != null) {
                    phrases.add(phrase)
                }
            }

            // Mezclar frases y tomar las primeras 5
            phrases.shuffle()
            viewModel.setPhrases(phrases.take(5))
        }.addOnFailureListener {
            Toast.makeText(this, "Error al cargar las frases", Toast.LENGTH_SHORT).show()
        }
    }

    // Método para cargar el siguiente fragmento
    fun loadNextFragment(index: Int) {
        val phrases = viewModel.phrases.value ?: return
        if (index < phrases.size) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PhraseFragment.newInstance(index))
                .commit()
        } else {
            finishGame()
        }
    }

    private fun finishGame() {
        // Mostrar una pantalla de resumen o victoria
        Toast.makeText(this, "¡Juego terminado!", Toast.LENGTH_SHORT).show()
    }
}
