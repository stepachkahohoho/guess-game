package com.example.guessgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

class OnlineGameActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val roomCode = intent.getStringExtra("roomCode") ?: ""
        setContent {
            OnlineGameScreen(roomCode = roomCode)
        }
    }
}