package com.example.spendsense20

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.spendsense20.databinding.FragmentRegisterBinding
import com.example.spendsense20.model.User
import com.example.spendsense20.viewmodel.UserViewModel

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: FragmentRegisterBinding
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var tvAlreadyAccount : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tvAlreadyAccount = findViewById(R.id.tvAlreadyAccount)
        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                val user = User(username = username, email = email, password = password)
                userViewModel.registerUser(user,
                    onSuccess = {
                        Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, Login::class.java))
                        finish()
                    },
                    onError = {
                        Toast.makeText(this, "Registration failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    })
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

         tvAlreadyAccount.setOnClickListener {
            val go = Intent(this@RegisterActivity, Login::class.java)
            startActivity(go)
        }
    }
}
