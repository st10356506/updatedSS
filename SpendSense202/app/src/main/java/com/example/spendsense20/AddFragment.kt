/*Android Developers (n.d.). Fragments. [online] Android Developers. Available at: https://developer.android.com/guide/fragments.
(Android Developers, n.d.)*/

package com.example.spendsense20

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import android.app.DatePickerDialog
import android.widget.SeekBar
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.ViewModelProvider

class AddFragment : Fragment() {

    private lateinit var financeDao: FinanceDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var adapterExpense: FinanceAdapter
    private lateinit var adapterIncome: FinanceAdapter
    private lateinit var adapterFiltered: FinanceAdapter
    private var startDate: String? = null
    private var endDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add, container, false)

        financeDao = FinanceDB.getDatabase(requireContext()).FinanceDao()
        categoryDao = CategoryDB.getDatabase(requireContext()).CategoryDao()


        val totalBalanceTextView = view.findViewById<TextView>(R.id.totalBalanceTextView)
        val balanceSeekBar = view.findViewById<SeekBar>(R.id.balanceSeekBar)
        val saveButton = view.findViewById<Button>(R.id.saveBalanceButton)
        val editButton = view.findViewById<Button>(R.id.editBalanceButton)

        val sharedPref = requireContext().getSharedPreferences("SpendSensePrefs", 0)
        var isEditing = sharedPref.getBoolean("budget_editable", true)
        val savedBalance = sharedPref.getInt("total_balance", 0)

        totalBalanceTextView.text = "R$savedBalance"
        balanceSeekBar.progress = savedBalance
        balanceSeekBar.isEnabled = isEditing
        saveButton.visibility = if (isEditing) View.VISIBLE else View.GONE
        editButton.visibility = if (isEditing) View.GONE else View.VISIBLE

        balanceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                totalBalanceTextView.text = "R$progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        saveButton.setOnClickListener {
            val newBalance = balanceSeekBar.progress
            totalBalanceTextView.text = "R$newBalance"
            balanceSeekBar.isEnabled = false
            saveButton.visibility = View.GONE
            editButton.visibility = View.VISIBLE

            with(sharedPref.edit()) {
                putInt("total_balance", newBalance)
                putBoolean("budget_editable", false)
                apply()
            }
        }

        editButton.setOnClickListener {
            balanceSeekBar.isEnabled = true
            saveButton.visibility = View.VISIBLE
            editButton.visibility = View.GONE

            with(sharedPref.edit()) {
                putBoolean("budget_editable", true)
                apply()
            }
        }

        val recyclerViewIncome = view.findViewById<RecyclerView>(R.id.recyclerViewIncome)
        val recyclerViewExpense = view.findViewById<RecyclerView>(R.id.recyclerViewExpense)

        // Income adapter with delete functionality
        adapterIncome = FinanceAdapter(
            onImageClick = { imageUri ->
                val intent = Intent(requireContext(), ImageViewActivity::class.java)
                intent.putExtra("imageUri", imageUri)
                startActivity(intent)
            },
            onDeleteClick = { finance ->
                viewLifecycleOwner.lifecycleScope.launch {
                    financeDao.deleteFinance(finance.id)
                    val updatedIncome = financeDao.getAllIncome()
                    adapterIncome.submitList(updatedIncome)
                }
            }
        )

        // Expense adapter with delete functionality
        adapterExpense = FinanceAdapter(
            onImageClick = { imageUri ->
                val intent = Intent(requireContext(), ImageViewActivity::class.java)
                intent.putExtra("imageUri", imageUri)
                startActivity(intent)
            },
            onDeleteClick = { finance ->
                viewLifecycleOwner.lifecycleScope.launch {
                    financeDao.deleteFinance(finance.id)
                    val updatedExpenses = financeDao.getAllExpenses()
                    adapterExpense.submitList(updatedExpenses)
                }
            }
        )

        // Filtered expenses adapter
        adapterFiltered = FinanceAdapter(
            onImageClick = { imageUri ->
                val intent = Intent(requireContext(), ImageViewActivity::class.java)
                intent.putExtra("imageUri", imageUri)
                startActivity(intent)
            },
            onDeleteClick = { finance ->
                viewLifecycleOwner.lifecycleScope.launch {
                    financeDao.deleteFinance(finance.id)
                    refreshFilteredData()
                }
            }
        )

        recyclerViewIncome.adapter = adapterIncome
        recyclerViewIncome.layoutManager = LinearLayoutManager(requireContext())

        recyclerViewExpense.adapter = adapterExpense
        recyclerViewExpense.layoutManager = LinearLayoutManager(requireContext())

        val recyclerViewFiltered = view.findViewById<RecyclerView>(R.id.recyclerViewFilteredExpenses)
        recyclerViewFiltered.adapter = adapterFiltered
        recyclerViewFiltered.layoutManager = LinearLayoutManager(requireContext())

        // Loading initial data for income and expenses
        viewLifecycleOwner.lifecycleScope.launch {
            val incomeFinances = financeDao.getAllIncome()
            adapterIncome.submitList(incomeFinances)

            val expenseFinances = financeDao.getAllExpenses()
            adapterExpense.submitList(expenseFinances)
        }

        val btnStartDate = view.findViewById<Button>(R.id.btnStartDate)
        val btnEndDate = view.findViewById<Button>(R.id.btnEndDate)
        val btnShowSummary = view.findViewById<Button>(R.id.btnShowSummary)
        val tvSummary = view.findViewById<TextView>(R.id.tvCategorySummary)

        // Date picker for start date
        btnStartDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                startDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                btnStartDate.text = "From: $startDate"
                refreshFilteredData()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Date picker for end date
        btnEndDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                endDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                btnEndDate.text = "To: $endDate"
                refreshFilteredData()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Show summary button
        btnShowSummary.setOnClickListener {
            if (startDate != null && endDate != null) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val summaries = financeDao.getCategoryTotalsBetweenDates(startDate!!, endDate!!)
                    val summaryText = summaries.joinToString("\n") {
                        "${it.categoryName}: R${it.totalAmount}"
                    }
                    tvSummary.text = if (summaryText.isNotBlank()) summaryText else "No category totals found in the selected period."
                    tvSummary.visibility = View.VISIBLE

                    val filteredExpenses = financeDao.getExpensesBetweenDates(startDate!!, endDate!!)
                    adapterFiltered.submitList(filteredExpenses)
                    recyclerViewFiltered.visibility = if (filteredExpenses.isNotEmpty()) View.VISIBLE else View.GONE
                }
            } else {
                tvSummary.text = "Please select both dates."
                tvSummary.visibility = View.VISIBLE
                recyclerViewFiltered.visibility = View.GONE
            }
        }

        // Button to add expense
        view.findViewById<Button>(R.id.addExpenseButton).setOnClickListener {
            val intent = Intent(requireContext(), CameraPage::class.java)
            startActivity(intent)
        }

        // Button to add income
        view.findViewById<Button>(R.id.addIncomeButton).setOnClickListener {
            val intent = Intent(requireContext(), CameraPage::class.java)
            startActivity(intent)
        }

        // Button to open calculator
        val calculatorButton = view.findViewById<ImageButton>(R.id.btnCalculator)
        calculatorButton.setOnClickListener {
            val intent = Intent(requireContext(), CalculatorPage::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun refreshFilteredData() {
        if (startDate != null && endDate != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                val filtered = financeDao.getExpensesBetweenDates(startDate!!, endDate!!)
                adapterFiltered.submitList(filtered)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
    }
}