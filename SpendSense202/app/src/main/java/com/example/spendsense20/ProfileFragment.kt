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
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), Login::class.java))
            requireActivity().finish()
            return
        }

        val uid = currentUser.uid
        val email = currentUser.email
        binding.tvEmail.text = email ?: "No email"

        userRef = database.getReference("users").child(uid)

        fetchUsername()
        fetchRankAndBadge(uid)

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val prefs = requireContext().getSharedPreferences("SpendSensePrefs", 0)
            prefs.edit().clear().apply()

            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.ivEdit.setOnClickListener {
            showImageMenu()
        }

        binding.ivRankBadge.setOnClickListener {
            launchKonfetti()
        }
    }

    private fun fetchUsername() {
        userRef.child("username").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.getValue(String::class.java)
                binding.tvUsername.text = username ?: "Username not found"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Failed to load username: ${error.message}")
            }
        })
    }

    private fun fetchRankAndBadge(uid: String) {
        val financeRef = database.getReference("finances").child(uid)

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

                binding.ivRankBadge.setImageResource(badgeRes)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Failed to fetch finance data: ${error.message}")
            }
        })
    }

    private fun showImageMenu() {
        val popup = PopupMenu(requireContext(), binding.ivEdit)
        popup.menuInflater.inflate(R.menu.menu_profile_image, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.option_image1 -> {
                    binding.ivProfile.setImageResource(R.drawable.newguy)
                    true
                }
                R.id.option_image2 -> {
                    binding.ivProfile.setImageResource(R.drawable.newgirl)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

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
                emitter = Emitter(duration = 2, TimeUnit.SECONDS).max(100),
                position = Position.Relative(0.5, 0.3)
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}