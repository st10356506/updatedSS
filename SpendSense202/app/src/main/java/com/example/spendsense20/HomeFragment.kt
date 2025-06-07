package com.example.spendsense20

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.formatter.PercentFormatter


class HomeFragment : Fragment() {

    private lateinit var databaseRef: DatabaseReference
    private var userId: String? = null

    // UI Elements
    private lateinit var progressBarFill: View
    private lateinit var tvFrequentCategory: TextView
    private lateinit var tvPointsEarned: TextView
    private lateinit var tvUserRank: TextView
    private lateinit var tvProgressText: TextView
    private lateinit var totalBalanceTextView: TextView
    private lateinit var tvNextRankLabel: TextView
    private lateinit var ivRankImage : ImageView
    private lateinit var konfettiView : KonfettiView
    private lateinit var pieChart : PieChart
    private lateinit var btnViewGoalsProgress : Button
    private lateinit var btnBack : Button
    private lateinit var btnShowBarGraph:Button

    private val goal = 15000.0

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val btnShowBarGraph = view.findViewById<Button>(R.id.btnShowBarGraph)

        btnShowBarGraph.setOnClickListener {
            val intent = android.content.Intent(requireContext(), ExpenseBarGraphActivity::class.java)
            startActivity(intent)
        }

        userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return view
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("finances").child(userId!!)

        // Bind views
        progressBarFill = view.findViewById(R.id.progressBarFill)
        tvFrequentCategory = view.findViewById(R.id.tvFrequentCategory)
        tvPointsEarned = view.findViewById(R.id.tvPointsEarned)
        tvUserRank = view.findViewById(R.id.tvUserRank)
        tvProgressText = view.findViewById(R.id.tvProgressText)
        totalBalanceTextView = view.findViewById(R.id.totalBalanceTextView)
        tvNextRankLabel = view.findViewById(R.id.tvNextRankLabel)
        ivRankImage = view.findViewById(R.id.ivRankImage)
        konfettiView = view.findViewById(R.id.konfettiView)
        btnViewGoalsProgress = view.findViewById(R.id.btnViewGoalsProgress)
        val ivInfoRank = view.findViewById<ImageView>(R.id.questionIcon)
        ivInfoRank.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("How Ranking Works")
                .setMessage(
                    "Your rank is based on the number of points earned while capturing income. Expenses remove points.\n" +
                            "Points are earned by budgeting wisely!"
                )
                .setPositiveButton("OK", null)
                .show()
        }
        pieChart = view.findViewById(R.id.pieChart)

        ivRankImage.setOnClickListener {
            konfettiView.visibility = View.VISIBLE
            konfettiView.start(
                Party(
                    speed = 30f,
                    maxSpeed = 50f,
                    damping = 0.8f,
                    spread = 360,
                    colors = listOf(
                        0xFF006400.toInt(), // Dark green
                        0xFF228B22.toInt(), // Forest green
                        0xFF32CD32.toInt(), // Lime green
                        0xFF7CFC00.toInt()  // Lawn green
                    ),
                    emitter = Emitter(duration = 2, TimeUnit.SECONDS).perSecond(40),
                    position = Position.Relative(0.5, 0.3)
                )
            )
        }

        btnViewGoalsProgress.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, GProgressFragment())
                .addToBackStack(null)
                .commit()
        }

        listenToFinanceChanges()
        return view
    }

    private fun listenToFinanceChanges() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val finances = mutableListOf<FinanceEntity>()
                for (child in snapshot.children) {
                    val entity = child.getValue(FinanceEntity::class.java)
                    if (entity != null) {
                        finances.add(entity)
                    }
                }
                updateUI(finances)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragment", "Database error: ${error.message}")
            }
        })
    }
    private fun updateUI(finances: List<FinanceEntity>) {
        val income = finances.filter { it.type == "income" }.sumOf { it.amount }
        val expenses = finances.filter { it.type == "expense" }.sumOf { it.amount }
        val balance = income - expenses

        totalBalanceTextView.text = "R %.2f".format(balance)

        val points = (balance / 100).toInt().coerceAtLeast(0)
        tvPointsEarned.text = "$points pts"

        val (currentRank, nextThreshold, nextRank) = when {
            points >= 800 -> Triple("Platinum", null, "Max Rank Achieved")
            points >= 500 -> Triple("Emerald", 800, "Platinum")
            points >= 250 -> Triple("Gold", 500, "Emerald")
            points >= 100 -> Triple("Silver", 250, "Gold")
            else -> Triple("Bronze", 100, "Silver")
        }

        tvUserRank.text = currentRank
        tvNextRankLabel.text = nextThreshold?.let { "Progress to $nextRank" } ?: "Max Rank Achieved"

        val lowerBound = when (currentRank) {
            "Bronze" -> 0
            "Silver" -> 100
            "Gold" -> 250
            "Emerald" -> 500
            "Platinum" -> 800
            else -> 0
        }

        val upperBound = nextThreshold ?: lowerBound
        val range = (upperBound - lowerBound).coerceAtLeast(1)
        val progressRatio = ((points - lowerBound).toDouble() / range).coerceIn(0.0, 1.0)

        progressBarFill.post {
            val parentWidth = (progressBarFill.parent as View).width
            val newWidth = (parentWidth * progressRatio).toInt()
            val params = progressBarFill.layoutParams
            params.width = newWidth
            progressBarFill.layoutParams = params
        }

        tvProgressText.text = nextThreshold?.let {
            "${(progressRatio * 100).toInt()}% to $nextRank"
        } ?: "Rank Maxed!"

        val rankDrawableRes = when (currentRank) {
            "Bronze" -> R.drawable.bronze
            "Silver" -> R.drawable.silver
            "Gold" -> R.drawable.gold
            "Emerald" -> R.drawable.emerald
            "Platinum" -> R.drawable.platinum
            else -> R.drawable.bronze
        }
        ivRankImage.setImageResource(rankDrawableRes)

        // Show most frequent expense category
        val categoryMap = finances.groupingBy { it.name }.eachCount()
        val mostFrequent = categoryMap.maxByOrNull { it.value }?.key ?: "None"
        tvFrequentCategory.text = mostFrequent

        // Only use expense entries for pie chart
        val expenseCategories = finances
            .filter { it.type == "expense" }
            .groupingBy { it.name }
            .fold(0.0) { acc, item -> acc + item.amount }

        if (expenseCategories.isNotEmpty()) {
            val entries = expenseCategories.map { (category, amount) ->
                PieEntry(amount.toFloat(), category)
            }

            val dataSet = PieDataSet(entries, "")
            dataSet.colors = listOf(
                android.graphics.Color.parseColor("#A7D1AB"),
                android.graphics.Color.parseColor("#8FBC8F"),
                android.graphics.Color.parseColor("#66CDAA"),
                android.graphics.Color.parseColor("#458B74"),
                android.graphics.Color.parseColor("#2E8B57"),
                android.graphics.Color.parseColor("#D25BF8")
            )
            dataSet.sliceSpace = 3f
            dataSet.valueTextSize = 12f
            dataSet.setDrawValues(true)

            val data = PieData(dataSet)
            data.setValueFormatter(PercentFormatter(pieChart)) //show % only
            pieChart.data = data
            pieChart.description.isEnabled = false
            pieChart.centerText = "Spending Breakdown"
            pieChart.setDrawEntryLabels(false)
            pieChart.setUsePercentValues(true)
            pieChart.animateY(1000)
            pieChart.invalidate()
            data.setValueFormatter(PercentFormatter(pieChart))
            data.setValueTextSize(12f)

            val legend = pieChart.legend
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            legend.setDrawInside(false)
            legend.isWordWrapEnabled = true
            legend.textSize = 10f
            legend.xEntrySpace = 12f
            legend.yEntrySpace = 15f
            legend.isWordWrapEnabled = true

        } else {
            val emptyEntries = listOf(PieEntry(1f, "No Data"))

            val emptyDataSet = PieDataSet(emptyEntries, "")
            emptyDataSet.colors = listOf(android.graphics.Color.parseColor("#a9a9a9"))
            emptyDataSet.valueTextSize = 0f
            emptyDataSet.setDrawValues(false)

            val emptyData = PieData(emptyDataSet)

            pieChart.data = emptyData
            pieChart.setUsePercentValues(false)
            pieChart.description.isEnabled = false
            pieChart.centerText = "No expense data"
            pieChart.setDrawEntryLabels(false)
            pieChart.animateY(1000)
            pieChart.invalidate()

            val legend = pieChart.legend
            legend.isEnabled = false
        }

    }
}