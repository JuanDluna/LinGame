package com.example.lingame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class TutorialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        val platformControl1 = findViewById<PlatformControl>(R.id.platformControl)
        //TODO Implementar actividad de tutorial

        platformControl1.targetActivity = GameLogicaActivity::class.java
    }
}