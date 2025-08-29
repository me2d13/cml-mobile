package eu.me2d.cmlmobile.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val key: String,
    val message: String?
)

@Serializable
data class RegisterResponse(
    val status: String?
)
