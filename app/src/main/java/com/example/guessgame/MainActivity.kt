package com.example.guessgame

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MainScreen() }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var codeInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                val roomCode = generateRoomCode()
                val intent = Intent(context, OnlineGameActivity::class.java)
                intent.putExtra("roomCode", roomCode)
                intent.putExtra("isHost", true)
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) { Text("Создать комнату") }

        Button(
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) { Text("Войти в комнату") }

        Button(
            onClick = {
                val intent = Intent(context, SingleGameActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) { Text("Обычный режим") }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Вход в комнату") },
            text = {
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { codeInput = it },
                    label = { Text("Код комнаты") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    if (codeInput.isNotBlank()) {
                        val intent = Intent(context, OnlineGameActivity::class.java)
                        intent.putExtra("roomCode", codeInput)
                        intent.putExtra("isHost", false)
                        context.startActivity(intent)
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) { Text("Отмена") }
            }
        )
    }
}

fun generateRoomCode(): String {
    val chars = ('A'..'Z') + ('0'..'9')
    return (1..6).map { chars.random() }.joinToString("")
}