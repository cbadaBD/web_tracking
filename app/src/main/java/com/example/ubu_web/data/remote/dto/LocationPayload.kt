package com.example.ubu_web.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LocationPayload(
    val device_name: String,
    val device_label: String,
    val points: List<PointDto>
)

@Serializable
data class PointDto(
    val lat: Double,
    val lng: Double,
    val timestamp: Long,
    val accuracy: Float? = null,
    val speed: Float? = null,
    val heading: Float? = null,
    val battery: Int? = null
)