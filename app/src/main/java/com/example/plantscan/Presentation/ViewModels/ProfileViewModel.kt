package com.example.plantscan.Presentation.ViewModels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantscan.api.UserRepository
import com.example.plantscan.apiconnect.model.Profile
import kotlinx.coroutines.launch

open class ProfileViewModel : ViewModel() {
    private val repository = UserRepository()

    open var fullName by mutableStateOf("")
        private set
    open var email by mutableStateOf("")
        private set
    open var phone by mutableStateOf("")
        private set

    open var isLoading by mutableStateOf(false)
        private set
    open var error by mutableStateOf<String?>(null)
        private set

    fun loadUser(userEmail: String) {
        isLoading = true
        error = null

        viewModelScope.launch {
            try {
                val user = repository.getUserByEmail(userEmail)
                if (user != null) {
                    email = user.email ?: ""
                    val profile: Profile? = repository.getProfileByUserId(user.id ?: "")
                    fullName = listOf(profile?.first_name, profile?.last_name).filterNotNull().joinToString(" ")
                    phone = profile?.phone ?: ""
                } else {
                    error = "Пользователь не найден"
                }
            } catch (e: Exception) {
                error = "Ошибка: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun logout() {
        fullName = ""
        email = ""
        phone = ""
    }
}
