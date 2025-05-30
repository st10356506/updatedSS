package com.example.spendsense20

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class CalculatorPage : AppCompatActivity() {

    private lateinit var display: TextView
    private var currentInput = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calculator_page)

        display = findViewById(R.id.calculatorDisplay)

        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        // Number buttons
        val numberButtons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3,
            R.id.btn4, R.id.btn5, R.id.btn6,
            R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnDot
        )

        numberButtons.forEach { id ->
            findViewById<Button>(id).setOnClickListener {
                currentInput += (it as Button).text
                display.text = currentInput
            }
        }

        // Operator buttons
        val operatorButtons = listOf(R.id.btnAdd, R.id.btnSub, R.id.btnMul, R.id.btnDiv)
        operatorButtons.forEach { id ->
            findViewById<Button>(id).setOnClickListener {
                currentInput += " ${(it as Button).text} "
                display.text = currentInput
            }
        }

        // Clear button
        findViewById<Button>(R.id.btnClear).setOnClickListener {
            currentInput = ""
            display.text = "0"
        }

        // Equals button
        findViewById<Button>(R.id.btnEquals).setOnClickListener {
            try {
                val result = eval(currentInput)
                display.text = result.toString()
                currentInput = result.toString()
            } catch (e: Exception) {
                display.text = "Error"
            }
        }
    }

    // Native expression evaluator
    private fun eval(expr: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expr.length) expr[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
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

                var x: Double
                val startPos = pos
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch in '0'.code..'9'.code || ch == '.'.code) {
                    while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                    x = expr.substring(startPos, pos).toDouble()
                } else {
                    throw RuntimeException("Unexpected character: ${ch.toChar()}")
                }

                return x
            }
        }.parse()
    }
}