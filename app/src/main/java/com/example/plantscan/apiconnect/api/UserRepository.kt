package com.example.plantscan.api

import android.util.Log
import com.example.app.domain.utils.SupabaseConfig
import com.example.plantscan.apiconnect.model.Profile
import com.example.plantscan.apiconnect.model.User
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.security.MessageDigest

class UserRepository(
    private val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
) {

    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun createUser(
        email: String,
        password: String,
        firstName: String?,
        lastName: String?,
        phone: String?
    ): User? {
        return try {
            val passwordHash = hashPassword(password)

            if (getUserByEmail(email) != null) {
                throw Exception("Этот email уже используется")
            }

            val bodyMap = mutableMapOf<String, Any>(
                "email" to email,
                "password_hash" to passwordHash
            )

            firstName?.takeIf { it.isNotBlank() }?.let { bodyMap["first_name"] = it }
            lastName?.takeIf { it.isNotBlank() }?.let { bodyMap["last_name"] = it }
            phone?.takeIf { it.isNotBlank() }?.let { bodyMap["phone"] = it }

            println("ОТПРАВЛЯЕМ В SUPABASE: $ Write bodyMap")

            val response = client.post("${SupabaseConfig.BASE_URL_REST}/users") {
                header("apikey", SupabaseConfig.API_KEY)
                header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
                contentType(ContentType.Application.Json)
                setBody(bodyMap)
            }

            if (response.status == HttpStatusCode.Created) {
                getUserByEmail(email)
            } else {
                throw Exception("Ошибка: ${response.status} — ${response.bodyAsText()}")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getUsers(): List<User> {
        return try {
            val response: HttpResponse = client.get("${SupabaseConfig.BASE_URL_REST}/users") {
                header("apikey", SupabaseConfig.API_KEY)
                header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
            }

            if (response.status.value !in 200..299) return emptyList()

            Json.decodeFromString<List<User>>(response.bodyAsText())
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getUserByEmail(email: String): User? {
        return try {
            val encodedEmail = email.trim().lowercase()
            Log.d("UserRepository", "🔍 Ищем пользователя по email: '$encodedEmail'")

            val response: HttpResponse = client.get("${SupabaseConfig.BASE_URL_REST}/users") {
                header("apikey", SupabaseConfig.API_KEY)
                header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
                parameter("email", "eq.$encodedEmail")
                parameter("select", "*")
            }

            Log.d("UserRepository", "Response status: ${response.status}")
            val body = response.bodyAsText()
            Log.d("UserRepository", "Response body: $body")

            if (response.status.value !in 200..299) {
                Log.e("UserRepository", "Ошибка: ${response.status}")
                return null
            }

            if (body.isBlank() || body == "[]") {
                Log.w("UserRepository", "Пользователь с email '$encodedEmail' не найден")
                return null
            }

            val users: List<User> = Json { ignoreUnknownKeys = true }.decodeFromString(body)
            val user = users.firstOrNull()
            Log.d("UserRepository", "Найден пользователь: $user")
            user
        } catch (e: Exception) {
            Log.e("UserRepository", "Ошибка getUserByEmail", e)
            null
        }
    }

    suspend fun getProfileByUserId(userId: String): Profile? {
        return try {
            val response: HttpResponse = client.get("${SupabaseConfig.BASE_URL_REST}/profiles") {
                header("apikey", SupabaseConfig.API_KEY)
                header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
                parameter("user_id", "eq.$userId")
                parameter("select", "*")
            }

            if (response.status.value !in 200..299) return null

            val body = response.bodyAsText()
            val profiles: List<Profile> = Json { ignoreUnknownKeys = true }.decodeFromString(body)
            profiles.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getProfile(userId: String): Profile? {
        val response: HttpResponse = client.get("${SupabaseConfig.BASE_URL_REST}/profiles") {
            header("apikey", SupabaseConfig.API_KEY)
            header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
            parameter("user_id", "eq.$userId")
            parameter("select", "*")
        }

        val body = response.bodyAsText()
        if (response.status.value !in 200..299 || body.isBlank() || body == "[]") return null
        return Json { ignoreUnknownKeys = true }.decodeFromString<List<Profile>>(body).firstOrNull()
    }


    suspend fun updateUser(
        userId: String,
        firstName: String? = null,
        lastName: String? = null,
        email: String? = null,
        password: String? = null
    ): Boolean {
        return try {
            val bodyMap = mutableMapOf<String, Any>()

            firstName?.takeIf { it.isNotBlank() }?.let { bodyMap["first_name"] = it }
            lastName?.takeIf { it.isNotBlank() }?.let { bodyMap["last_name"] = it }
            email?.takeIf { it.isNotBlank() }?.let { bodyMap["email"] = it }
            password?.takeIf { it.isNotBlank() }?.let { bodyMap["password_hash"] = hashPassword(it) }

            if (bodyMap.isEmpty()) return false

            val response: HttpResponse = client.patch("${SupabaseConfig.BASE_URL_REST}/users?id=eq.$userId") {
                header("apikey", SupabaseConfig.API_KEY)
                header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
                contentType(ContentType.Application.Json)
                setBody(bodyMap)
            }

            response.status.value in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateProfile(
        userId: String,
        firstName: String? = null,
        lastName: String? = null,
        phone: String? = null
    ): Boolean {
        return try {
            val bodyMap = mutableMapOf<String, Any>()

            firstName?.takeIf { it.isNotBlank() }?.let { bodyMap["first_name"] = it }
            lastName?.takeIf { it.isNotBlank() }?.let { bodyMap["last_name"] = it }
            phone?.takeIf { it.isNotBlank() }?.let { bodyMap["phone"] = it }

            if (bodyMap.isEmpty()) return false

            val response: HttpResponse = client.patch("${SupabaseConfig.BASE_URL_REST}/profiles?user_id=eq.$userId") {
                header("apikey", SupabaseConfig.API_KEY)
                header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
                contentType(ContentType.Application.Json)
                setBody(bodyMap)
            }

            if (response.status.value in 200..299) {
                Log.d("UserRepository", "Профиль успешно обновлён: $bodyMap")
                true
            } else {
                Log.e("UserRepository", "Ошибка обновления профиля: ${response.status}")
                false
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Ошибка updateProfile()", e)
            false
        }
    }

    suspend fun getUserById(userId: String): User? {
        val response: HttpResponse = client.get("${SupabaseConfig.BASE_URL_REST}/users") {
            header("apikey", SupabaseConfig.API_KEY)
            header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
            parameter("id", "eq.$userId")
            parameter("select", "*")
        }

        val body = response.bodyAsText()
        if (response.status.value !in 200..299 || body.isBlank() || body == "[]") return null
        return Json { ignoreUnknownKeys = true }.decodeFromString<List<User>>(body).firstOrNull()
    }

    suspend fun updateUserPassword(
        userId: String,
        oldPassword: String,
        newPassword: String
    ): Boolean {
        val user = getUserById(userId) ?: return false

        val oldPasswordHash = hashPassword(oldPassword)

        if (user.password_hash.isNullOrEmpty() || !user.password_hash.equals(oldPasswordHash, ignoreCase = true)) {
            return false
        }

        return updateUser(userId, password = newPassword)
    }
}