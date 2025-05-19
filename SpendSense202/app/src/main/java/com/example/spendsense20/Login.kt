package com.example.spendsense20

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.spendsense20.data.AppDatabase
import com.example.spendsense20.data.UserDao
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Login : AppCompatActivity() {
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var backToRegisterText: TextView
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_login)

        // Bind views
        usernameEditText = findViewById(R.id.etUsername)
        passwordEditText = findViewById(R.id.etPassword)
        loginButton = findViewById(R.id.btnLogin)
        backToRegisterText = findViewById(R.id.tvbackToReg)

        // Initialize DAO
        val db = AppDatabase.getDatabase(this)
        userDao = db.userDao()

        // Handle login button click
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(username, password)
            }
        }

        // Navigate to Register screen
        backToRegisterText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun loginUser(username: String, password: String) {
        lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) {
                userDao.login(username, password)
            }

            if (user != null) {
                Toast.makeText(this@Login, "Welcome, ${user.username}!", Toast.LENGTH_SHORT).show()

                // Navigate to MainActivity with user info
                val intent = Intent(this@Login, MainActivity::class.java).apply {
                    putExtra("username", user.username)
                    putExtra("email", user.email)
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this@Login, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
