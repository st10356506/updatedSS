package com.example.spendsense20.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.spendsense20.FinanceEntity
import com.example.spendsense20.Login
import com.example.spendsense20.R
import com.example.spendsense20.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var financeRef: DatabaseReference
    private lateinit var userRef: DatabaseReference
    private val goal = 15000.0

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

        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
        financeRef = FirebaseDatabase.getInstance().getReference("finances").child(uid)

        fetchUsername(uid)
        fetchRankAndBadge()

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
    }

    private fun fetchUsername(uid: String) {
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

    private fun fetchRankAndBadge() {
        financeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val finances = mutableListOf<FinanceEntity>()
                for (child in snapshot.children) {
                    val entity = child.getValue(FinanceEntity::class.java)
                    if (entity != null) {
                        finances.add(entity)
                    }
                }
                updateRankUI(finances)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Failed to fetch finances: ${error.message}")
            }
        })
    }

    private fun updateRankUI(finances: List<FinanceEntity>) {
        val income = finances.filter { it.type == "income" }.sumOf { it.amount }
        val expenses = finances.filter { it.type == "expense" }.sumOf { it.amount }
        val balance = income - expenses

        val points = (balance / 100).toInt().coerceAtLeast(0)
        val rank = when {
            points >= 200 -> "Platinum"
            points >= 100 -> "Gold"
            points >= 50 -> "Silver"
            else -> "Bronze"
        }

        binding.tvUserRank.text = rank

        val badgeRes = when (rank) {
            "Bronze" -> R.drawable.bronze
            "Silver" -> R.drawable.silver
            "Gold" -> R.drawable.gold
            "Platinum" -> R.drawable.platinum
            else -> R.drawable.bronze
        }

        binding.ivRankBadge.setImageResource(badgeRes)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}