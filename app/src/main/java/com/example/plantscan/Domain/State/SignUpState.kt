package com.example.plantscan.Domain.State

data class SignUpState(
    val email: String = "",
    val password: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val confirmPassword: String = ""
)
