package com.example.greenaura

data class ReminderModel(
    val plantName: String,
    val task: String,
    val frequency: String,
    val time: String,
    val selectedDays: List<Int>,  // Days represented as integers (1 for Sunday, 7 for Saturday)
    val isActive: Boolean
)

