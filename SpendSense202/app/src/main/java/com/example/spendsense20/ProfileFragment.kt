package com.example.spendsense20.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.spendsense20.Login
import com.example.spendsense20.R
import com.example.spendsense20.databinding.FragmentProfileBinding
import com.example.spendsense20.viewmodel.UserViewModel
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by viewModels()

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

        val email = arguments?.getString("email")
        val username = arguments?.getString("username")

        Log.d("ProfileFragment", "Received username: $username, email: $email")
        Toast.makeText(requireContext(), "Username: $username, Email: $email", Toast.LENGTH_LONG).show()

        if (email.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No email passed", Toast.LENGTH_SHORT).show()
            return
        }

        binding.tvUsername.text = username
        binding.tvEmail.text = email

        fetchUserData(email)

        binding.btnLogout.setOnClickListener {
            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.ivEdit.setOnClickListener {
            showImageMenu()
        }
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

    private fun fetchUserData(email: String) {
        lifecycleScope.launch {
            userViewModel.getUserByEmail(email) { fetchedUser ->
                if (!isAdded) return@getUserByEmail

                fetchedUser?.let {
                    binding.tvUsername.text = it.username
                    binding.tvEmail.text = it.email
               }  ?: run {
                    Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
