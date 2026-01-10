package com.example.plantscan.Presentation.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantscan.apiconnect.api.FavoritesRepository
import com.example.plantscan.apiconnect.model.FavoritePlant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlantDetailViewModel(
    private val repository: FavoritesRepository = FavoritesRepository()
) : ViewModel() {

    private val _plant = MutableStateFlow<FavoritePlant?>(null)
    val plant: StateFlow<FavoritePlant?> = _plant

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadPlant(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getPlantById(id)
            _plant.value = result
            _isLoading.value = false
        }
    }
}