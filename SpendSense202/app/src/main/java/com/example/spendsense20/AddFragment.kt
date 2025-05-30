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

class AddFragment : Fragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddBinding.inflate(inflater, container, false)
        val view = binding.root

        val saveButton = binding.saveBalanceButton
        val editButton = binding.editBalanceButton

        val totalBalanceTextView = binding.totalBalanceTextView

        val balanceSeekBar = binding.balanceSeekBar
        val balanceEditText = binding.balanceEditText

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return view
        }

        val userId = currentUser.uid
        databaseRef = FirebaseDatabase.getInstance().getReference("finances").child(userId)

        setEditingEnabled(false)
        saveButton.visibility = View.GONE
        editButton.visibility = View.GONE

        databaseRef.child("balance").get().addOnSuccessListener { balanceSnapshot ->
            val balance = balanceSnapshot.getValue(Int::class.java) ?: 0
            totalBalanceTextView.text = "R$balance"
            balanceSeekBar.progress = balance
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
            balanceSeekBar.progress = 0
            balanceEditText.setText("0")
            isEditing = true
            updateUIEditingState(isEditing)
        }

        balanceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                totalBalanceTextView.text = "R$progress"
                if (isEditing) {
                    balanceEditText.setText(progress.toString())
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        saveButton.setOnClickListener {
            val manualInput = balanceEditText.text.toString().toIntOrNull()
            if (manualInput == null) {
                Toast.makeText(requireContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val newBalance = manualInput

            totalBalanceTextView.text = "R$newBalance"
            balanceSeekBar.progress = newBalance

            setEditingEnabled(false)

            val updates = mapOf(
                "balance" to newBalance,
                "budget_editable" to false
            )
            databaseRef.updateChildren(updates).addOnFailureListener {
                Log.e("AddFragment", "Failed to save balance/edit state: ${it.message}")
                Toast.makeText(requireContext(), "Failed to save data", Toast.LENGTH_SHORT).show()
            }

            isEditing = false
            updateUIEditingState(false)
        }


        val recyclerViewIncome = binding.recyclerViewIncome
        val recyclerViewExpense = binding.recyclerViewExpense
        val recyclerViewFiltered = binding.recyclerViewFilteredExpenses

        adapterIncome = createFinanceAdapter()
        adapterExpense = createFinanceAdapter()
        adapterFiltered = createFinanceAdapter()

        recyclerViewIncome.adapter = adapterIncome
        recyclerViewExpense.adapter = adapterExpense
        recyclerViewFiltered.adapter = adapterFiltered

        recyclerViewIncome.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewExpense.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewFiltered.layoutManager = LinearLayoutManager(requireContext())

        databaseRef.addValueEventListener(object : ValueEventListener {
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
                balanceSeekBar.progress = calculatedBalanceInt.coerceIn(0, balanceSeekBar.max)
                if (isEditing) {
                    balanceEditText.setText(calculatedBalanceInt.toString())
                }

                refreshAdapters()
            }



            override fun onCancelled(error: DatabaseError) {
                Log.e("AddFragment", "Database error: ${error.message}")
            }
        })

        binding.btnStartDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                startDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                binding.btnStartDate.text = "From: $startDate"
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnEndDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                endDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                binding.btnEndDate.text = "To: $endDate"
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

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

                val summaryText = summary.entries.joinToString("\n") { "${it.key}: R${it.value}" }
                binding.tvCategorySummary.text = if (summaryText.isNotBlank()) summaryText else "No category totals found in the selected period."
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

        binding.addExpenseButton.setOnClickListener {
            startActivity(Intent(requireContext(), CameraPage::class.java))
        }

        binding.addIncomeButton.setOnClickListener {
            startActivity(Intent(requireContext(), CameraPage::class.java))
        }

        binding.btnCalculator.setOnClickListener {
            startActivity(Intent(requireContext(), CalculatorPage::class.java))
        }

        return view
    }

    private fun setEditingEnabled(enabled: Boolean) {
        binding.balanceSeekBar.isEnabled = enabled
        binding.balanceEditText.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    private fun updateUIEditingState(editing: Boolean) {
        binding.balanceSeekBar.isEnabled = editing
        binding.balanceEditText.visibility = if (editing) View.VISIBLE else View.GONE
        binding.saveBalanceButton.visibility = if (editing) View.VISIBLE else View.GONE
        binding.editBalanceButton.visibility = if (editing) View.GONE else View.VISIBLE
    }

    private fun createFinanceAdapter(): FinanceAdapter {
        return FinanceAdapter(
            onImageClick = { imageUri ->
                startActivity(Intent(requireContext(), ImageViewActivity::class.java).apply {
                    putExtra("imageUri", imageUri)
                })
            },
            onDeleteClick = { finance ->
                databaseRef.child(finance.id).removeValue()
                allEntries.removeIf { it.id == finance.id }
                refreshAdapters()
            }
        )
    }

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