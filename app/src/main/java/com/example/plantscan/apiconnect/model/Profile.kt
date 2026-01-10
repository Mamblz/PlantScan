package com.example.plantscan.apiconnect.model


import kotlinx.serialization.Serializable;

@Serializable
data class Profile(
    val user_id: String,
    val first_name: String = "",
    val last_name: String = "",
    val phone: String = "",
    val recovery_code: String =""
)