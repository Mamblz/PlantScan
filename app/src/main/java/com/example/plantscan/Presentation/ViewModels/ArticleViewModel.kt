package com.example.plantscan.Presentation.ViewModels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantscan.apiconnect.api.ArticleRepository
import com.example.plantscan.apiconnect.model.ArticleItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ArticleViewModel : ViewModel() {
    private val _articlesList = MutableStateFlow<List<ArticleItem>>(emptyList())
    val articlesList: StateFlow<List<ArticleItem>> = _articlesList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadArticles()
    }

    fun loadArticles() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val articlesFromApi = ArticleRepository().getArticlesFromSupabase()
                _articlesList.value = articlesFromApi
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Ошибка загрузки"
            } finally {
                _isLoading.value = false
            }
        }
    }
}