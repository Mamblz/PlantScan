package com.example.plantscan.Presentation.ViewModels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.domain.utils.SupabaseConfig
import com.example.plantscan.Domain.State.SignUpState
import com.example.plantscan.apiconnect.model.Profile
import com.example.plantscan.apiconnect.model.User
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.security.MessageDigest

sealed class ResultState {
    object Initialized : ResultState()
    object Loading : ResultState()
    data class Success(
        val message: String,
        val readyToNavigate: Boolean = false
    ) : ResultState()
    data class Error(val message: String) : ResultState()
}

class SignUpViewModel : ViewModel() {

    private val _uiState = mutableStateOf(SignUpState())
    val uiState: State<SignUpState> = _uiState

    private val _resultState = MutableStateFlow<ResultState>(ResultState.Initialized)
    val resultState: StateFlow<ResultState> = _resultState.asStateFlow()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) { json(json) }
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun generateRecoveryCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..8).map { chars.random() }.joinToString("")
    }

    private fun updateState(update: (SignUpState) -> SignUpState) {
        _uiState.value = update(_uiState.value)
        _resultState.value = ResultState.Initialized
    }

    fun onFirstNameChange(value: String) = updateState { it.copy(firstName = value) }
    fun onLastNameChange(value: String) = updateState { it.copy(lastName = value) }
    fun onEmailChange(value: String) = updateState { it.copy(email = value) }

    fun onPhoneChange(value: String) {
        val digits = value.filter { it.isDigit() }
        val trimmed = digits.take(12)
        updateState { it.copy(phone = trimmed) }
    }

    fun onPasswordChange(value: String) = updateState { it.copy(password = value) }
    fun onConfirmPasswordChange(value: String) =
        updateState { it.copy(confirmPassword = value) }

    private fun isPasswordValid(password: String): Boolean {
        val regex = Regex("^(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{6,}\$")
        return regex.matches(password)
    }

    private fun isPhoneValid(phone: String): Boolean {
        val digits = phone.filter { it.isDigit() }
        return digits.length in 10..12
    }

    fun signUp() {
        val state = _uiState.value

        if (state.firstName.isBlank()) {
            _resultState.value = ResultState.Error("Введите имя")
            return
        }
        if (state.lastName.isBlank()) {
            _resultState.value = ResultState.Error("Введите фамилию")
            return
        }
        if (state.email.isBlank()) {
            _resultState.value = ResultState.Error("Введите email")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _resultState.value = ResultState.Error("Неверный формат email")
            return
        }
        if (!isPhoneValid(state.phone)) {
            _resultState.value = ResultState.Error("Неверный номер телефона")
            return
        }
        if (!isPasswordValid(state.password)) {
            _resultState.value = ResultState.Error("Некорректный пароль")
            return
        }
        if (state.password != state.confirmPassword) {
            _resultState.value = ResultState.Error("Пароли не совпадают")
            return
        }

        _resultState.value = ResultState.Loading

        viewModelScope.launch {
            try {
                val checkEmail = client.get("${SupabaseConfig.BASE_URL_REST}/users") {
                    header("apikey", SupabaseConfig.API_KEY)
                    header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
                    parameter("email", "eq.${state.email}")
                    parameter("select", "id")
                }
                val emailExists: List<User> =
                    if (checkEmail.status == HttpStatusCode.OK)
                        json.decodeFromString(checkEmail.bodyAsText())
                    else emptyList()

                if (emailExists.isNotEmpty()) {
                    _resultState.value = ResultState.Error("Этот email уже зарегистрирован")
                    return@launch
                }

                val checkPhone = client.get("${SupabaseConfig.BASE_URL_REST}/profiles") {
                    header("apikey", SupabaseConfig.API_KEY)
                    header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
                    parameter("phone", "eq.${state.phone}")
                    parameter("select", "user_id")
                }
                val phoneExists: List<Profile> =
                    if (checkPhone.status == HttpStatusCode.OK)
                        json.decodeFromString(checkPhone.bodyAsText())
                    else emptyList()

                if (phoneExists.isNotEmpty()) {
                    _resultState.value = ResultState.Error("Этот номер телефона уже зарегистрирован")
                    return@launch
                }

                val passwordHash = hashPassword(state.password)
                val userResponse = client.post("${SupabaseConfig.BASE_URL_REST}/users") {
                    header("apikey", SupabaseConfig.API_KEY)
                    header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
                    header("Prefer", "return=representation")
                    contentType(ContentType.Application.Json)
                    setBody(
                        mapOf(
                            "email" to state.email,
                            "password_hash" to passwordHash
                        )
                    )
                }
                if (userResponse.status != HttpStatusCode.Created) {
                    _resultState.value = ResultState.Error("Ошибка сервера")
                    return@launch
                }

                val users: List<User> =
                    json.decodeFromString(userResponse.bodyAsText())
                val createdUser = users.firstOrNull()
                    ?: return@launch.also {
                        _resultState.value = ResultState.Error("Пользователь не найден")
                    }

                val recoveryCode = generateRecoveryCode()
                val profileResponse =
                    client.post("${SupabaseConfig.BASE_URL_REST}/profiles") {
                        header("apikey", SupabaseConfig.API_KEY)
                        header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
                        header("Prefer", "return=representation")
                        contentType(ContentType.Application.Json)
                        setBody(
                            Profile(
                                user_id = createdUser.id!!,
                                first_name = state.firstName,
                                last_name = state.lastName,
                                phone = state.phone,
                                recovery_code = recoveryCode
                            )
                        )
                    }

                if (profileResponse.status != HttpStatusCode.Created) {
                    _resultState.value = ResultState.Error("Ошибка при создании профиля")
                    return@launch
                }

                _resultState.value = ResultState.Success(
                    "Регистрация успешна!\nВаш код восстановления: $recoveryCode",
                    readyToNavigate = false
                )

                delay(20_000)

                _resultState.value = ResultState.Success(
                    "Регистрация успешна!\nВаш код восстановления: $recoveryCode",
                    readyToNavigate = true
                )

            } catch (e: Exception) {
                _resultState.value = ResultState.Error("Ошибка: ${e.message}")
            }
        }
    }
}