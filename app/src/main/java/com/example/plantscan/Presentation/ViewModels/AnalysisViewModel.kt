package com.example.plantscan.Presentation.ViewModels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantscan.apiconnect.api.FavoritesRepository
import com.example.plantscan.apiconnect.api.HistoryRepository
import com.example.plantscan.apiconnect.api.LeafDiseaseRepository
import com.example.plantscan.apiconnect.model.FavoritePlant
import com.example.plantscan.apiconnect.model.PlantHistory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.util.*

class AnalysisViewModel(
    private val leafDiseaseRepository: LeafDiseaseRepository = LeafDiseaseRepository(),
    private val favoritesRepository: FavoritesRepository = FavoritesRepository(),
    private val historyRepository: HistoryRepository = HistoryRepository()
) : ViewModel() {

    private val _diseaseResult = MutableStateFlow<LeafDiseaseRepository.DiseaseResponse?>(null)
    val diseaseResult: StateFlow<LeafDiseaseRepository.DiseaseResponse?> = _diseaseResult

    var isAnalyzing by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var attemptCount by mutableStateOf(0)
        private set

    var imageUri by mutableStateOf<String?>(null)
        private set

    private var currentAnalysisId: UUID? = null

    fun setImage(uri: String) {
        imageUri = uri
    }

    fun identifyPlant(
        file: File,
        maxRetries: Int = 3,
        delayBetweenRetries: Long = 2000L
    ) {
        if (!file.exists()) {
            errorMessage = "Файл изображения не найден"
            _diseaseResult.value = null
            return
        }

        if (isAnalyzing && imageUri == file.absolutePath) return

        val analysisId = UUID.randomUUID()
        currentAnalysisId = analysisId
        imageUri = file.absolutePath

        viewModelScope.launch {
            isAnalyzing = true
            errorMessage = null
            attemptCount = 0
            var result: LeafDiseaseRepository.DiseaseResponse? = null

            while (attemptCount < maxRetries && currentAnalysisId == analysisId) {
                attemptCount++
                try {
                    val res = leafDiseaseRepository.analyzeLeaf(file)
                    if (currentAnalysisId != analysisId) return@launch

                    if (res != null) {
                        result = res
                        break
                    } else {
                        errorMessage = "Не удалось определить растение. Попытка $attemptCount из $maxRetries"
                    }
                } catch (e: Exception) {
                    if (currentAnalysisId != analysisId) return@launch
                    errorMessage = "Ошибка при анализе: ${e.message}. Попытка $attemptCount из $maxRetries"
                }

                if (attemptCount < maxRetries) delay(delayBetweenRetries)
            }

            if (currentAnalysisId == analysisId) {
                _diseaseResult.value = result

                if (result == null) {
                    errorMessage = "Не удалось определить растение после $maxRetries попыток"
                }

                isAnalyzing = false
            }
        }
    }

    suspend fun saveToFavorites(context: Context): Boolean {
        val result = _diseaseResult.value
        val filePath = imageUri

        if (result == null) {
            errorMessage = "Нет данных для сохранения"
            return false
        }

        if (filePath == null) {
            errorMessage = "Путь к изображению отсутствует"
            return false
        }

        val file = File(filePath)
        if (!file.exists()) {
            errorMessage = "Файл изображения не найден"
            return false
        }

        val plant = FavoritePlant(
            name = result.plant_name,
            description = "Анализ от ${LocalDate.now()}",
            diagnosis = result.diagnosis,
            symptoms = result.symptoms,
            causes = result.causes,
            recommendations = result.recommendations
        )

        return try {
            favoritesRepository.saveFavoriteWithPhoto(plant, file, context)
        } catch (e: Exception) {
            errorMessage = "Не удалось сохранить в избранное"
            false
        }
    }

    suspend fun saveHistory(context: Context): Boolean {
        val result = _diseaseResult.value
        val filePath = imageUri

        if (result == null || filePath == null) return false

        val file = File(filePath)

        val historyItem = PlantHistory(
            name = result.plant_name,
            diagnosis = result.diagnosis,
            symptoms = result.symptoms,
            causes = result.causes,
            recommendations = result.recommendations,
            image_url = imageUri ?: ""
        )

        return historyRepository.saveHistory(historyItem, file, context)
    }

    fun reset() {
        _diseaseResult.value = null
        imageUri = null
        isAnalyzing = false
        errorMessage = null
        attemptCount = 0
        currentAnalysisId = null
    }
}
