package com.example.spendsense20

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendsense20.data.Goal
import com.example.spendsense20.adapter.GoalAdapter
import com.example.spendsense20.databinding.FragmentGoalBinding
import com.example.spendsense20.viewmodel.GoalViewModel
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

    private var selectedDate: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoalBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[GoalViewModel::class.java]

        setupSeekBars()     // Configure seek bars
        setupDatePicker()   // Set up calendar picker
        setupRecyclerView() // Prepare RecyclerView
        setupAddGoalButton()// Add goal logic
        observeGoals()      // Observe database

        return binding.root
    }

    private fun observeGoals() {
        viewModel.allGoals.observe(viewLifecycleOwner) { goals ->
            goalAdapter.updateGoals(goals)
        }
    }

    private fun setupSeekBars() {
        val range = seekBarMaxLimit - seekBarMinLimit
        binding.seekBarMin.max = range
        binding.seekBarMax.max = range

        binding.seekBarMin.progress = minContribution - seekBarMinLimit
        binding.seekBarMax.progress = maxContribution - seekBarMinLimit

        updateRangeText()

        // Min seek bar listener
        binding.seekBarMin.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                minContribution = progress + seekBarMinLimit
                if (minContribution > maxContribution) {
                    minContribution = maxContribution
                    binding.seekBarMin.progress = minContribution - seekBarMinLimit
                }
                updateRangeText()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Max seek bar listener
        binding.seekBarMax.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                maxContribution = progress + seekBarMinLimit
                if (maxContribution < minContribution) {
                    maxContribution = minContribution
                    binding.seekBarMax.progress = maxContribution - seekBarMinLimit
                }
                updateRangeText()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updateRangeText() {
        binding.currentRangeText.text = "Range: R$minContribution - R$maxContribution"
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        binding.btnPickDate.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, y, m, d ->
                calendar.set(y, m, d)
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                selectedDate = sdf.format(calendar.time)
                binding.selectedDateText.text = selectedDate
            }, year, month, day).show()
        }
    }

    private fun setupRecyclerView() {
        goalAdapter = GoalAdapter(emptyList())
        binding.goalsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.goalsRecyclerView.adapter = goalAdapter
    }

    private fun setupAddGoalButton() {
        binding.btnAddGoal.setOnClickListener {
            val name = binding.inputGoalName.text.toString().trim()
            val amountStr = binding.inputGoalAmount.text.toString().trim()

            if (name.isEmpty() || amountStr.isEmpty() || selectedDate.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountStr.toIntOrNull()
            if (amount == null) {
                Toast.makeText(requireContext(), "Enter a valid amount.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create new goal and insert
            val goal = Goal(
                name = name,
                amount = amount,
                targetDate = selectedDate,
                minContribution = minContribution,
                maxContribution = maxContribution
            )

            viewModel.insertGoal(goal)
            clearInputs()
        }
    }

    private fun clearInputs() {
        minContribution = seekBarMinLimit
        maxContribution = seekBarMaxLimit
        binding.seekBarMin.progress = minContribution - seekBarMinLimit
        binding.seekBarMax.progress = maxContribution - seekBarMinLimit
        binding.inputGoalName.text?.clear()
        binding.inputGoalAmount.text?.clear()
        binding.selectedDateText.text = ""
        selectedDate = ""
        updateRangeText()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
