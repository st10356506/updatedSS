/*Android Studio (2019). Save data in a local database using Room | Android Developers. [online] Android Developers. Available at: https://developer.android.com/training/data-storage/room.
(Android Studio, 2019)*/

package com.example.spendsense20

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.liveData
import kotlinx.coroutines.launch

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val financeDao: FinanceDao = FinanceDB.getDatabase(application).FinanceDao()

    // Insert a new finance entry
    fun insertFinance(finance: FinanceEntity) {
        viewModelScope.launch {
            financeDao.insertFinance(finance)
        }
    }

    // Get data for a selected date
    fun getFinanceByDate(selectedDate: String): LiveData<List<FinanceEntity>> {
        return financeDao.getFinanceByDate(selectedDate)
    }

    fun getCategoryTotals(startDate: String, endDate: String): LiveData<List<CategorySummary>> {
        return liveData {
            val categoryTotals = financeDao.getCategoryTotalsBetweenDates(startDate, endDate)
            emit(categoryTotals)
        }
    }
}