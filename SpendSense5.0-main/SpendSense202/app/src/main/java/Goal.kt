package com.example.spendsense20.data

data class Goal(
    var name: String = "",
    var amount: Int = 0,
    var targetDate: String = "",
    var minContribution: Int = 0,
    var maxContribution: Int = 0
)
