package com.example.plantscan.Presentation.Screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.app.domain.utils.SupabaseConfig
import com.example.plantscan.apiconnect.model.User
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.security.MessageDigest

class ForgotPasswordViewModel : ViewModel() {

    var email by mutableStateOf("")
    var recoveryCode by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    private val json = Json { ignoreUnknownKeys = true }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) { json(json) }
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun resetPassword() {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage = "Введите корректный email"
            return
        }
        if (recoveryCode.isBlank()) {
            errorMessage = "Введите код восстановления"
            return
        }
        if (newPassword.length < 6) {
            errorMessage = "Пароль должен быть не менее 6 символов"
            return
        }
        if (newPassword != confirmPassword) {
            errorMessage = "Пароли не совпадают"
            return
        }

        errorMessage = null
        successMessage = null
        isLoading = true

        viewModelScope.launch {
            try {
                val usersResponse = client.get("${SupabaseConfig.BASE_URL_REST}/users") {
                    header("apikey", SupabaseConfig.API_KEY)
                    header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
                    parameter("email", "eq.$email")
                    parameter("select", "id")
                }

                if (usersResponse.status != HttpStatusCode.OK) {
                    errorMessage = "Ошибка подключения к серверу"
                    isLoading = false
                    return@launch
                }

                val users: List<User> = json.decodeFromString(usersResponse.bodyAsText())
                val user = users.firstOrNull()
                if (user == null) {
                    errorMessage = "Пользователь с таким email не найден"
                    isLoading = false
                    return@launch
                }

                val profileResponse = client.get("${SupabaseConfig.BASE_URL_REST}/profiles") {
                    header("apikey", SupabaseConfig.API_KEY)
                    header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
                    parameter("recovery_code", "eq.$recoveryCode")
                    parameter("select", "user_id")
                }

                if (profileResponse.status != HttpStatusCode.OK) {
                    errorMessage = "Ошибка подключения к серверу"
                    isLoading = false
                    return@launch
                }

                val profilesJson = profileResponse.bodyAsText()
                if (!profilesJson.contains(user.id.toString())) {
                    errorMessage = "Неверный код восстановления"
                    isLoading = false
                    return@launch
                }

                val passwordHash = hashPassword(newPassword)
                val updateResponse = client.patch("${SupabaseConfig.BASE_URL_REST}/users?id=eq.${user.id}") {
                    header("apikey", SupabaseConfig.API_KEY)
                    header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("password_hash" to passwordHash))
                }

                if (updateResponse.status == HttpStatusCode.NoContent) {
                    successMessage = "Пароль успешно обновлён!"
                    email = ""
                    recoveryCode = ""
                    newPassword = ""
                    confirmPassword = ""
                } else {
                    errorMessage = "Не удалось обновить пароль"
                }

            } catch (e: Exception) {
                errorMessage = "Ошибка: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
