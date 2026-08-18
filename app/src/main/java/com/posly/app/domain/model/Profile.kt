package com.posly.app.domain.model

import java.util.UUID

/**
 * Domain model for user profile & role.
 */
data class Profile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String = "",
    val role: UserRole,
    val pinCode: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    OWNER, WORKER
}
