package com.posly.app.di

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.posly.app.data.local.PoslyDatabase
import com.posly.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PoslyDatabase {
        // Retrieve or generate DB passphrase stored in EncryptedSharedPreferences
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            context,
            "posly_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val passphraseKey = "db_passphrase"
        val passphrase = prefs.getString(passphraseKey, null)
            ?: java.util.UUID.randomUUID().toString().also {
                prefs.edit().putString(passphraseKey, it).apply()
            }

        return PoslyDatabase.create(context, passphrase.toByteArray())
    }

    @Provides fun provideProfileDao(db: PoslyDatabase) = db.profileDao()
    @Provides fun provideProductDao(db: PoslyDatabase) = db.productDao()
    @Provides fun provideOrderDao(db: PoslyDatabase) = db.orderDao()
    @Provides fun provideExpenseDao(db: PoslyDatabase) = db.expenseDao()
    @Provides fun provideSettingsDao(db: PoslyDatabase) = db.settingsDao()
}
