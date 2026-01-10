package com.example.plantscan.apiconnect.api

import android.util.Log
import com.example.app.domain.utils.SupabaseConfig
import com.example.plantscan.apiconnect.model.ArticleItem
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class ArticleRepository(
    private val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
) {

    suspend fun getArticles(): List<ArticleItem> {
        return try {
            val response: HttpResponse = client.get("${SupabaseConfig.BASE_URL_REST}/article") {
                header("apikey", SupabaseConfig.API_KEY)
                header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
                parameter("select", "*")
            }

            if (response.status.value !in 200..299) {
                Log.e("ArticleRepository", "Ошибка загрузки статей: ${response.status}")
                return emptyList()
            }

            val body = response.bodyAsText()
            if (body.isBlank() || body == "[]") return emptyList()

            Json { ignoreUnknownKeys = true }
                .decodeFromString(ListSerializer(ArticleItem.serializer()), body)
        } catch (e: Exception) {
            Log.e("ArticleRepository", "Ошибка getArticles()", e)
            emptyList()
        }
    }

    suspend fun getArticleById(id: Int): ArticleItem? {
        return try {
            val response: HttpResponse = client.get("${SupabaseConfig.BASE_URL_REST}/article") {
                header("apikey", SupabaseConfig.API_KEY)
                header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
                parameter("id", "eq.$id")
                parameter("select", "*")
            }

            val body = response.bodyAsText()
            if (response.status.value !in 200..299 || body.isBlank() || body == "[]") return null

            Json { ignoreUnknownKeys = true }
                .decodeFromString(ListSerializer(ArticleItem.serializer()), body)
                .firstOrNull()
        } catch (e: Exception) {
            Log.e("ArticleRepository", "Ошибка getArticleById()", e)
            null
        }
    }

    suspend fun getArticlesFromSupabase(): List<ArticleItem> {
        return try {
            val response: HttpResponse = client.get("${SupabaseConfig.BASE_URL_REST}/articles") {
                header("apikey", SupabaseConfig.API_KEY)
                header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
            }

            if (response.status.value !in 200..299) {
                Log.e("ArticleRepository", "Ошибка: ${response.status}")
                return emptyList()
            }

            val body = response.bodyAsText()
            if (body.isBlank() || body == "[]") return emptyList()

            Json { ignoreUnknownKeys = true }.decodeFromString(ListSerializer(ArticleItem.serializer()), body)
        } catch (e: Exception) {
            Log.e("ArticleRepository", "Ошибка загрузки статей", e)
            emptyList()
        }
    }
}