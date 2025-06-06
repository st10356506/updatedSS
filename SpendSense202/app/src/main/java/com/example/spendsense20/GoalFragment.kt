package com.example.spendsense20

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendsense20.data.Goal
import com.example.spendsense20.adapter.GoalAdapter
import com.example.spendsense20.databinding.FragmentGoalBinding
import com.example.spendsense20.viewmodel.GoalViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class GoalFragment : Fragment() {

    private var _binding: FragmentGoalBinding? = null
    private val binding get() = _binding!!

    private val seekBarMinLimit = 10
    private val seekBarMaxLimit = 100000

    private var minContribution = seekBarMinLimit
    private var maxContribution = seekBarMaxLimit

    private lateinit var goalAdapter: GoalAdapter
    private lateinit var viewModel: GoalViewModel

    private var startDate: String = ""
    private var endDate: String = ""
    private var selectedCategory: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoalBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[GoalViewModel::class.java]

        setupCategoryDropdown()
        setupSeekBars()
        setupEditTextListeners()
        setupDatePickers()
        setupRecyclerView()  // Initialize RecyclerView
        setupAddGoalButton()
        observeGoals()  // Observe goals from ViewModel

        return binding.root
    }

    private fun setupCategoryDropdown() {
        val categories = listOf("Food", "Transport", "Bills", "Shopping", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                binding.editCustomCategory.visibility =
                    if (categories[position] == "Other") View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerCategory.adapter = adapter
        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategory = parent.getItemAtPosition(position).toString()
                binding.editCustomCategory.visibility =
                    if (selectedCategory == "Other") View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedCategory = ""
            }
        }
    }

    private fun setupSeekBars() {
        val range = seekBarMaxLimit - seekBarMinLimit
        binding.seekBarMin.max = range / 10
        binding.seekBarMax.max = range / 10

        binding.seekBarMin.progress = (minContribution - seekBarMinLimit) / 10
        binding.seekBarMax.progress = (maxContribution - seekBarMinLimit) / 10

        binding.editTextMinInput.setText(minContribution.toString())
        binding.editTextMaxInput.setText(maxContribution.toString())

        updateRangeText()

        binding.seekBarMin.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    minContribution = seekBarMinLimit + (progress * 10)
                    if (minContribution > maxContribution) {
                        maxContribution = minContribution
                        binding.seekBarMax.progress = (maxContribution - seekBarMinLimit) / 10
                        binding.editTextMaxInput.setText(maxContribution.toString())
                    }
                    binding.editTextMinInput.setText(minContribution.toString())
                    updateRangeText()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.seekBarMax.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    maxContribution = seekBarMinLimit + (progress * 10)
                    if (maxContribution < minContribution) {
                        minContribution = maxContribution
                        binding.seekBarMin.progress = (minContribution - seekBarMinLimit) / 10
                        binding.editTextMinInput.setText(minContribution.toString())
                    }
                    binding.editTextMaxInput.setText(maxContribution.toString())
                    updateRangeText()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupEditTextListeners() {
        binding.editTextMinInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s?.toString()?.toIntOrNull()
                if (input != null) {
                    minContribution = input.coerceIn(seekBarMinLimit, seekBarMaxLimit)
                    if (minContribution > maxContribution) {
                        maxContribution = minContribution
                        binding.editTextMaxInput.setText(maxContribution.toString())
                        binding.seekBarMax.progress = (maxContribution - seekBarMinLimit) / 10
                    }
                    binding.seekBarMin.progress = (minContribution - seekBarMinLimit) / 10
                    updateRangeText()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.editTextMaxInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s?.toString()?.toIntOrNull()
                if (input != null) {
                    maxContribution = input.coerceIn(seekBarMinLimit, seekBarMaxLimit)
                    if (maxContribution < minContribution) {
                        minContribution = maxContribution
                        binding.editTextMinInput.setText(minContribution.toString())
                        binding.seekBarMin.progress = (minContribution - seekBarMinLimit) / 10
                    }
                    binding.seekBarMax.progress = (maxContribution - seekBarMinLimit) / 10
                    updateRangeText()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()

        binding.btnStartPickDate.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, day ->
                calendar.set(year, month, day)
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                startDate = sdf.format(calendar.time)
                binding.startDateText.text = startDate
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnEndPickDate.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, day ->
                calendar.set(year, month, day)
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                endDate = sdf.format(calendar.time)
                binding.endDateText.text = endDate
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun updateRangeText() {
        binding.currentRangeText.text = "Range: R$minContribution - R$maxContribution"
    }

    private fun setupRecyclerView() {
        goalAdapter = GoalAdapter(goals = listOf(), deleteGoal = { goal ->
            // Call deleteGoal with the goal's ID when deleting
            deleteGoal(goal.id)  // Pass goal.id or goal directly based on your implementation
        })
        binding.goalsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.goalsRecyclerView.adapter = goalAdapter
    }

    private fun setupAddGoalButton() {
        binding.btnAddGoal.setOnClickListener {
            val finalCategory = if (binding.editCustomCategory.visibility == View.VISIBLE &&
                binding.editCustomCategory.text.isNotBlank()
            ) {
                binding.editCustomCategory.text.toString()
            } else {
                selectedCategory
            }

            if (finalCategory.isBlank() || startDate.isBlank() || endDate.isBlank()) {
                Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get current user ID
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId.isNullOrBlank()) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ Attach userId to goal
            val goal = Goal(
                startDate = startDate,
                endDate = endDate,
                category = finalCategory,
                minContribution = minContribution,
                maxContribution = maxContribution,
                id = userId
            )

            viewModel.insertGoal(goal)
            Toast.makeText(requireContext(), "Goal saved!", Toast.LENGTH_SHORT).show()
            clearInputs()
        }
    }


    private fun deleteGoal(goalId: String) {
        viewModel.deleteGoal(goalId)  // Deleting goal by ID
        Toast.makeText(requireContext(), "Goal deleted!", Toast.LENGTH_SHORT).show()
    }

    private fun clearInputs() {
        minContribution = seekBarMinLimit
        maxContribution = seekBarMaxLimit

        binding.editTextMinInput.setText(minContribution.toString())
        binding.editTextMaxInput.setText(maxContribution.toString())
        binding.seekBarMin.progress = (minContribution - seekBarMinLimit) / 10
        binding.seekBarMax.progress = (maxContribution - seekBarMinLimit) / 10
        binding.currentRangeText.text = "Range: R$minContribution - R$maxContribution"

        startDate = ""
        endDate = ""
        selectedCategory = ""
        binding.spinnerCategory.setSelection(0)
        binding.editCustomCategory.setText("")
        binding.editCustomCategory.visibility = View.GONE
        binding.startDateText.text = ""
        binding.endDateText.text = ""
    }

    private fun observeGoals() {
        viewModel.allGoals.observe(viewLifecycleOwner) { goalWithIdList ->
            val goalList = goalWithIdList.map { it.goal }
            goalAdapter.updateGoals(goalList) // ✅ Now it's List<Goal>
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
