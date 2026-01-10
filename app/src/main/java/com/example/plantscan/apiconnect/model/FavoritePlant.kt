package com.example.plantscan.apiconnect.model

import kotlinx.serialization.Serializable

@Serializable
data class FavoritePlant(
    val id: String? = null,
    val user_id: String? = null,
    val plant_id: Int? = null,
    val created_at: String? = null,

    val name: String,
    val description: String? = null,
    val photo_url: String? = null,
    val diagnosis: String? = null,

    val symptoms: List<String> = emptyList(),
    val causes: List<String> = emptyList(),
    val recommendations: List<String> = emptyList()
)