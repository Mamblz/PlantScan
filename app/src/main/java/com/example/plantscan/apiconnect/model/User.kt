package com.example.plantscan.apiconnect.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String? = null,
    val password_hash: String? = null,
    val email: String? = null,
    val created_at: String? = null
)