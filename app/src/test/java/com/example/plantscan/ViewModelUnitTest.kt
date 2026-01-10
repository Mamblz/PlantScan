package com.example.plantscan

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.plantscan.Presentation.ViewModels.*
import com.example.plantscan.apiconnect.api.FavoritesRepository
import com.example.plantscan.apiconnect.api.HistoryRepository
import com.example.plantscan.apiconnect.api.LeafDiseaseRepository
import com.example.plantscan.apiconnect.model.FavoritePlant
import io.mockk.*
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull


@ExperimentalCoroutinesApi
class ViewModelUnitTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var leafRepo: LeafDiseaseRepository
    private lateinit var favoritesRepo: FavoritesRepository
    private lateinit var historyRepo: HistoryRepository
    private lateinit var analysisVM: AnalysisViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        leafRepo = mockk()
        favoritesRepo = mockk()
        historyRepo = mockk()
        analysisVM = AnalysisViewModel(leafRepo, favoritesRepo, historyRepo)
    }

    @Test
    fun `setImage sets imageUri`() {
        val path = "/tmp/image.jpg"
        analysisVM.setImage(path)
        assertEquals(path, analysisVM.imageUri)
    }

    @Test
    fun `identifyPlant returns error when file does not exist`() = runTest {
        val file = File("/tmp/nonexistent.jpg")
        analysisVM.identifyPlant(file)
        assertEquals("Файл изображения не найден", analysisVM.errorMessage)
    }


    @Test
    fun `reset clears all states`() {
        analysisVM.setImage("/tmp/img.jpg")
        analysisVM.reset()
        assertNull(analysisVM.imageUri)
        assertFalse(analysisVM.isAnalyzing)
        assertNull(analysisVM.errorMessage)
    }

    @Test
    fun `saveHistory returns false if diseaseResult is null`() = runTest {
        val context = mockk<android.content.Context>()
        val result = analysisVM.saveHistory(context)
        assertFalse(result)
    }

    @Test
    fun `resetNavigation resets navigateToMain`() {
        val vm = SignInViewModel()
        vm.resetNavigation()
        assertFalse(vm.navigateToMain.value)
    }

    @Test
    fun `resetState resets SignInViewModel`() {
        val vm = SignInViewModel()
        vm.updateEmail("a@b.com")
        vm.updatePassword("123456")
        vm.resetState()
        assertEquals("", vm.uiState.value.email)
        assertEquals("", vm.uiState.value.password)
    }

    @Test
    fun `changePassword validation fails for empty old password`() {
        val vm = ChangePasswordViewModel()
        vm.updateState(
            ChangePasswordUiState(
                oldPassword = "",
                newPassword = "123456",
                confirmPassword = "123456"
            )
        )
        vm.changePassword()
        assertEquals("Введите старый пароль", vm.uiState.value.oldPasswordError)
    }

    @Test
    fun `loadFavorites updates favorites list`() = runTest {
        coEvery { favoritesRepo.getFavorites() } returns listOf(FavoritePlant(name = "Test"))
        val vm = FavoritesViewModel(favoritesRepo)
        advanceUntilIdle()
        assertEquals(1, vm.favorites.value.size)
        assertEquals("Test", vm.favorites.value[0].name)
    }

    @Test
    fun `addItem adds to historyList`() {
        val vm = HistoryViewModel()
        val item = PlantHistoryItem("10", null, "Лилия", "2025-11-23")
        vm.addItem(item)
        assertEquals(item, vm.historyList.first())
    }

    @Test
    fun `loadPlant updates plant`() = runTest {
        val repo = mockk<FavoritesRepository>()
        val plant = FavoritePlant(name = "Роза")
        coEvery { repo.getPlantById("1") } returns plant
        val vm = PlantDetailViewModel(repo)
        vm.loadPlant("1")
        advanceUntilIdle()
        assertEquals(plant, vm.plant.value)
    }
}
