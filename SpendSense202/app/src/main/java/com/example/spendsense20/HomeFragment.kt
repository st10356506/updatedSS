package com.example.spendsense20

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var financeAdapter: FinanceAdapter
    private lateinit var databaseRef: DatabaseReference
    private val expensesList = mutableListOf<FinanceEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseRef = FirebaseDatabase.getInstance().getReference("financeEntries")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentDateTextView = view.findViewById<TextView>(R.id.currentDateTextView)
        currentDateTextView.text = getCurrentDate()

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewExpense)
        financeAdapter = FinanceAdapter(
            onImageClick = {
                Toast.makeText(requireContext(), "Image preview not available here", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = {
                Toast.makeText(requireContext(), "Deletion is not allowed on the Home page", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = financeAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadExpensesFromFirebase()
    }

    private fun loadExpensesFromFirebase() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                expensesList.clear()
                for (child in snapshot.children) {
                    val finance = child.getValue(FinanceEntity::class.java)
                    if (finance != null && finance.type == "Expense") {
                        expensesList.add(finance)
                    }
                }
                financeAdapter.submitList(expensesList.toList()) // Create a new list to trigger update
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}
