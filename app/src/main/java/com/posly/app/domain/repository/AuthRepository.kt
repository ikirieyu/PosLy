package com.posly.app.domain.repository

import com.posly.app.domain.model.Profile
import com.posly.app.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /** Current authenticated user profile, null if not logged in */
    val currentProfile: Flow<Profile?>

    /** Login with email and password via Supabase Auth */
    suspend fun login(email: String, password: String): Result<Profile>

    /** Login with username (for worker accounts) */
    suspend fun loginWithUsername(username: String, password: String): Result<Profile>

    /** Logout current session */
    suspend fun logout()

    /** Verify Owner PIN for sensitive operations (e.g., void approval) */
    suspend fun verifyOwnerPin(pin: String): Result<Boolean>

    /** Change current user's password */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>

    /** Change current user's PIN */
    suspend fun changePin(newPin: String): Result<Unit>

    /** Register a new worker account (Owner only) */
    suspend fun registerWorker(name: String, email: String, password: String): Result<Profile>

    /** Get all worker profiles (Owner only) */
    suspend fun getAllWorkers(): Flow<List<Profile>>

    /** Deactivate a worker account */
    suspend fun deactivateWorker(workerId: String): Result<Unit>

    /** Delete a worker account */
    suspend fun deleteWorker(workerId: String): Result<Unit>

    /** Check if any account exists (first launch detection) */
    suspend fun hasOwnerAccount(): Boolean

    /** Register owner account on first launch */
    suspend fun registerOwner(name: String, email: String, password: String, pin: String): Result<Profile>

    /** Get current user role */
    suspend fun getCurrentUserRole(): UserRole?
}
