package com.example.spendsense20.ui

import android.content.Intent
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

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var databaseRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

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

        // ✅ Correctly display email
        binding.tvEmail.text = email ?: "No email"

        // ✅ Fetch and display the username from Firebase Database
        fetchUsername(uid)

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
        databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
        databaseRef.child("username").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.getValue(String::class.java)
                binding.tvUsername.text = username ?: "Username not found"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Database error: ${error.message}")
                Toast.makeText(requireContext(), "Failed to load username", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
