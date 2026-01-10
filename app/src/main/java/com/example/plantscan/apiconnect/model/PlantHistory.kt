package com.example.plantscan.apiconnect.model

import kotlinx.serialization.Serializable

@Serializable
data class PlantHistory(
    val id: String? = null,
    val name: String,
    val diagnosis: String,
    val symptoms: List<String> = emptyList(),
    val causes: List<String> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val image_url: String,
    val created_at: String? = null
)

