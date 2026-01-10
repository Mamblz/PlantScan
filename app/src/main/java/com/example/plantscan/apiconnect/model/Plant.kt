package com.example.plantscan.apiconnect.model

import kotlinx.serialization.Serializable

@Serializable
data class Plant(
    val id: Int? = null,
    val common_name: String? = null,
    val scientific_name: String? = null,
    val family: String? = null,
    val description: String? = null
)