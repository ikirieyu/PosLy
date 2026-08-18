package com.posly.app.domain.repository

import com.posly.app.domain.model.StoreSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getStoreSettings(): Flow<StoreSettings>
    suspend fun updateStoreSettings(settings: StoreSettings): Result<Unit>
    suspend fun updateSupabaseConfig(url: String, anonKey: String): Result<Unit>
    suspend fun testSupabaseConnection(url: String, anonKey: String): Result<Boolean>
    suspend fun uploadLogo(imageBytes: ByteArray): Result<String>
    suspend fun exportDatabaseBackup(): Result<String>  // returns file path
    suspend fun importDatabaseBackup(filePath: String): Result<Unit>
}
