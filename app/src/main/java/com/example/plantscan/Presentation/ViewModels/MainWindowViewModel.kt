package com.example.plantscan.Presentation.ViewModels

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import com.example.plantscan.apiconnect.api.HistoryRepository
import com.example.plantscan.apiconnect.model.Plant
import com.example.plantscan.apiconnect.model.PlantHistory
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(
    private val historyRepository: HistoryRepository = HistoryRepository()
) : ViewModel() {

    var currentPhotoFile: File? = null
    val imagePath = mutableStateOf<String?>(null)
    val plantDetails = mutableStateOf<Plant?>(null)
    val plantHistory = mutableStateOf<List<PlantHistory>>(emptyList())
    val isLoading = mutableStateOf(false)

    fun launchCamera(context: Context, launcher: ManagedActivityResultLauncher<Uri, Boolean>) {
        val file = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        currentPhotoFile = file
        imagePath.value = file.absolutePath
        launcher.launch(uri)
    }

    fun loadHistory(context: Context) {
        viewModelScope.launch {
            try {
                plantHistory.value = historyRepository.getLastThree()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun reset() {
        plantDetails.value = null
        imagePath.value = null
        isLoading.value = false
        currentPhotoFile = null
    }
}
