package com.example.spendsense20

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.spendsense20.databinding.FragmentRegisterBinding
import com.google.firebase.database.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var tvAlreadyAccount: TextView
    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tvAlreadyAccount = findViewById(R.id.tvAlreadyAccount)

        // Reference to Firebase Realtime Database
        databaseRef = FirebaseDatabase.getInstance().getReference("users")

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Log the input values for debugging
            Log.d("RegisterActivity", "Username: $username, Email: $email, Password: $password")

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                checkIfUserExists(username, email, password)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        tvAlreadyAccount.setOnClickListener {
            val go = Intent(this@RegisterActivity, Login::class.java)
            startActivity(go)
        }
    }

    // Check if username is already taken
    private fun checkIfUserExists(username: String, email: String, password: String) {
        Log.d("RegisterActivity", "Checking if user exists with username: $username")

        databaseRef.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Log.d("RegisterActivity", "Username already exists")
                        Toast.makeText(this@RegisterActivity, "Username already exists", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("RegisterActivity", "Username is available, proceeding with registration")
                        registerUser(username, email, password)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("RegisterActivity", "Database error: ${error.message}")
                    Toast.makeText(this@RegisterActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Register the user
    private fun registerUser(username: String, email: String, password: String) {
        val userId = databaseRef.push().key ?: return
        val user = User(username, password, email)

        // Log the user data being saved
        Log.d("RegisterActivity", "Registering user: $user")

        databaseRef.child(userId).setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "User registered successfully")
                Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, Login::class.java))
                finish()
            }
            .addOnFailureListener {
                Log.e("RegisterActivity", "Registration failed: ${it.message}")
                Toast.makeText(this, "Registration failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
