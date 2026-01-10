package com.example.plantscan.Presentation.ViewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.domain.utils.SupabaseConfig
import com.example.plantscan.api.UserRepository
import com.example.plantscan.apiconnect.model.Profile
import com.example.plantscan.apiconnect.model.User
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

data class PersonalDataUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val emailError: String? = null,
    val phoneError: String? = null
)

class PersonalDataViewModel : ViewModel() {

    private val repository = UserRepository()
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    var uiState = mutableStateOf(PersonalDataUiState())
        private set

    sealed class Result {
        object Idle : Result()
        object Loading : Result()
        data class Success(val message: String) : Result()
        data class Error(val message: String) : Result()
    }

    private val _resultState = MutableStateFlow<Result>(Result.Idle)
    val resultState: StateFlow<Result> = _resultState

    init {
        loadUserData()
    }

    fun updateState(newState: PersonalDataUiState) {
        val emailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(newState.email).matches()
        val emailError =
            if (newState.email.isNotBlank() && !emailValid) "Неверный формат email" else null

        val phoneValid =
            newState.phone.isEmpty() || (newState.phone.length in 10..12 && newState.phone.all { it.isDigit() || it == '+' })
        val phoneError = if (!phoneValid) "Неверный формат номера" else null

        uiState.value = newState.copy(emailError = emailError, phoneError = phoneError)
    }

    fun loadUserData() {
        viewModelScope.launch {
            try {
                val userId = com.example.plantscan.apiconnect.model.UserSession.currentUserId
                val user = repository.getUsers().find { it.id == userId }
                val profile = repository.getProfileByUserId(userId)

                if (user != null) {
                    uiState.value = PersonalDataUiState(
                        firstName = profile?.first_name ?: "",
                        lastName = profile?.last_name ?: "",
                        email = user.email ?: "",
                        phone = profile?.phone ?: ""
                    )
                } else {
                    _resultState.value = Result.Error("Не удалось загрузить данные пользователя")
                }
            } catch (e: Exception) {
                _resultState.value = Result.Error("Ошибка загрузки: ${e.message}")
            }
        }
    }

    fun saveChanges() {
        val stateValue = uiState.value

        if (!stateValue.emailError.isNullOrEmpty() || !stateValue.phoneError.isNullOrEmpty()) {
            _resultState.value = Result.Error("Исправьте ошибки в полях")
            return
        }

        _resultState.value = Result.Loading

        viewModelScope.launch {
            try {
                val userId = com.example.plantscan.apiconnect.model.UserSession.currentUserId

                val checkEmail = client.get("${SupabaseConfig.BASE_URL_REST}/users") {
                    header("apikey", SupabaseConfig.API_KEY)
                    header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
                    parameter("email", "eq.${stateValue.email}")
                    parameter("select", "id")
                }
                val emailExists: List<User> =
                    if (checkEmail.status == HttpStatusCode.OK)
                        Json.decodeFromString(checkEmail.bodyAsText())
                    else emptyList()
                if (emailExists.any { it.id != userId }) {
                    _resultState.value = Result.Error("Этот email уже зарегистрирован")
                    return@launch
                }

                val checkPhone = client.get("${SupabaseConfig.BASE_URL_REST}/profiles") {
                    header("apikey", SupabaseConfig.API_KEY)
                    header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
                    parameter("phone", "eq.${stateValue.phone}")
                    parameter("select", "user_id")
                }
                val phoneExists: List<Profile> =
                    if (checkPhone.status == HttpStatusCode.OK)
                        Json.decodeFromString(checkPhone.bodyAsText())
                    else emptyList()
                if (phoneExists.any { it.user_id != userId }) {
                    _resultState.value = Result.Error("Этот номер телефона уже зарегистрирован")
                    return@launch
                }

                val userSuccess = repository.updateUser(
                    userId = userId,
                    email = stateValue.email
                )

                val profileSuccess = repository.updateProfile(
                    userId = userId,
                    firstName = stateValue.firstName,
                    lastName = stateValue.lastName,
                    phone = stateValue.phone
                )

                if (userSuccess && profileSuccess) {
                    _resultState.value = Result.Success("Изменения сохранены")
                } else {
                    _resultState.value = Result.Error("Ошибка при сохранении данных")
                }

            } catch (e: Exception) {
                _resultState.value = Result.Error("Ошибка: ${e.message}")
            }
        }
    }
}
