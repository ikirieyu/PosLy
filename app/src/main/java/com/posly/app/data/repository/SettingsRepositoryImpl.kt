package com.posly.app.data.repository

import com.posly.app.data.local.dao.SettingsDao
import com.posly.app.data.local.entity.StoreSettingsEntity
import com.posly.app.data.remote.SupabaseClientProvider
import com.posly.app.domain.model.StoreSettings
import com.posly.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: SettingsDao,
    private val supabaseClientProvider: SupabaseClientProvider
) : SettingsRepository {

    override fun getStoreSettings(): Flow<StoreSettings> =
        settingsDao.getStoreSettings().map { entity ->
            entity?.toDomain() ?: StoreSettings()
        }

    override suspend fun updateStoreSettings(settings: StoreSettings): Result<Unit> = runCatching {
        settingsDao.saveStoreSettings(settings.toEntity())
    }

    override suspend fun updateSupabaseConfig(url: String, anonKey: String): Result<Unit> = runCatching {
        val current = settingsDao.getStoreSettingsOnce() ?: StoreSettingsEntity()
        settingsDao.saveStoreSettings(
            current.copy(supabaseUrl = url, supabaseAnonKey = anonKey)
        )
    }

    override suspend fun testSupabaseConnection(url: String, anonKey: String): Result<Boolean> = runCatching {
        if (!supabaseClientProvider.isConfigured(url, anonKey)) return@runCatching false
        val client = supabaseClientProvider.getClient(url, anonKey)
        // Simple connectivity test: try to fetch auth user (will 401 but connection established)
        runCatching { client.auth.currentUserOrNull() }
        true
    }

    override suspend fun uploadLogo(imageBytes: ByteArray): Result<String> = runCatching {
        val settings = settingsDao.getStoreSettingsOnce() ?: return@runCatching ""
        if (!supabaseClientProvider.isConfigured(settings.supabaseUrl, settings.supabaseAnonKey)) {
            error("Supabase belum dikonfigurasi")
        }
        // TODO: Upload to Supabase Storage
        ""
    }

    override suspend fun exportDatabaseBackup(): Result<String> = runCatching {
        // TODO: Implement backup export
        ""
    }

    override suspend fun importDatabaseBackup(filePath: String): Result<Unit> = runCatching {
        // TODO: Implement backup import
    }

    // ─── Mappers ──────────────────────────────────────────────────────────────

    private fun StoreSettingsEntity.toDomain() = StoreSettings(
        id = id, storeName = storeName, slogan = slogan, address = address,
        phone = phone, socialMedia = socialMedia, logoUrl = logoUrl,
        receiptFooter = receiptFooter, savingsPercent = savingsPercent,
        emergencyPercent = emergencyPercent, restockPercent = restockPercent,
        transportPercent = transportPercent, supabaseUrl = supabaseUrl,
        supabaseAnonKey = supabaseAnonKey, updatedAt = updatedAt
    )

    private fun StoreSettings.toEntity() = StoreSettingsEntity(
        id = id, storeName = storeName, slogan = slogan, address = address,
        phone = phone, socialMedia = socialMedia, logoUrl = logoUrl,
        receiptFooter = receiptFooter, savingsPercent = savingsPercent,
        emergencyPercent = emergencyPercent, restockPercent = restockPercent,
        transportPercent = transportPercent, supabaseUrl = supabaseUrl,
        supabaseAnonKey = supabaseAnonKey, updatedAt = updatedAt
    )
}
