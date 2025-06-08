package com.example.spendsense20

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.spendsense20.RegisterActivity

class Login : AppCompatActivity() {
//initialize views
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var backToRegisterText: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_login)
//initialize firebase

        // Anon (2020a). Manage Users in Firebase. [online] Firebase.
        // Available at: https://firebase.google.com/docs/auth/web/manage-users
        firebaseAuth = FirebaseAuth.getInstance()

//save to users table in firebase
        // Firebase (2019). Firebase Realtime Database. [online] Firebase.
        // Available at: https://firebase.google.com/docs/database
        databaseRef = FirebaseDatabase.getInstance().getReference("users")


        // Developers, A. (n.d.). Build a UI with Layout Editor. [online] Android Developers.
        // Available at: https://developer.android.com/studio/write/layout-editor
        emailEditText = findViewById(R.id.etUsername)
        passwordEditText = findViewById(R.id.etPassword)
        loginButton = findViewById(R.id.btnLogin)
        backToRegisterText = findViewById(R.id.tvbackToReg)
//login button logic
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
//check if fields are filled
            if (email.isEmpty() || password.isEmpty()) {

                // GeeksforGeeks (2023). Android - Login and Logout Using Shared Preferences in Kotlin. [online] GeeksforGeeks.
                // Available at: https://www.geeksforgeeks.org/android-login-and-logout-using-shared-preferences-in-kotlin/
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            } else {

                // Anon (2020a). Manage Users in Firebase. [online] Firebase.
                // Available at: https://firebase.google.com/docs/auth/web/manage-users
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = firebaseAuth.currentUser?.uid ?: return@addOnCompleteListener

                            // Fetch user profile data from Firebase Realtime Database
                            // Anon (2020b). Read and Write Data on the Web | Firebase Realtime Database. [online] Firebase.
                            // Available at: https://firebase.google.com/docs/database/web/read-and-write
                            databaseRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val user = snapshot.getValue(User::class.java)
                                    if (user != null) {
                                        // Session management to store logged-in user details
                                        SessionManager.userEmail = user.email
                                        SessionManager.username = user.username


                                        // GeeksforGeeks (2023). Android - Login and Logout Using Shared Preferences in Kotlin. [online] GeeksforGeeks.
                                        // Available at: https://www.geeksforgeeks.org/android-login-and-logout-using-shared-preferences-in-kotlin/
                                        Toast.makeText(this@Login, "Welcome, ${user.username}!", Toast.LENGTH_SHORT).show()


                                        // Android Developers (n.d.). Fragments. [online] Android Developers.
                                        // Available at: https://developer.android.com/guide/fragments
                                        val intent = Intent(this@Login, MainActivity::class.java).apply {
                                            putExtra("username", user.username)
                                            putExtra("email", user.email)
                                        }
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(this@Login, "User data not found", Toast.LENGTH_SHORT).show()
                                    }
                                }
//error handling
                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@Login, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                                }
                            })
                        } else {
                            Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
//go back to register
        backToRegisterText.setOnClickListener {
            // Navigate back to RegisterActivity
            // Android Developers (n.d.). Fragments. [online] Android Developers.
            // Available at: https://developer.android.com/guide/fragments
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }
}
