package com.example.plantscan.Presentation.ViewModels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.domain.state.ResultState
import com.example.app.domain.state.SignInState
import com.example.plantscan.api.UserRepository
import com.example.plantscan.apiconnect.model.User
import com.example.plantscan.apiconnect.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest

class SignInViewModel : ViewModel() {

    private val repository = UserRepository()
    private val _navigateToMain = MutableStateFlow(false)
    val navigateToMain: StateFlow<Boolean> = _navigateToMain.asStateFlow()

    private val _uiState = mutableStateOf(SignInState())
    val uiState: State<SignInState> = _uiState

    private val _resultState = MutableStateFlow<ResultState>(ResultState.Initialized)
    val resultState: StateFlow<ResultState> = _resultState.asStateFlow()

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private suspend fun findUserByEmail(email: String): User? {
        return try {
            repository.getUserByEmail(email.trim().lowercase())
        } catch (e: Exception) {
            null
        }
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun signIn(email: String, password: String, onSuccess: () -> Unit = {}) {
        if (email.isBlank()) {
            _resultState.value = ResultState.Error("Введите email")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _resultState.value = ResultState.Error("Неверный формат email")
            return
        }

        if (password.isBlank()) {
            _resultState.value = ResultState.Error("Введите пароль")
            return
        }

        _resultState.value = ResultState.Loading

        viewModelScope.launch {
            try {
                val user = findUserByEmail(email)

                when {
                    user == null -> {
                        _resultState.value = ResultState.Error("Пользователь с таким email не найден")
                    }
                    user.password_hash != hashPassword(password) -> {
                        _resultState.value = ResultState.Error("Неверный пароль")
                    }
                    else -> {
                        UserSession.currentEmail = user.email ?: ""
                        UserSession.currentUserId = user.id ?: ""

                        _resultState.value = ResultState.Success("Успешный вход")
                        _navigateToMain.value = true
                        onSuccess()
                    }
                }
            } catch (e: Exception) {
                _resultState.value = ResultState.Error("Ошибка сети: ${e.message}")
            }
        }
    }

    fun resetState() {
        _resultState.value = ResultState.Initialized
        _uiState.value = SignInState()
    }

    fun resetNavigation() {
        _navigateToMain.value = false
    }
}