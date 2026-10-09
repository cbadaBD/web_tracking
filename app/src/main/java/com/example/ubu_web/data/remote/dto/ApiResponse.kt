package com.example.ubu_web.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse(
    val message: String? = null,
    val device_id: Int? = null,
    val device_name: String? = null,
    val received: Int? = null,
    val inserted: Int? = null,
    val error: String? = null
)