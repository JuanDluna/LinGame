package com.example.lingame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class TutorialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        val platformControl1 = findViewById<PlatformControl>(R.id.platformControl)
        platformControl1.targetActivity = LoginActivity::class.java
    }
}