package com.posly.app.data.repository

import android.content.Context
import com.posly.app.data.local.dao.ProfileDao
import com.posly.app.data.local.dao.SettingsDao
import com.posly.app.data.local.entity.ProfileEntity
import com.posly.app.data.local.entity.StoreSettingsEntity
import com.posly.app.data.remote.SupabaseClientProvider
import com.posly.app.domain.model.Profile
import com.posly.app.domain.model.UserRole
import com.posly.app.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileDao: ProfileDao,
    private val settingsDao: SettingsDao,
    private val supabaseClientProvider: SupabaseClientProvider
) : AuthRepository {

    private val _currentProfile = MutableStateFlow<Profile?>(null)
    override val currentProfile: Flow<Profile?> = _currentProfile.asStateFlow()

    override suspend fun login(email: String, password: String): Result<Profile> = runCatching {
        val settings = settingsDao.getStoreSettingsOnce()
        if (settings != null && supabaseClientProvider.isConfigured(settings.supabaseUrl, settings.supabaseAnonKey)) {
            val client = supabaseClientProvider.getClient(settings.supabaseUrl, settings.supabaseAnonKey)
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val userId = client.auth.currentUserOrNull()?.id ?: error("Login failed")
            // Sync profile from Supabase
            val remoteProfile = client.from("profiles").select {
                filter { eq("id", userId) }
            }.decodeSingle<Map<String, Any?>>()
            val profile = Profile(
                id = userId,
                name = remoteProfile["name"] as? String ?: "",
                email = email,
                role = if (remoteProfile["role"] == "owner") UserRole.OWNER else UserRole.WORKER
            )
            profileDao.insertProfile(profile.toEntity())
            _currentProfile.value = profile
            profile
        } else {
            // Offline: match against local profile table
            val localProfile = profileDao.getOwner()?.toDomain()
                ?: error("Tidak ada akun terdaftar")
            _currentProfile.value = localProfile
            localProfile
        }
    }

    override suspend fun loginWithUsername(username: String, password: String): Result<Profile> =
        login(username, password)

    override suspend fun logout() {
        _currentProfile.value = null
        val settings = settingsDao.getStoreSettingsOnce()
        if (settings != null && supabaseClientProvider.isConfigured(settings.supabaseUrl, settings.supabaseAnonKey)) {
            runCatching {
                supabaseClientProvider.getClient(settings.supabaseUrl, settings.supabaseAnonKey)
                    .auth.signOut()
            }
        }
    }

    override suspend fun verifyOwnerPin(pin: String): Result<Boolean> = runCatching {
        val ownerPin = profileDao.getOwnerPin()
        ownerPin != null && ownerPin == pin
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = runCatching {
        // TODO: Implement via Supabase auth.updateUser
    }

    override suspend fun changePin(newPin: String): Result<Unit> = runCatching {
        val currentProfile = _currentProfile.value ?: error("Not authenticated")
        val entity = profileDao.getProfileById(currentProfile.id) ?: error("Profile not found")
        profileDao.updateProfile(entity.copy(pinCode = newPin))
    }

    override suspend fun registerWorker(name: String, email: String, password: String): Result<Profile> = runCatching {
        val newProfile = Profile(name = name, email = email, role = UserRole.WORKER)
        profileDao.insertProfile(newProfile.toEntity())
        newProfile
    }

    override fun getAllWorkers(): Flow<List<Profile>> =
        profileDao.getAllWorkers().map { list -> list.map { it.toDomain() } }

    override suspend fun deactivateWorker(workerId: String): Result<Unit> = runCatching {
        profileDao.deactivateWorker(workerId)
    }

    override suspend fun deleteWorker(workerId: String): Result<Unit> = runCatching {
        profileDao.deleteWorker(workerId)
    }

    override suspend fun hasOwnerAccount(): Boolean = profileDao.countOwners() > 0

    override suspend fun registerOwner(name: String, email: String, password: String, pin: String): Result<Profile> = runCatching {
        val profile = Profile(name = name, email = email, role = UserRole.OWNER, pinCode = pin)
        profileDao.insertProfile(profile.toEntity())
        _currentProfile.value = profile
        profile
    }

    override suspend fun getCurrentUserRole(): UserRole? = _currentProfile.value?.role

    // ─── Mappers ──────────────────────────────────────────────────────────────

    private fun ProfileEntity.toDomain() = Profile(
        id = id, name = name, email = email,
        role = if (role == "owner") UserRole.OWNER else UserRole.WORKER,
        pinCode = pinCode, avatarUrl = avatarUrl
    )

    private fun Profile.toEntity() = ProfileEntity(
        id = id, name = name, email = email,
        role = role.name.lowercase(), pinCode = pinCode, avatarUrl = avatarUrl
    )
}
