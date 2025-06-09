package com.example.spendsense20

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spendsense20.databinding.FragmentAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat

class AddFragment : Fragment() {
//initialize the views and variables
    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var calculationBreakdownTextView: TextView

    private lateinit var adapterExpense: FinanceAdapter
    private lateinit var adapterIncome: FinanceAdapter
    private lateinit var adapterFiltered: FinanceAdapter
    private lateinit var databaseRef: DatabaseReference
    private var startDate: String? = null
    private var endDate: String? = null

    private val allEntries = mutableListOf<FinanceEntity>()

    private var isEditing: Boolean = true
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//bind views
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddBinding.inflate(inflater, container, false)
        val view = binding.root

        val editButton = binding.editBalanceButton
        val totalBalanceTextView = binding.totalBalanceTextView
        val balanceEditText = binding.balanceEditText

        binding.btnAllExpenses.setOnClickListener {
            val intent = Intent(requireContext(), AllExpensesActivity::class.java)
            startActivity(intent)
        }

        binding.btnAllIncome.setOnClickListener {
            val intent = Intent(requireContext(), AllIncomeActivity::class.java)
            startActivity(intent)
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
    //testing to check if user is logged in, if not this message will show
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return view
        }
//fetch the user id and only display data for the logged in user
        val userId = currentUser.uid
        databaseRef = FirebaseDatabase.getInstance().getReference("finances").child(userId)
        // Google Firebase. (n.d.). Firebase Realtime Database documentation. Retrieved June 7, 2025, from https://firebase.google.com/docs/database

        setEditingEnabled(false)

        editButton.visibility = View.GONE

        // Firebase Realtime Database call to retrieve balance
        databaseRef.child("balance").get().addOnSuccessListener { balanceSnapshot ->
            val balance = balanceSnapshot.getValue(Int::class.java) ?: 0
            totalBalanceTextView.text = "R$balance"

            balanceEditText.setText(balance.toString())

            databaseRef.child("budget_editable").get().addOnSuccessListener { editSnapshot ->
                isEditing = editSnapshot.getValue(Boolean::class.java) ?: true

            }.addOnFailureListener {
                Log.e("AddFragment", "Failed to get editing state: ${it.message}")
                isEditing = true
            }

        }.addOnFailureListener {
            Log.e("AddFragment", "Failed to get balance from Firebase: ${it.message}")
            totalBalanceTextView.text = "R0"
            balanceEditText.setText("0")
            isEditing = true
            updateUIEditingState(isEditing)
        }
//display the filtered data in the recycler view
        val recyclerViewFiltered = binding.recyclerViewFilteredExpenses
        adapterIncome = createFinanceAdapter()
        adapterExpense = createFinanceAdapter()
        adapterFiltered = createFinanceAdapter()

        recyclerViewFiltered.adapter = adapterFiltered
        recyclerViewFiltered.layoutManager = LinearLayoutManager(requireContext())

        databaseRef.addValueEventListener(object : ValueEventListener {
            // Google Firebase. (n.d.). Listen for value events. Retrieved June 7, 2025, from https://firebase.google.com/docs/database/android/read-and-write#read_data
            override fun onDataChange(snapshot: DataSnapshot) {
                allEntries.clear()
                for (child in snapshot.children) {
                    if (child.key == "balance" || child.key == "budget_editable") continue
                    val finance = child.getValue(FinanceEntity::class.java)
                    if (finance != null) {
                        val financeWithId = finance.copy(id = child.key ?: "")
                        allEntries.add(financeWithId)
                    }
                }

                val totalIncome = allEntries.filter { it.type.equals("income", ignoreCase = true) }
                    .sumOf { it.amount }
                val totalExpense = allEntries.filter { it.type.equals("expense", ignoreCase = true) }
                    .sumOf { it.amount }

                val calculatedBalance = totalIncome - totalExpense
                val calculatedBalanceInt = calculatedBalance.toInt()

                totalBalanceTextView.text = "R$calculatedBalanceInt"
                refreshAdapters()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AddFragment", "Database error: ${error.message}")
            }
        })
//filter start date
        binding.btnStartDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                startDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                binding.btnStartDate.text = "From: $startDate"
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
            // Android Developers. (n.d.). DatePickerDialog. Retrieved June 7, 2025, from https://developer.android.com/reference/android/app/DatePickerDialog
        }
//filter end date
        binding.btnEndDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                endDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                binding.btnEndDate.text = "To: $endDate"
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
            // Android Developers. (n.d.). DatePickerDialog. Retrieved June 7, 2025, from https://developer.android.com/reference/android/app/DatePickerDialog
        }
//button for showing the summary
        binding.btnShowSummary.setOnClickListener {
            if (startDate != null && endDate != null) {
                val start = try { dateFormat.parse(startDate!!) } catch (e: Exception) { null }
                val end = try { dateFormat.parse(endDate!!) } catch (e: Exception) { null }

                if (start == null || end == null) {
                    Toast.makeText(requireContext(), "Invalid date format", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val summary = allEntries.filter {
                    val entryDate = try { dateFormat.parse(it.date) } catch (e: Exception) { null }
                    entryDate != null &&
                            !entryDate.before(start) && !entryDate.after(end) &&
                            it.type.equals("expense", ignoreCase = true)
                }.groupBy { it.name }
                    .mapValues { (_, entries) -> entries.sumOf { it.amount } }

                val summaryBuilder = SpannableStringBuilder()
                // Android Developers. (n.d.). SpannableStringBuilder. Retrieved June 7, 2025, from https://developer.android.com/reference/android/text/SpannableStringBuilder

                val blackColor = ContextCompat.getColor(requireContext(), android.R.color.black)
                val redColor = ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
//show the summary
                summary.entries.forEach { (category, total) ->
                    val line = "$category: R$total\n"
                    val startAmount = line.indexOf("R")
                    val endAmount = line.length
                    val startCategory = 0
                    val endCategory = startAmount
                    val startIndex = summaryBuilder.length
                    summaryBuilder.append(line)

                    summaryBuilder.setSpan(
                        ForegroundColorSpan(blackColor),
                        startIndex + startCategory,
                        startIndex + endCategory,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    summaryBuilder.setSpan(
                        ForegroundColorSpan(redColor),
                        startIndex + startAmount,
                        startIndex + endAmount,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
//check if theres data to show in the summary block
                if (summaryBuilder.isNotEmpty()) {
                    binding.tvCategorySummary.text = summaryBuilder
                } else {
                    binding.tvCategorySummary.text = "No category totals found in the selected period."
                }
                binding.tvCategorySummary.visibility = View.VISIBLE
                recyclerViewFiltered.visibility = View.VISIBLE

                databaseRef.get().addOnSuccessListener { snapshot ->
                    val filteredList = mutableListOf<FinanceEntity>()
                    for (item in snapshot.children) {
                        if (item.key == "balance" || item.key == "budget_editable") continue
                        val finance = item.getValue(FinanceEntity::class.java)
                        if (finance != null) {
                            val financeDate = try { dateFormat.parse(finance.date) } catch (e: Exception) { null }
                            if (financeDate != null &&
                                finance.type.equals("expense", ignoreCase = true) &&
                                !financeDate.before(start) && !financeDate.after(end)
                            ) {
                                filteredList.add(finance.copy(id = item.key ?: ""))
                            }
                        }
                    }
                    adapterFiltered.submitList(filteredList.toList())
                }.addOnFailureListener {
                    Log.e("AddFragment", "Failed to load filtered data: ${it.message}")
                }
            } else {
                binding.tvCategorySummary.text = "Please select both dates."
                binding.tvCategorySummary.visibility = View.VISIBLE
                recyclerViewFiltered.visibility = View.GONE
            }
        }
//add expense button
        binding.addExpenseButton.setOnClickListener {
            startActivity(Intent(requireContext(), CameraPage::class.java))
            // Developers, A. (2024). CameraPage Activity. Unpublished internal document.
        }
//add income button
        binding.addIncomeButton.setOnClickListener {
            startActivity(Intent(requireContext(), CameraPage::class.java))
            // Developers, A. (2024). CameraPage Activity. Unpublished internal document.
        }
//show calulator button
        binding.btnCalculator.setOnClickListener {
            startActivity(Intent(requireContext(), CalculatorPage::class.java))
            // GeeksforGeeks. (2019). CalculatorPage Example. Retrieved June 7, 2025, from https://www.geeksforgeeks.org/android-building-a-simple-calculator-app/
        }

        return view
    }

    //show the UI toggle logic
    private fun setEditingEnabled(enabled: Boolean) {
        binding.balanceEditText.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    private fun updateUIEditingState(editing: Boolean) {
        binding.balanceEditText.visibility = if (editing) View.VISIBLE else View.GONE
        binding.editBalanceButton.visibility = if (editing) View.GONE else View.VISIBLE
    }

    //adapter used to populate RecyclerViews
    private fun createFinanceAdapter(): FinanceAdapter {
        return FinanceAdapter(
            onImageClick = { imageUri ->
                startActivity(Intent(requireContext(), ImageViewActivity::class.java).apply {
                    putExtra("imageUri", imageUri)
                })
            },
            onDeleteClick = { finance ->
                databaseRef.child(finance.id).removeValue()
                // Google Firebase. (n.d.). Delete data. Retrieved June 7, 2025, from https://firebase.google.com/docs/database/android/delete-data
                allEntries.removeIf { it.id == finance.id }
                refreshAdapters()
            }
        )
    }
//method for refreshing adapters
    private fun refreshAdapters() {
        adapterIncome.submitList(allEntries.filter { it.type.equals("income", ignoreCase = true) }.toList())
        adapterExpense.submitList(allEntries.filter { it.type.equals("expense", ignoreCase = true) }.toList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
