package com.example.spendsense20

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
//initialize views
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var registerButton: Button
    private lateinit var backToLoginText: TextView
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_register) // Android Developers (n.d.). Build a UI with Layout Editor. Available at: https://developer.android.com/studio/write/layout-editor

        firebaseAuth = FirebaseAuth.getInstance() // Firebase (2020). Firebase Authentication. Available at: https://firebase.google.com/docs/auth
// Android Developers (n.d.). FindViewById usage. Available at: https://developer.android.com/reference/android/app/Activity#findViewById(int)
        usernameEditText = findViewById(R.id.etUsername)
        emailEditText = findViewById(R.id.etEmail)
        passwordEditText = findViewById(R.id.etPassword)
        registerButton = findViewById(R.id.btnRegister)
        backToLoginText = findViewById(R.id.tvAlreadyAccount)
//register logic
        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
//check if all fields are filled
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show() // GeeksforGeeks (2023). Android - Login and Logout Using Shared Preferences in Kotlin. Available at: https://www.geeksforgeeks.org/android-login-and-logout-using-shared-preferences-in-kotlin/
                return@setOnClickListener
            }

            if (!isValidPassword(password)) {
                passwordEditText.error = "Password must be at least 6 characters, include uppercase, lowercase, and a number" // OWASP Foundation (2022). Password Policy Guidelines. Available at: https://owasp.org/www-community/password-special-requirements
                return@setOnClickListener
            }
//create new user in database
            firebaseAuth.createUserWithEmailAndPassword(email, password) // Firebase (2020). Create Users with Email and Password. Available at: https://firebase.google.com/docs/auth/android/password-auth
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val currentUser = firebaseAuth.currentUser
                        if (currentUser != null) {
                            val user = User( // User data class assumed defined elsewhere
                                username = username,
                                email = currentUser.email ?: "",
                                uid = currentUser.uid
                            )
                            FirebaseDatabase.getInstance().getReference("users") // Firebase (2019). Firebase Realtime Database. Available at: https://firebase.google.com/docs/database
                                .child(currentUser.uid)
                                .setValue(user)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show() // GeeksforGeeks (2023). Android Toasts. Available at: https://www.geeksforgeeks.org/android-toast-message-using-kotlin/
                                    startActivity(Intent(this, Login::class.java))
                                    finish()
                                }
                                //handle errors
                                .addOnFailureListener {
                                    Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show() // GeeksforGeeks (2023). Android Toasts. Available at: https://www.geeksforgeeks.org/android-toast-message-using-kotlin/
                                }
                        }
                        //message if registration fails
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show() // Firebase (2020). Firebase Authentication Error Handling. Available at: https://firebase.google.com/docs/auth/android/errors
                    }
                }
        }
//back to login button logic
        backToLoginText.setOnClickListener {
            startActivity(Intent(this, Login::class.java)) // Android Developers (n.d.). Start Activity. Available at: https://developer.android.com/reference/android/content/Context#startActivity(android.content.Intent)
            finish()
        }
    }
}
//check if password is valid
private fun isValidPassword(password: String): Boolean {
    val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}$")
    return passwordRegex.matches(password)
}
