package com.example.guessgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun OnlineGameScreen(roomCode: String, isHost: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isHost) "Вы создали комнату!" else "Вы вошли в комнату",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Код комнаты:",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = roomCode,
            style = MaterialTheme.typography.headlineLarge
        )
        // Здесь можешь добавить остальной UI для онлайн-игры
    }
}