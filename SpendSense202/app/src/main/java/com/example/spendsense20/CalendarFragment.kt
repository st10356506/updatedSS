/*GeeksforGeeks (2019a). Android | Creating a Calendar View app. [online] GeeksforGeeks. Available at: https://www.geeksforgeeks.org/android-creating-a-calendar-view-app/.
(GeeksforGeeks, 2019a)*/

package com.example.spendsense20

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendsense20.databinding.FragmentCalendarBinding
import kotlinx.coroutines.launch
import java.util.Calendar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CalendarFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CalendarFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var financeDao: FinanceDao
    private lateinit var binding: FragmentCalendarBinding
    private lateinit var financeAdapter: FinanceAdapter
    private lateinit var financeViewModel: FinanceViewModel
    private var selectedDate: String = "" // this will be updated when the user selects a date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCalendarBinding.inflate(inflater, container, false)

        // Initialize ViewModel
        financeViewModel = ViewModelProvider(this).get(FinanceViewModel::class.java)

        // Initialize RecyclerView and adapter
        financeAdapter = FinanceAdapter(
            onImageClick = { imageUri ->
                val intent = Intent(requireContext(), ImageViewActivity::class.java)
                intent.putExtra("imageUri", imageUri)
                startActivity(intent)
            },
            onDeleteClick = { finance ->
                lifecycleScope.launch {
                    financeDao.deleteFinance(finance.id)
                    val updatedList = financeDao.getAllFinances()
                    financeAdapter.submitList(updatedList)
                }
            }
        )
        binding.transactionsRecyclerView.adapter = financeAdapter
        binding.transactionsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.btnAddFinance.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .add(R.id.wholePage, AddFragment())
                .addToBackStack(null)
                .commit()
            Toast.makeText(requireContext(), "Opened Finance Page", Toast.LENGTH_LONG).show()
        }


        // Set up the CalendarView
        setupCalendarView()

        //get current date
        selectedDate = getCurrentDate()

        // Observe LiveData from ViewModel for the selected date
        loadFinanceDataForSelectedDate()

        return binding.root
    }

    private fun setupCalendarView() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Format the selected date properly (e.g., 2025-04-28)
            val formattedMonth = (month + 1).toString().padStart(2, '0')
            val formattedDay = dayOfMonth.toString().padStart(2, '0')
            selectedDate = "$year-$formattedMonth-$formattedDay"

            // Load transactions for the selected date
            loadFinanceDataForSelectedDate()
        }
    }

    private fun loadFinanceDataForSelectedDate() {
        financeViewModel.getFinanceByDate(selectedDate).observe(viewLifecycleOwner) { finances ->
            financeAdapter.submitList(finances)

            if (finances.isNullOrEmpty()) {
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.transactionsRecyclerView.visibility = View.GONE
            } else {
                binding.emptyStateLayout.visibility = View.GONE
                binding.transactionsRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val day = calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        return "$year-$month-$day"
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CalendarFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
