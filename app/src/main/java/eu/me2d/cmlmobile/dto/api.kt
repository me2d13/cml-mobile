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

@Serializable
data class ApiCommand(
    val number: Int,
    val description: String
)