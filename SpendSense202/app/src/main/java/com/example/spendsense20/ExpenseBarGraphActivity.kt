package com.example.spendsense20

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.spendsense20.data.Goal
import com.example.spendsense20.viewmodel.GoalViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ExpenseBarGraphActivity : AppCompatActivity() {

    private lateinit var btnStartDate: Button
    private lateinit var btnEndDate: Button
    private lateinit var btnLoadData: Button
    private lateinit var barChart: BarChart

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private var startDate: Date = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }.time

    private var endDate: Date = Calendar.getInstance().time

    private lateinit var databaseRef: DatabaseReference
    private lateinit var viewModel: GoalViewModel

    private val categories = listOf("Food", "Transport", "Bills", "Shopping", "Other")
    private val expenseSums = mutableMapOf<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_bar_graph)

        btnStartDate = findViewById(R.id.btnStartDate)
        btnEndDate = findViewById(R.id.btnEndDate)
        btnLoadData = findViewById(R.id.btnLoadData)
        barChart = findViewById(R.id.barChart)

        viewModel = ViewModelProvider(this)[GoalViewModel::class.java]

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        databaseRef = FirebaseDatabase.getInstance().getReference("finances").child(currentUser.uid)

        btnStartDate.text = "Start: ${dateFormat.format(startDate)}"
        btnEndDate.text = "End: ${dateFormat.format(endDate)}"
        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
        btnStartDate.setOnClickListener { pickDate(true) }
        btnEndDate.setOnClickListener { pickDate(false) }
        btnLoadData.setOnClickListener {
            if (startDate.after(endDate)) {
                Toast.makeText(this, "Start date must be before end date", Toast.LENGTH_SHORT).show()
            } else {
                loadExpensesForPeriod(startDate, endDate)
            }
        }

        setupChart()

        // Load goals once, to avoid multiple LiveData observers on each data load
        viewModel.allGoals.observe(this) { goalWithIdList ->
            displayChart(goalWithIdList.map { it.goal })
        }
    }

    private fun pickDate(isStart: Boolean) {
        val calendar = Calendar.getInstance()
        calendar.time = if (isStart) startDate else endDate

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                if (isStart) {
                    startDate = calendar.time
                    btnStartDate.text = "Start: ${dateFormat.format(startDate)}"
                } else {
                    endDate = calendar.time
                    btnEndDate.text = "End: ${dateFormat.format(endDate)}"
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadExpensesForPeriod(startDate: Date, endDate: Date) {

        expenseSums.clear()
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
                    } ?: continue

                    if (!entryDate.before(startDate) && !entryDate.after(endDate)) {
                        // Normalize category name to match categories list
                        val categoryRaw = finance.name.trim()
                        val category = categories.find { it.equals(categoryRaw, ignoreCase = true) } ?: "Other"
                        expenseSums[category] = (expenseSums[category] ?: 0) + finance.amount.toInt()

                    }
                }
                // Trigger redraw with updated data
                viewModel.allGoals.value?.let { displayChart(it.map { it.goal }) }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ExpenseBarGraphActivity, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayChart(goals: List<Goal>) {
        // Filter goals active in the selected date range
        val filteredGoals = goals.filter { goal ->
            try {
                val goalStart = dateFormat.parse(goal.startDate) ?: startDate
                val goalEnd = dateFormat.parse(goal.endDate) ?: endDate
                !(goalEnd.before(startDate) || goalStart.after(endDate)) // Overlapping periods only
            } catch (e: Exception) {
                true
            }
        }

        val entriesMin = ArrayList<BarEntry>()
        val entriesActual = ArrayList<BarEntry>()
        val entriesMax = ArrayList<BarEntry>()

        categories.forEachIndexed { index, category ->
            val categoryGoals = filteredGoals.filter { goal ->
                val goalCategory = goal.category.trim()
                val matchedCategory = categories.find { it.equals(goalCategory, ignoreCase = true) } ?: "Other"
                matchedCategory.equals(category, ignoreCase = true)
            }

            val minSum = categoryGoals.sumOf { it.minContribution.toInt() }
            val maxSum = categoryGoals.sumOf { it.maxContribution.toInt() }
            val actualSum = expenseSums[category] ?: 0

            entriesMin.add(BarEntry(index.toFloat(), minSum.toFloat()))
            entriesActual.add(BarEntry(index.toFloat(), actualSum.toFloat()))
            entriesMax.add(BarEntry(index.toFloat(), maxSum.toFloat()))
        }

        val setMin = BarDataSet(entriesMin, "Min").apply {
            color = Color.parseColor("#4A90E2")
            valueTextSize = 10f
        }
        val setActual = BarDataSet(entriesActual, "Actual").apply {
            color = Color.parseColor("#7ED321")
            valueTextSize = 10f
        }
        val setMax = BarDataSet(entriesMax, "Max").apply {
            color = Color.parseColor("#D0021B")
            valueTextSize = 10f
        }

        val barData = BarData(setMin, setActual, setMax)

        val groupSpace = 0.1f
        val barSpace = 0.05f
        val barWidth = 0.25f
        val groupCount = categories.size

        barData.barWidth = barWidth

        barChart.data = barData

        val startX = 0f

        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(categories)
            granularity = 1f
            isGranularityEnabled = true
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            textSize = 12f

            setCenterAxisLabels(true)

            axisMinimum = startX
            axisMaximum = startX + barData.getGroupWidth(groupSpace, barSpace) * groupCount
            labelCount = groupCount

            xOffset = 0f
            spaceMin = 0f
            spaceMax = 0f
        }

        barChart.axisLeft.apply {
            axisMinimum = 0f
            textSize = 12f
        }

        barChart.axisRight.isEnabled = false

        barChart.legend.apply {
            isEnabled = true
            textSize = 12f
        }

        barChart.description.isEnabled = false

        // Group bars after axis min/max setup
        barChart.groupBars(startX, groupSpace, barSpace)

        barChart.invalidate()
    }

    private fun setupChart() {
        barChart.apply {
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            description.isEnabled = false
            legend.isWordWrapEnabled = true
            legend.textSize = 12f
            setPinchZoom(true)
            setScaleEnabled(true)
            animateY(800)
            setExtraBottomOffset(20f)
        }
    }
}
