package com.example.guessgame

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.random.Random

class SingleGameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SingleGameScreen()
        }
    }
}

data class LeaderboardEntry(val name: String, val attempts: Int)

@Composable
fun SingleGameScreen() {
    val context = LocalContext.current
    val numberLength = 4
    var secretNumber by remember { mutableStateOf(generateNumberWithRepeats(numberLength)) }
    var input by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    var attempts by remember { mutableStateOf(0) }
    var showWinDialog by remember { mutableStateOf(false) }
    var playerName by remember { mutableStateOf("") }
    var leaderboard by remember { mutableStateOf(loadLeaderboard(context)) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Отгадай число из $numberLength цифр (могут повторяться)", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = input,
                onValueChange = { if (it.length <= numberLength) input = it },
                label = { Text("Введите число") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                if (input.length != numberLength) {
                    feedback = "Введите $numberLength цифр!"
                    return@Button
                }
                attempts++
                if (input == secretNumber) {
                    feedback = "Поздравляем! Ты угадал за $attempts попыток!"
                    showWinDialog = true
                } else {
                    feedback = "Не угадал! Быки: ${countBulls(secretNumber, input)}, Коровы: ${countCows(secretNumber, input)}"
                }
            }) {
                Text("Проверить")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(feedback)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                secretNumber = generateNumberWithRepeats(numberLength)
                input = ""
                feedback = ""
                attempts = 0
            }) {
                Text("Заново")
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text("Лидерборд", style = MaterialTheme.typography.titleMedium)
            LeaderboardList(leaderboard)
        }

        if (showWinDialog) {
            AlertDialog(
                onDismissRequest = { showWinDialog = false },
                title = { Text("Победа!") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Введите своё имя для таблицы лидеров:")
                        OutlinedTextField(
                            value = playerName,
                            onValueChange = { playerName = it },
                            label = { Text("Имя") }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showWinDialog = false
                        val entry = LeaderboardEntry(playerName.ifBlank { "Игрок" }, attempts)
                        leaderboard = leaderboard.plus(entry).sortedBy { it.attempts }.take(10)
                        saveLeaderboard(context, leaderboard)
                        // Сброс игры
                        secretNumber = generateNumberWithRepeats(numberLength)
                        input = ""
                        feedback = ""
                        attempts = 0
                        playerName = ""
                    }) {
                        Text("Сохранить")
                    }
                }
            )
        }
    }
}

// Генератор числа с любыми цифрами
fun generateNumberWithRepeats(length: Int): String =
    (1..length).map { Random.nextInt(0, 10) }.joinToString("")

fun countBulls(secret: String, guess: String): Int =
    secret.zip(guess).count { (s, g) -> s == g }

fun countCows(secret: String, guess: String): Int {
    val bulls = secret.zip(guess).map { (s, g) -> s == g }
    val secretFiltered = secret.filterIndexed { i, _ -> !bulls[i] }
    val guessFiltered = guess.filterIndexed { i, _ -> !bulls[i] }
    val secretCounts = secretFiltered.groupingBy { it }.eachCount().toMutableMap()
    var cows = 0
    for (g in guessFiltered) {
        if ((secretCounts[g] ?: 0) > 0) {
            cows++
            secretCounts[g] = secretCounts[g]!! - 1
        }
    }
    return cows
}

// ===== Лидерборд (SharedPreferences в JSON) =====

fun loadLeaderboard(context: Context): List<LeaderboardEntry> {
    val prefs = context.getSharedPreferences("leaderboard", Context.MODE_PRIVATE)
    val json = prefs.getString("data", null)
    return if (json != null) {
        Gson().fromJson(json, object : TypeToken<List<LeaderboardEntry>>() {}.type)
    } else {
        emptyList()
    }
}

fun saveLeaderboard(context: Context, leaderboard: List<LeaderboardEntry>) {
    val prefs = context.getSharedPreferences("leaderboard", Context.MODE_PRIVATE)
    val json = Gson().toJson(leaderboard)
    prefs.edit().putString("data", json).apply()
}

// ===== Компонент для вывода лидерборда =====
@Composable
fun LeaderboardList(entries: List<LeaderboardEntry>) {
    if (entries.isEmpty()) {
        Text("Пока нет результатов")
    } else {
        LazyColumn(
            modifier = Modifier.heightIn(max = 200.dp),
            userScrollEnabled = false
        ) {
            items(entries) { entry ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(entry.name)
                    Text("${entry.attempts} попыток")
                }
                Divider()
            }
        }
    }
}