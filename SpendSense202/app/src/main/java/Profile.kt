package com.example.spendsense20.ui
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.spendsense20.databinding.FragmentProfileBinding
import com.example.spendsense20.viewmodel.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: FragmentProfileBinding
    private val userViewModel: UserViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
// Get email passed from LoginActivity
        val email = intent.getStringExtra("email")
        if (email.isNullOrEmpty()) {
            Toast.makeText(this, "No email passed", Toast.LENGTH_SHORT).show()
            return
        }
// Fetch user data based on email
        fetchUserData(email)
    }
    private fun fetchUserData(email: String) {
        lifecycleScope.launch {
// Fetch user by email from the repository
            val user = userViewModel.getUserByEmail(email) { fetchedUser ->
// Update the UI after user is fetched
                fetchedUser?.let {

                    binding.tvUsername.text = it.username
                    binding.tvEmail.text = it.email
                } ?: run {
                    Toast.makeText(this@ProfileActivity, "User not found",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}