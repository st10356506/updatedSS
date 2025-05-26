package com.example.spendsense20

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var financeDao: FinanceDao
    private lateinit var financeAdapter: FinanceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        financeDao = FinanceDB.getDatabase(requireContext()).FinanceDao()
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

        loadExpenses()
    }

    private fun loadExpenses() {
        lifecycleScope.launch {
            val expenses = financeDao.getAllExpenses()
            financeAdapter.submitList(expenses)
        }
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}
