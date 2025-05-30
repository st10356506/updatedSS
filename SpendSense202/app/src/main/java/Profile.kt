package com.example.spendsense20

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.spendsense20.databinding.FragmentProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: FragmentProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get username and email from intent
        val username = intent.getStringExtra("username")
        val email = intent.getStringExtra("email")

        if (username.isNullOrEmpty() || email.isNullOrEmpty()) {
            Toast.makeText(this, "Missing user data", Toast.LENGTH_SHORT).show()
            return
        }

        // Populate UI with intent data
        binding.tvUsername.text = username
        binding.tvEmail.text = email
    }
}
