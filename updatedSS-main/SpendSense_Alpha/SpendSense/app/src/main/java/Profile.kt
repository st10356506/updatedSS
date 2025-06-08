package com.example.spendsense20

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.spendsense20.databinding.FragmentProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: FragmentProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//inflate view
        // Reference: Anon (2025). View binding. [online] Android Developers. Available at: https://developer.android.com/topic/libraries/view-binding?authuser=1 [Accessed 6 Jun. 2025].
        binding = FragmentProfileBinding.inflate(layoutInflater)


        // Reference: Developers, A. (n.d.). Build a UI with Layout Editor. [online] Android Developers. Available at: https://developer.android.com/studio/write/layout-editor. (Developers, A., n.d.)
        setContentView(binding.root)
//show username and email
        // Reference: Anon (2020b). Read and Write Data on the Web | Firebase Realtime Database. [online] Firebase. Available at: https://firebase.google.com/docs/database/web/read-and-write.
        val username = intent.getStringExtra("username")
        val email = intent.getStringExtra("email")

//if userdata is missing
        // Reference: GeeksforGeeks (2023). Android - Login and Logout Using Shared Preferences in Kotlin. [online] GeeksforGeeks. Available at: https://www.geeksforgeeks.org/android-login-and-logout-using-shared-preferences-in-kotlin/.
        if (username.isNullOrEmpty() || email.isNullOrEmpty()) {
            Toast.makeText(this, "Missing user data", Toast.LENGTH_SHORT).show()
            return
        }
//show username and email on text views
        // Sets the text of TextViews with the data retrieved from Intent
        // Reference: Developers, A. (n.d.). Build a UI with Layout Editor. [online] Android Developers. Available at: https://developer.android.com/studio/write/layout-editor. (Developers, A., n.d.)
        binding.tvUsername.text = username
        binding.tvEmail.text = email
    }
}
