package com.example.app.domain.state

sealed class ResultState {
    object Initialized : ResultState()
    object Idle : ResultState()
    object Loading : ResultState()
    data class Success(val message: String) : ResultState()
    data class Error(val message: String) : ResultState()
}