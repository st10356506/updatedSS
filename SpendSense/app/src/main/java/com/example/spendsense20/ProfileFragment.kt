package com.example.spendsense20.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.spendsense20.Login
import com.example.spendsense20.R
import com.example.spendsense20.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.Angle
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit

class ProfileFragment : Fragment() {
//initialize views
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var userRef: DatabaseReference
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false) // Developers, A. (n.d.). Build a UI with Layout Editor. Available at: https://developer.android.com/studio/write/layout-editor
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance() // Anon (2020a). Manage Users in Firebase. Available at: https://firebase.google.com/docs/auth/web/manage-users
        database = FirebaseDatabase.getInstance() // Firebase (2019). Firebase Realtime Database. Available at: https://firebase.google.com/docs/database
//get the current user info
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show() // GeeksforGeeks (2023). Android - Login and Logout Using Shared Preferences in Kotlin. Available at: https://www.geeksforgeeks.org/android-login-and-logout-using-shared-preferences-in-kotlin/
            startActivity(Intent(requireContext(), Login::class.java))
            requireActivity().finish()
            return
        }

        val uid = currentUser.uid
        val email = currentUser.email
        binding.tvEmail.text = email ?: "No email" // GeeksforGeeks (2021). Implementing Edit Profile Data Functionality in Social Media Android App. Available at: https://www.geeksforgeeks.org/implementing-edit-profile-data-functionality-in-social-media-android-app/

        userRef = database.getReference("users").child(uid) // Firebase (2019). Firebase Realtime Database. Available at: https://firebase.google.com/docs/database

        fetchUsername()
        fetchRankAndBadge(uid)
//logout logic
        binding.btnLogout.setOnClickListener {
            auth.signOut() // Anon (2020a). Manage Users in Firebase. Available at: https://firebase.google.com/docs/auth/web/manage-users
            val prefs = requireContext().getSharedPreferences("SpendSensePrefs", 0)
            prefs.edit().clear().apply() // GeeksforGeeks (2023). Android - Login and Logout Using Shared Preferences in Kotlin. Available at: https://www.geeksforgeeks.org/android-login-and-logout-using-shared-preferences-in-kotlin/

            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
//edit profile icon
        binding.ivEdit.setOnClickListener {
            showImageMenu() // Aamjit Yanglem (2022). How do I add a functional edit icon for a profile picture in Android Studio? Available at: https://stackoverflow.com/questions/71860869/how-do-i-add-a-functional-edit-icon-for-a-profile-picture-in-android-studio
        }
//show the rank image
        binding.ivRankBadge.setOnClickListener {
            launchKonfetti() // Segijn, D. (2024). DanielMartinus/Konfetti. Available at: https://github.com/DanielMartinus/Konfetti
        }
    }
//fetch username and email
    private fun fetchUsername() {
        userRef.child("username").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.getValue(String::class.java)
                binding.tvUsername.text = username ?: "Username not found" // Firebase (2019). Firebase Realtime Database. Available at: https://firebase.google.com/docs/database
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Failed to load username: ${error.message}") // Firebase (2019). Firebase Realtime Database. Available at: https://firebase.google.com/docs/database
            }
        })
    }
//fetch rank badge
    private fun fetchRankAndBadge(uid: String) {
        val financeRef = database.getReference("finances").child(uid) // Firebase (2019). Firebase Realtime Database. Available at: https://firebase.google.com/docs/database

        financeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalIncome = 0.0
                var totalExpense = 0.0

                for (child in snapshot.children) {
                    val type = child.child("type").getValue(String::class.java)
                    val amount = child.child("amount").getValue(Double::class.java) ?: 0.0

                    if (type == "income") {
                        totalIncome += amount
                    } else if (type == "expense") {
                        totalExpense += amount
                    }
                }
//update the rank badge
                val balance = totalIncome - totalExpense
                val points = (balance / 100).toInt().coerceAtLeast(0)

                val rank = when {
                    points >= 800 -> "Platinum"
                    points >= 500 -> "Emerald"
                    points >= 250 -> "Gold"
                    points >= 100 -> "Silver"
                    else -> "Bronze"
                }

                binding.tvUserRank.text = rank

                val badgeRes = when (rank) {
                    "Bronze" -> R.drawable.bronze
                    "Silver" -> R.drawable.silver
                    "Gold" -> R.drawable.gold
                    "Emerald" -> R.drawable.emerald
                    "Platinum" -> R.drawable.platinum
                    else -> R.drawable.bronze
                }

                binding.ivRankBadge.setImageResource(badgeRes) // GeeksforGeeks (2021). Implementing Edit Profile Data Functionality in Social Media Android App. Available at: https://www.geeksforgeeks.org/implementing-edit-profile-data-functionality-in-social-media-android-app/
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Failed to fetch finance data: ${error.message}") // Firebase (2019). Firebase Realtime Database. Available at: https://firebase.google.com/docs/database
            }
        })
    }
//edit profile icon
    private fun showImageMenu() {
        val popup = PopupMenu(requireContext(), binding.ivEdit)
        popup.menuInflater.inflate(R.menu.menu_profile_image, popup.menu) // Android Developers (n.d.). Build a UI with Layout Editor. Available at: https://developer.android.com/studio/write/layout-editor
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.option_image1 -> {
                    binding.ivProfile.setImageResource(R.drawable.newguy) // Aamjit Yanglem (2022). How do I add a functional edit icon for a profile picture in Android Studio? Available at: https://stackoverflow.com/questions/71860869/how-do-i-add-a-functional-edit-icon-for-a-profile-picture-in-android-studio
                    true
                }
                R.id.option_image2 -> {
                    binding.ivProfile.setImageResource(R.drawable.newgirl) // Aamjit Yanglem (2022). How do I add a functional edit icon for a profile picture in Android Studio? Available at: https://stackoverflow.com/questions/71860869/how-do-i-add-a-functional-edit-icon-for-a-profile-picture-in-android-studio
                    true
                }
                else -> false
            }
        }
        popup.show() // Android Developers (n.d.). Build a UI with Layout Editor. Available at: https://developer.android.com/studio/write/layout-editor
    }
//show confetti when rank is clicked
    private fun launchKonfetti() {
        binding.konfettiView.start(
            Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                angle = Angle.BOTTOM,
                colors = listOf(
                    0xFF006400.toInt(),
                    0xFF228B22.toInt(),
                    0xFF32CD32.toInt(),
                    0xFF7CFC00.toInt()
                ),
                emitter = Emitter(duration = 2, TimeUnit.SECONDS).max(100), // Segijn, D. (2024). DanielMartinus/Konfetti. Available at: https://github.com/DanielMartinus/Konfetti
                position = Position.Relative(0.5, 0.3) // Segijn, D. (2024). DanielMartinus/Konfetti. Available at: https://github.com/DanielMartinus/Konfetti
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
