package com.example.plantscan

import com.example.plantscan.api.UserRepository
import com.example.plantscan.apiconnect.api.*
import com.example.plantscan.apiconnect.model.*
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class RepositoryUnitTest {
    @Test
    fun `updateUser returns false if no fields`() = runTest {
        val repo = UserRepository()
        val result = repo.updateUser("123")
        assertFalse(result)
    }
    @Test
    fun `saveFavorite returns true on success`() = runTest {
        val plant = FavoritePlant(name = "Test")
        val client = HttpClient(MockEngine) {
            engine { addHandler { respond("", HttpStatusCode.Created) } }
            install(ContentNegotiation) { json() }
        }
        val repo = FavoritesRepository(client)
        val result = repo.saveFavorite(plant)
        assertTrue(result)
    }

    @Test
    fun `getArticleById returns null if not found`() = runTest {
        val repo = ArticleRepository()
        val result = repo.getArticleById(123)
        assertNull(result)
    }

    @Test
    fun `getUsers returns empty list on error`() = runTest {
        val client = HttpClient(MockEngine) {
            engine { addHandler { respondError(HttpStatusCode.InternalServerError) } }
            install(ContentNegotiation) { json() }
        }
        val repo = UserRepository(client)
        val users = repo.getUsers()
        assertTrue(users.isEmpty())
    }
    @Test
    fun `updateProfile returns false if no fields`() = runTest {
        val repo = UserRepository()
        val result = repo.updateProfile("123")
        assertFalse(result)
    }

    @Test
    fun `getArticlesFromSupabase returns empty list if response blank`() = runTest {
        val client = HttpClient(MockEngine) {
            engine { addHandler { respond("", HttpStatusCode.OK) } }
            install(ContentNegotiation) { json() }
        }
        val repo = ArticleRepository(client)
        val articles = repo.getArticlesFromSupabase()
        assertTrue(articles.isEmpty())
    }

    @Test
    fun `deleteFavorite returns false on network error`() = runTest {
        val client = HttpClient(MockEngine) {
            engine { addHandler { respondError(HttpStatusCode.InternalServerError) } }
            install(ContentNegotiation) { json() }
        }
        val repo = FavoritesRepository(client)
        val result = repo.deleteFavorite("123")
        assertFalse(result)
    }
}
