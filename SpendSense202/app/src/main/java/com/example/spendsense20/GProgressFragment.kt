package com.example.spendsense20

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.spendsense20.viewmodel.GoalViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class GProgressFragment : Fragment() {

    private lateinit var viewModel: GoalViewModel

    private lateinit var labelFood: TextView
    private lateinit var minFood: TextView
    private lateinit var progressFood: TextView
    private lateinit var maxFood: TextView
    private lateinit var progressBarFood: ProgressBar

    private lateinit var labelTransport: TextView
    private lateinit var minTransport: TextView
    private lateinit var progressTransport: TextView
    private lateinit var maxTransport: TextView
    private lateinit var progressBarTransport: ProgressBar

    private lateinit var labelBills: TextView
    private lateinit var minBills: TextView
    private lateinit var progressBills: TextView
    private lateinit var maxBills: TextView
    private lateinit var progressBarBills: ProgressBar

    private lateinit var labelShopping: TextView
    private lateinit var minShopping: TextView
    private lateinit var progressShopping: TextView
    private lateinit var maxShopping: TextView
    private lateinit var progressBarShopping: ProgressBar

    private lateinit var labelOther: TextView
    private lateinit var minOther: TextView
    private lateinit var progressOther: TextView
    private lateinit var maxOther: TextView
    private lateinit var progressBarOther: ProgressBar

    private lateinit var btnBack : Button

    private lateinit var databaseRef: DatabaseReference
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Map to hold expense sums by category for current month
    private val expenseSums = mutableMapOf<String, Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gprogress, container, false)

        labelFood = view.findViewById(R.id.labelFood)
        minFood = view.findViewById(R.id.minFood)
        progressFood = view.findViewById(R.id.progressFood)
        maxFood = view.findViewById(R.id.maxFood)
        progressBarFood = view.findViewById(R.id.progressBarFood)

        labelTransport = view.findViewById(R.id.labelTransport)
        minTransport = view.findViewById(R.id.minTransport)
        progressTransport = view.findViewById(R.id.progressTransport)
        maxTransport = view.findViewById(R.id.maxTransport)
        progressBarTransport = view.findViewById(R.id.progressBarTransport)

        labelBills = view.findViewById(R.id.labelBills)
        minBills = view.findViewById(R.id.minBills)
        progressBills = view.findViewById(R.id.progressBills)
        maxBills = view.findViewById(R.id.maxBills)
        progressBarBills = view.findViewById(R.id.progressBarBills)

        labelShopping = view.findViewById(R.id.labelShopping)
        minShopping = view.findViewById(R.id.minShopping)
        progressShopping = view.findViewById(R.id.progressShopping)
        maxShopping = view.findViewById(R.id.maxShopping)
        progressBarShopping = view.findViewById(R.id.progressBarShopping)

        labelOther = view.findViewById(R.id.labelOther)
        minOther = view.findViewById(R.id.minOther)
        progressOther = view.findViewById(R.id.progressOther)
        maxOther = view.findViewById(R.id.maxOther)
        progressBarOther = view.findViewById(R.id.progressBarOther)

        btnBack = view.findViewById(R.id.btnBack)

        viewModel = ViewModelProvider(this)[GoalViewModel::class.java]

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            databaseRef = FirebaseDatabase.getInstance().getReference("finances").child(currentUser.uid)
            loadExpensesForCurrentMonth()
        }

        observeGoals()

        return view
    }

    private fun loadExpensesForCurrentMonth() {
        expenseSums.clear()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val monthStart = calendar.time

        calendar.add(Calendar.MONTH, 1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.add(Calendar.DATE, -1)
        val monthEnd = calendar.time

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    if (child.key == "balance" || child.key == "budget_editable") continue

                    val finance = child.getValue(FinanceEntity::class.java) ?: continue

                    if (!finance.type.equals("expense", ignoreCase = true)) continue

                    val entryDate = try {
                        dateFormat.parse(finance.date)
                    } catch (e: Exception) {
                        null
                    }
                    if (entryDate == null) continue

                    if (!entryDate.before(monthStart) && !entryDate.after(monthEnd)) {
                        val category = finance.name.trim()
                        val currentSum = expenseSums[category] ?: 0
                        expenseSums[category] = currentSum + finance.amount.toInt()
                    }
                }
                // Refresh goals UI after loading expenses
                observeGoals()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
            }
        })
    }

    private fun observeGoals() {
        viewModel.allGoals.observe(viewLifecycleOwner) { goalWithIdList ->
            val goals = goalWithIdList.map { it.goal }

            updateCategoryViews(goals.filter { it.category.equals("Food", true) }, minFood, progressFood, maxFood, progressBarFood, "Food")
            updateCategoryViews(goals.filter { it.category.equals("Transport", true) }, minTransport, progressTransport, maxTransport, progressBarTransport, "Transport")
            updateCategoryViews(goals.filter { it.category.equals("Bills", true) }, minBills, progressBills, maxBills, progressBarBills, "Bills")
            updateCategoryViews(goals.filter { it.category.equals("Shopping", true) }, minShopping, progressShopping, maxShopping, progressBarShopping, "Shopping")
            updateCategoryViews(goals.filter { it.category.equals("Other", true) }, minOther, progressOther, maxOther, progressBarOther, "Other")
        }
    }

    private fun updateCategoryViews(
        goals: List<com.example.spendsense20.data.Goal>,
        minTextView: TextView,
        progressTextView: TextView,
        maxTextView: TextView,
        progressBar: ProgressBar,
        category: String
    ) {
        if (goals.isEmpty()) {
            minTextView.text = "R0"
            progressTextView.text = "R0"
            maxTextView.text = "R0"
            progressBar.progress = 0
        } else {
            val minSum = goals.sumOf { it.minContribution.toInt() }
            val maxSum = goals.sumOf { it.maxContribution.toInt() }

            val actualSpending = expenseSums[category] ?: 0

            minTextView.text = "R$minSum"
            progressTextView.text = "R$actualSpending"
            maxTextView.text = "R$maxSum"

            val progressPercent = if (maxSum > 0) {
                ((actualSpending.toFloat() / maxSum.toFloat()) * 100).toInt().coerceIn(0, 100)
            } else 0

            progressBar.progress = progressPercent
        }

        btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .addToBackStack(null)
                .commit()
        }
    }

}