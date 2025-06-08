package com.example.spendsense20.data
//goals variables
data class Goal(
    var id: String="",
    var category: String ="",
    var startDate: String = "",
    var endDate: String = "",
    var minContribution: Int = 0,
    var maxContribution: Int = 0
)
