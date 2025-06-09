package com.example.spendsense20

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendsense20.databinding.FragmentCalendarBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class CalendarFragment : Fragment() {
//initialize views
    private lateinit var binding: FragmentCalendarBinding
    private lateinit var financeAdapter: FinanceAdapter
    private lateinit var databaseRef: DatabaseReference
    private var selectedDate: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)

        // Get current user's UID and setup user-specific DB reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        // Google Firebase. (n.d.). Firebase Authentication documentation. Retrieved June 7, 2025, from https://firebase.google.com/docs/auth
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return binding.root
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("finances").child(userId)
        // Google Firebase. (n.d.). Firebase Realtime Database documentation. Retrieved June 7, 2025, from https://firebase.google.com/docs/database

        // Adapter setup
        financeAdapter = FinanceAdapter(
            onImageClick = { imageUri ->
                val intent = Intent(requireContext(), ImageViewActivity::class.java)
                intent.putExtra("imageUri", imageUri)
                startActivity(intent)
            },
            onDeleteClick = { finance ->
                databaseRef.child(finance.id).removeValue()
                // Google Firebase. (n.d.). Delete data. Retrieved June 7, 2025, from https://firebase.google.com/docs/database/android/delete-data
                loadFinanceDataForSelectedDate() // refresh data
            }
        )

        binding.transactionsRecyclerView.apply {
            adapter = financeAdapter
            layoutManager = LinearLayoutManager(context)
            // Android Developers. (n.d.). RecyclerView and LinearLayoutManager. Retrieved June 7, 2025, from https://developer.android.com/guide/topics/ui/layout/recyclerview
        }

        binding.btnAddFinance.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .add(R.id.wholePage, AddFragment())
                .addToBackStack(null)
                .commit()
            Toast.makeText(requireContext(), "Opened Finance Page", Toast.LENGTH_LONG).show()
        }

        setupCalendarView()
        selectedDate = getCurrentDate()
        loadFinanceDataForSelectedDate()

        return binding.root
    }
//show the calendar
    private fun setupCalendarView() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val formattedMonth = (month + 1).toString().padStart(2, '0')
            val formattedDay = dayOfMonth.toString().padStart(2, '0')
            selectedDate = "$year-$formattedMonth-$formattedDay"
            loadFinanceDataForSelectedDate()
        }
        // Android Developers. (n.d.). CalendarView. Retrieved June 7, 2025, from https://developer.android.com/reference/android/widget/CalendarView
    }
//load finances to the calendar
    private fun loadFinanceDataForSelectedDate() {
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            // Google Firebase. (n.d.). Listen for single value events. Retrieved June 7, 2025, from https://firebase.google.com/docs/database/android/read-and-write#read_data
            override fun onDataChange(snapshot: DataSnapshot) {
                val filteredList = mutableListOf<FinanceEntity>()
                for (child in snapshot.children) {
                    try {
                        val entry = child.getValue(FinanceEntity::class.java)
                        if (entry?.date == selectedDate) {
                            filteredList.add(entry)
                        }
                    } catch (e: Exception) {
                        // Log and skip the invalid entries
                        android.util.Log.e("CalendarFragment", "Skipping invalid entry: ${child.key}, error: ${e.message}")
                    }
                }

                financeAdapter.submitList(filteredList)
//check if the finances for the day are present
                if (filteredList.isEmpty()) {
                    binding.emptyStateLayout.visibility = View.VISIBLE
                    binding.transactionsRecyclerView.visibility = View.GONE
                } else {
                    binding.emptyStateLayout.visibility = View.GONE
                    binding.transactionsRecyclerView.visibility = View.VISIBLE
                }
            }
//handle database errors
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error loading data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
//get the current date and show it on the calendar
    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        // Java Platform SE 8. (n.d.). Calendar. Retrieved June 7, 2025, from https://docs.oracle.com/javase/8/docs/api/java/util/Calendar.html
        val year = calendar.get(Calendar.YEAR)
        val month = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val day = calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        return "$year-$month-$day"
    }
}
