package com.example.guessgame

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth
import kotlin.random.Random

class OnlineGameActivity : AppCompatActivity() {
    private lateinit var db: DatabaseReference
    private lateinit var roomCode: String
    private var isHost = false
    private lateinit var playerId: String
    private lateinit var etGuess: EditText
    private lateinit var btnSend: Button
    private lateinit var tvStatus: TextView
    private lateinit var guessesLayout: LinearLayout

    private var numberToGuess: String = ""
    private lateinit var tvRoomCode: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_game)

        etGuess = findViewById(R.id.etGuess) ?: throw RuntimeException("etGuess не найден")
        btnSend = findViewById(R.id.btnSend) ?: throw RuntimeException("btnSend не найден")
        tvStatus = findViewById(R.id.tvStatus) ?: throw RuntimeException("tvStatus не найден")
        guessesLayout = findViewById(R.id.guessesLayout) ?: throw RuntimeException("guessesLayout не найден")
        tvRoomCode = findViewById(R.id.tvRoomCode) ?: throw RuntimeException("tvRoomCode не найден")
        tvRoomCode = findViewById(R.id.tvRoomCode)
        tvRoomCode.text = "Код комнаты: $roomCode"
        etGuess = findViewById(R.id.etGuess)
        btnSend = findViewById(R.id.btnSend)
        tvStatus = findViewById(R.id.tvStatus)
        guessesLayout = findViewById(R.id.guessesLayout)

        db = FirebaseDatabase.getInstance().reference
        roomCode = intent.getStringExtra("roomCode") ?: ""
        isHost = intent.getBooleanExtra("isHost", false)
        playerId = FirebaseAuth.getInstance().uid ?: Random.nextInt(100000,999999).toString()

        if (isHost) {
            numberToGuess = generateNumber()
            db.child("rooms").child(roomCode).setValue(
                mapOf(
                    "host" to playerId,
                    "number" to numberToGuess,
                    "guesses" to mapOf<String, Any>()
                )
            )
            tvStatus.text = "Ожидание игрока..."
            waitForSecondPlayer()
        } else {
            joinRoom()
        }

        btnSend.setOnClickListener {
            val guess = etGuess.text.toString()
            if (!isValidGuess(guess)) {
                Toast.makeText(this, "Введите 4 разные цифры", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            etGuess.text.clear()
            sendGuess(guess)
        }
    }

    private fun waitForSecondPlayer() {
        db.child("rooms").child(roomCode).child("guest").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    tvStatus.text = "Игра началась! Угадывайте число."
                    listenGuesses()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun joinRoom() {
        db.child("rooms").child(roomCode).get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                Toast.makeText(this, "Комната не найдена", Toast.LENGTH_SHORT).show()
                finish()
                return@addOnSuccessListener
            }
            db.child("rooms").child(roomCode).child("guest").setValue(playerId)
            numberToGuess = snapshot.child("number").value as? String ?: ""
            tvStatus.text = "Игра началась! Угадывайте число."
            listenGuesses()
        }
    }

    private fun sendGuess(guess: String) {
        val guessEntry = mapOf(
            "player" to playerId,
            "guess" to guess
        )
        db.child("rooms").child(roomCode).child("guesses").push().setValue(guessEntry)
    }

    private fun listenGuesses() {
        db.child("rooms").child(roomCode).child("guesses").addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val guessEntry = snapshot.value as? Map<*, *> ?: return
                val player = guessEntry["player"] as? String ?: ""
                val guess = guessEntry["guess"] as? String ?: ""
                val colors = getColors(guess, numberToGuess)
                addGuessRow(guess, colors, player == playerId)
                if (colors.all { it == ColorResult.GREEN }) {
                    tvStatus.text = if (player == playerId) "Вы победили!" else "Ваш соперник победил!"
                    btnSend.isEnabled = false
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun isValidGuess(guess: String): Boolean =
        guess.length == 4 && guess.all { it.isDigit() } && guess.toSet().size == 4

    private fun addGuessRow(guess: String, colors: List<ColorResult>, mine: Boolean) {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        for (i in 0 until 4) {
            val iv = ImageView(this)
            iv.layoutParams = LinearLayout.LayoutParams(72, 72)
            iv.setImageResource(
                when (colors[i]) {
                    ColorResult.GREEN -> R.drawable.ic_green
                    ColorResult.YELLOW -> R.drawable.ic_yellow
                    ColorResult.RED -> R.drawable.ic_red
                }
            )
            row.addView(iv)
        }
        val tv = TextView(this)
        tv.text = "  $guess${if (mine) " (вы)" else " (соперник)"}"
        tv.textSize = 18f
        row.addView(tv)
        guessesLayout.addView(row)
    }

    private fun getColors(guess: String, answer: String): List<ColorResult> {
        val colors = MutableList(4) { ColorResult.RED }
        val guessFlags = BooleanArray(4)
        val answerFlags = BooleanArray(4)
        for (i in 0 until 4) {
            if (guess[i] == answer[i]) {
                colors[i] = ColorResult.GREEN
                guessFlags[i] = true
                answerFlags[i] = true
            }
        }
        for (i in 0 until 4) {
            if (!guessFlags[i]) {
                for (j in 0 until 4) {
                    if (!answerFlags[j] && guess[i] == answer[j]) {
                        colors[i] = ColorResult.YELLOW
                        answerFlags[j] = true
                        break
                    }
                }
            }
        }
        return colors
    }

    private fun generateNumber(): String {
        val digits = mutableListOf<Int>()
        while (digits.size < 4) {
            val d = Random.nextInt(if (digits.isEmpty()) 1 else 0, 10)
            if (d !in digits) digits.add(d)
        }
        return digits.joinToString("")
    }
}

enum class ColorResult { GREEN, YELLOW, RED }




