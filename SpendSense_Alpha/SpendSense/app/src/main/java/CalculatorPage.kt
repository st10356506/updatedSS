package com.example.spendsense20

import android.os.Bundle
import android.widget.* // Android Developers (n.d.). Android widget classes. Available at: https://developer.android.com/reference/android/widget/package-summary
import androidx.appcompat.app.AppCompatActivity // Android Developers (n.d.). AppCompatActivity reference. Available at: https://developer.android.com/reference/androidx/appcompat/app/AppCompatActivity

class CalculatorPage : AppCompatActivity() {
//initialize views
    private lateinit var display: TextView // Android Developers (n.d.). TextView. Available at: https://developer.android.com/reference/android/widget/TextView
    private var currentInput = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Android Developers (n.d.). Activity Lifecycle. Available at: https://developer.android.com/guide/components/activities/activity-lifecycle
        setContentView(R.layout.calculator_page) // Android Developers (n.d.). Layouts and Views. Available at: https://developer.android.com/guide/topics/ui/declaring-layout

        display = findViewById(R.id.calculatorDisplay) // Android Developers (n.d.). findViewById. Available at: https://developer.android.com/reference/android/view/View#findViewById(int)
//back button
        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener { finish() } // Android Developers (n.d.). finish(). Available at: https://developer.android.com/reference/android/app/Activity#finish()

        val numberButtons = listOf(R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnDot) // Kotlin Documentation (n.d.). List literals. Available at: https://kotlinlang.org/docs/collections-overview.html
//number button logic
        numberButtons.forEach { id ->
            findViewById<Button>(id).setOnClickListener {
                currentInput += (it as Button).text // Kotlin (n.d.). Smart casts. Available at: https://kotlinlang.org/docs/typecasts.html#smart-casts
                display.text = currentInput
            }
        }
//operator buttons logic
        val operatorButtons = listOf(R.id.btnAdd, R.id.btnSub, R.id.btnMul, R.id.btnDiv)
        operatorButtons.forEach { id ->
            findViewById<Button>(id).setOnClickListener {
                currentInput += " ${(it as Button).text} "
                display.text = currentInput
            }
        }
//clear output
        findViewById<Button>(R.id.btnClear).setOnClickListener {
            currentInput = ""
            display.text = "0"
        }
//equals button logic
        findViewById<Button>(R.id.btnEquals).setOnClickListener {
            try {
                val result = eval(currentInput) // Based on shunting-yard expression evaluation, inspired by: Dijkstra, E.W. (1961).
                display.text = result.toString()
                currentInput = result.toString()
            } catch (e: Exception) {
                display.text = "Error"
            }
        }
    }
//operator logic
    private fun eval(expr: String): Double { // Simple recursive descent parser. Inspired by: https://stackoverflow.com/a/26227947
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() { ch = if (++pos < expr.length) expr[pos].code else -1 }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                return if (ch == charToEat) { nextChar(); true } else false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expr.length) throw RuntimeException("Unexpected: " + expr[pos])
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    x = when {
                        eat('+'.code) -> x + parseTerm()
                        eat('-'.code) -> x - parseTerm()
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    x = when {
                        eat('*'.code) -> x * parseFactor()
                        eat('/'.code) -> x / parseFactor()
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()

                val startPos = pos
                val x: Double = when {
                    eat('('.code) -> {
                        val result = parseExpression()
                        eat(')'.code)
                        result
                    }
                    ch in '0'.code..'9'.code || ch == '.'.code -> {
                        while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                        expr.substring(startPos, pos).toDouble()
                    }
                    else -> throw RuntimeException("Unexpected character: ${ch.toChar()}")
                }
                return x
            }
        }.parse()
    }
}
