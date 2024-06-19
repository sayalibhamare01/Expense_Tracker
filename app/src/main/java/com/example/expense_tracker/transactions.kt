package com.example.expense_tracker

data class Transaction(
    val name: String = "",
    val amount: Double = 0.0,
    val dateTime: String = "",
    val transactionType: String = "",
    val paymentMethod: String = "",
    val expenseCategory: String = "",
)
