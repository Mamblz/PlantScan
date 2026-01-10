package com.example.plantscan.Domain.Utils

sealed class ResultState {
    data object Loading : ResultState()
    data object Initialized : ResultState()
    data class Success(val message: String = "") : ResultState()
    data class Error(val message: String) : ResultState()
}