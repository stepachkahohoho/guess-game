package com.example.guessgame

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LeaderboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        val lbList = findViewById<LinearLayout>(R.id.lbList)
        val btnBack = findViewById<Button>(R.id.btnBack)

        val prefs = getSharedPreferences("leaderboard", MODE_PRIVATE)
        val results = prefs.getStringSet("results", emptySet())
            ?.mapNotNull { it.toIntOrNull() }?.sorted() ?: emptyList()

        lbList.removeAllViews()
        if (results.isEmpty()) {
            val tv = TextView(this)
            tv.text = "Нет результатов!"
            tv.textSize = 20f
            lbList.addView(tv)
        } else {
            for ((i, res) in results.withIndex()) {
                val tv = TextView(this)
                tv.text = "${i + 1}) $res попыток"
                tv.textSize = 20f
                lbList.addView(tv)
            }
        }

        btnBack.setOnClickListener { finish() }
    }
}