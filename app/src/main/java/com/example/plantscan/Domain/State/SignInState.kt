package com.example.app.domain.state

data class SignInState(
    val email: String = "",
    val password: String = "",
    val ErrorMessage: String? = null,
)
