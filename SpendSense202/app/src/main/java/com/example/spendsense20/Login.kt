package com.example.spendsense20

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*

class Login : AppCompatActivity() {

    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var backToRegisterText: TextView
    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_login)

        // Bind views
        emailEditText = findViewById(R.id.etUsername)
        passwordEditText = findViewById(R.id.etPassword)
        loginButton = findViewById(R.id.btnLogin)
        backToRegisterText = findViewById(R.id.tvbackToReg)

        // Reference to Firebase Database
        databaseRef = FirebaseDatabase.getInstance().getReference("users")

        // Handle login button click
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password)
            }
        }

        // Navigate to Register screen
        backToRegisterText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun loginUser(email: String, password: String) {
        // Query by email
        databaseRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnap in snapshot.children) {
                            val user = userSnap.getValue(User::class.java)
                            if (user != null && user.password == password) {
                                Toast.makeText(this@Login, "Welcome, ${user.username}!", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this@Login, MainActivity::class.java).apply {
                                    putExtra("username", user.username)
                                    putExtra("email", user.email)
                                }
                                startActivity(intent)
                                finish()
                                return
                            }
                        }
                        Toast.makeText(this@Login, "Incorrect password", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@Login, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Login, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
