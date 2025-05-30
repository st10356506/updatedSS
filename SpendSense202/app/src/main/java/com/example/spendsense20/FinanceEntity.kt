package com.example.spendsense20

data class FinanceEntity(
    var id: String = "", // Firebase document key
    var amount: Double = 0.0,
    var name: String = "",
    var description: String = "",
    var type: String = "", // "income" or "expense"
    var date: String = "",
    var imageUri: String? = null,
    var userEmail: String = ""
)