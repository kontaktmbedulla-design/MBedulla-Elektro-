package com.example.ui.model

enum class UserRole(val label: String) {
    MANAGER("Menaxher"),
    ELECTRICIAN("Elektricist")
}

data class UserSession(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val workerId: String? = null,
    val roleDescription: String = ""
)
