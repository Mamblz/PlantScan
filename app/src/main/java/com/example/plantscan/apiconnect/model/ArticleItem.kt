package com.example.plantscan.apiconnect.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticleItem(
    val id: Int,
    val title: String,
    val description: String,
    val content: String,
    @SerialName("image_url") val imageUrl: String? = null
)