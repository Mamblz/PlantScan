package com.example.plantscan

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.example.plantscan.Presentation.Screen.MainScreen
import com.example.plantscan.Presentation.Screen.PersonalDataScreen
import com.example.plantscan.Presentation.Screen.ProfileScreen
import com.example.plantscan.Presentation.Screen.SignUpScreen
import com.example.plantscan.Presentation.Screens.*
import com.example.plantscan.Presentation.ViewModels.ProfileViewModel
import org.junit.Rule
import org.junit.Test

class UiTests {

    @get:Rule
    val composeTestRule = createComposeRule()
    @Test
    fun signUpScreen_displaysAllFieldsAndButton() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            SignUpScreen(navController)
        }

        composeTestRule.onNodeWithText("Создание аккаунта").assertIsDisplayed()
        composeTestRule.onNodeWithText("Имя").assertIsDisplayed()
        composeTestRule.onNodeWithText("Фамилия").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Номер телефона").assertIsDisplayed()
        composeTestRule.onNodeWithText("Пароль").assertIsDisplayed()
        composeTestRule.onNodeWithText("Повторите пароль").assertIsDisplayed()
        composeTestRule.onNodeWithText("Зарегистрироваться").assertIsDisplayed()
    }

    @Test
    fun signUpScreen_buttonEnabledWhenFieldsFilled() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            SignUpScreen(navController)
        }

        composeTestRule.onNodeWithTag("firstNameField").performTextInput("John")
        composeTestRule.onNodeWithTag("lastNameField").performTextInput("Doe")
        composeTestRule.onNodeWithTag("emailField").performTextInput("john@example.com")
        composeTestRule.onNodeWithTag("phoneField").performTextInput("1234567890")
        composeTestRule.onNodeWithTag("passwordField").performTextInput("123456")
        composeTestRule.onNodeWithTag("confirmPasswordField").performTextInput("123456")

        composeTestRule.onNodeWithText("Зарегистрироваться").assertIsEnabled()
    }
    @Test
    fun signInScreen_displaysFieldsAndButton() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            SignInScreen(navController)
        }

        composeTestRule.onNodeWithText("Вход").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Пароль").assertIsDisplayed()
        composeTestRule.onNodeWithText("Войти").assertIsDisplayed()
        composeTestRule.onNodeWithText("Регистрация").assertIsDisplayed()
        composeTestRule.onNodeWithText("Забыли пароль?").assertIsDisplayed()
    }

    @Test
    fun signInScreen_buttonEnabledWithValidInput() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            SignInScreen(navController)
        }

        composeTestRule.onNodeWithTag("emailField").performTextInput("john@example.com")
        composeTestRule.onNodeWithTag("passwordField").performTextInput("123456")
        composeTestRule.onNodeWithText("Войти").assertIsEnabled()
    }
    @Test
    fun mainScreen_displaysButtonsAndHistoryList() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            MainScreen(navController)
        }

        composeTestRule.onNodeWithText("Сделать фото").assertIsDisplayed()
        composeTestRule.onNodeWithText("Из галереи").assertIsDisplayed()
        composeTestRule.onNodeWithText("Последние растения:").assertIsDisplayed()
    }
    @Test
    fun personalDataScreen_displaysAllFieldsAndButton() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            PersonalDataScreen(navController)
        }

        composeTestRule.onNodeWithText("Личные данные").assertIsDisplayed()
        composeTestRule.onNodeWithText("Имя").assertIsDisplayed()
        composeTestRule.onNodeWithText("Фамилия").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Номер телефона").assertIsDisplayed()
        composeTestRule.onNodeWithText("Сохранить изменения").assertIsDisplayed()
    }

    @Test
    fun personalDataScreen_buttonEnabledWithValidInput() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            PersonalDataScreen(navController)
        }

        composeTestRule.onNodeWithTag("firstNameField").performTextInput("John")
        composeTestRule.onNodeWithTag("lastNameField").performTextInput("Doe")
        composeTestRule.onNodeWithTag("emailField").performTextInput("john@example.com")
        composeTestRule.onNodeWithTag("phoneField").performTextInput("1234567890")

        composeTestRule.onNodeWithTag("saveButton").assertIsEnabled()
    }

    @Test
    fun signUpScreen_buttonDisabledForInvalidEmail() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            SignUpScreen(navController)
        }

        composeTestRule.onNodeWithTag("emailField").performTextInput("invalid_email")
        composeTestRule.onNodeWithTag("passwordField").performTextInput("123456")
        composeTestRule.onNodeWithTag("confirmPasswordField").performTextInput("123456")
        composeTestRule.onNodeWithTag("firstNameField").performTextInput("John")
        composeTestRule.onNodeWithTag("lastNameField").performTextInput("Doe")
        composeTestRule.onNodeWithTag("phoneField").performTextInput("1234567890")
        composeTestRule.onNodeWithText("Зарегистрироваться").assertIsNotEnabled()
    }

    @Test
    fun profileScreen_displaysUserInfoAndButtons() {
        val fakeViewModel = object : ProfileViewModel() {
            override var fullName: String = "John Doe"
            override var email: String = "john@example.com"
            override var phone: String = "1234567890"
            override var isLoading: Boolean = false
            override var error: String? = null
        }

        composeTestRule.setContent {
            val navController = rememberNavController()
            ProfileScreen(navController, viewModel = fakeViewModel)
        }
        composeTestRule.onNodeWithText("Профиль").assertIsDisplayed()
        composeTestRule.onNodeWithText("Имя и фамилия").assertIsDisplayed()
        composeTestRule.onNodeWithText("Электронная почта").assertIsDisplayed()
        composeTestRule.onNodeWithText("Телефон").assertIsDisplayed()
        composeTestRule.onNodeWithTag("personalDataButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("changePasswordButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("logoutButton").assertIsDisplayed()
    }


    @Test
    fun signInScreen_errorDisplayedForInvalidEmail() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            SignInScreen(navController)
        }
        composeTestRule.onNodeWithTag("emailField").performTextInput("invalid_email")
        composeTestRule.onNodeWithTag("passwordField").performTextInput("123456")
        composeTestRule.onNodeWithText("Войти").assertIsNotEnabled()
    }
}
