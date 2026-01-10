package com.example.plantscan.Presentation.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantscan.apiconnect.api.FavoritesRepository
import com.example.plantscan.apiconnect.model.FavoritePlant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: FavoritesRepository = FavoritesRepository()
) : ViewModel() {

    private val _favorites = MutableStateFlow<List<FavoritePlant>>(emptyList())
    val favorites: StateFlow<List<FavoritePlant>> = _favorites.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val list = repository.getFavorites()
                _favorites.value = list
            } catch (e: Exception) {
                _error.value = "Не удалось загрузить избранное"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() = loadFavorites()
}