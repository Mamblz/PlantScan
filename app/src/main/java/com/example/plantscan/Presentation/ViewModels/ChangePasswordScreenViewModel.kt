package com.example.plantscan.Presentation.ViewModels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantscan.api.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChangePasswordUiState(
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val oldPasswordError: String? = null,
    val newPasswordError: String? = null
)

class ChangePasswordViewModel : ViewModel() {

    private val repository = UserRepository()

    var uiState = mutableStateOf(ChangePasswordUiState())
        private set

    private val _resultState = MutableStateFlow<Result>(Result.Idle)
    val resultState: StateFlow<Result> = _resultState

    fun updateState(newState: ChangePasswordUiState) {
        val newPasswordError = when {
            newState.newPassword.isBlank() -> "Введите новый пароль"
            newState.newPassword.length < 6 -> "Пароль должен быть минимум 6 символов"
            else -> null
        }

        val oldPasswordError = if (newState.oldPassword.isBlank()) "Введите старый пароль" else null

        uiState.value = newState.copy(
            oldPasswordError = oldPasswordError,
            newPasswordError = newPasswordError
        )
    }

    fun changePassword() {
        val stateValue = uiState.value

        if (!stateValue.oldPasswordError.isNullOrEmpty() ||
            !stateValue.newPasswordError.isNullOrEmpty() ||
            stateValue.newPassword != stateValue.confirmPassword
        ) {
            if (stateValue.newPassword != stateValue.confirmPassword) {
                uiState.value = stateValue.copy(
                    oldPasswordError = stateValue.oldPasswordError,
                    newPasswordError = "Пароли не совпадают"
                )
            }
            return
        }

        _resultState.value = Result.Loading

        viewModelScope.launch {
            try {
                val userId = com.example.plantscan.apiconnect.model.UserSession.currentUserId

                val success = repository.updateUserPassword(
                    userId = userId,
                    oldPassword = stateValue.oldPassword,
                    newPassword = stateValue.newPassword
                )

                if (success) {
                    _resultState.value = Result.Success("Пароль успешно изменен")
                    uiState.value = ChangePasswordUiState()
                } else {
                    _resultState.value = Result.Error("Старый пароль введен неверно")
                }
            } catch (e: Exception) {
                _resultState.value = Result.Error("Ошибка: ${e.message}")
            }
        }
    }

    sealed class Result {
        object Idle : Result()
        object Loading : Result()
        data class Success(val message: String) : Result()
        data class Error(val message: String) : Result()
    }
}
