package com.example.plantscan.Presentation.ViewModels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

data class PlantHistoryItem(
    val id: String,
    val photoUrl: String?,
    val name: String,
    val dateAdded: String
)

class HistoryViewModel : ViewModel() {
    val historyList = mutableStateListOf<PlantHistoryItem>()

    init {
        historyList.addAll(
            listOf(
                PlantHistoryItem("1", null, "Роза", "2025-10-30"),
                PlantHistoryItem("2", null, "Тюльпан", "2025-10-29"),
                PlantHistoryItem("3", null, "Орхидея", "2025-10-28")
            )
        )
    }

    fun addItem(item: PlantHistoryItem) {
        historyList.add(0, item)
    }

    fun clearHistory() {
        historyList.clear()
    }
}
