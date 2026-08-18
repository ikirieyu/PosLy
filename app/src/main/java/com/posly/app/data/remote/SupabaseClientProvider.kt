package com.posly.app.data.remote

import android.content.Context
import com.posly.app.data.local.dao.SettingsDao
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides a lazily-initialized Supabase client.
 * URL and Key are read from the local database (set by user in Settings).
 * Reinitializes when credentials are updated.
 */
@Singleton
class SupabaseClientProvider @Inject constructor(
    private val context: Context
) {
    private var _client: SupabaseClient? = null

    @Volatile
    private var currentUrl: String = ""
    @Volatile
    private var currentKey: String = ""

    fun getClient(url: String, anonKey: String): SupabaseClient {
        if (_client == null || url != currentUrl || anonKey != currentKey) {
            synchronized(this) {
                if (_client == null || url != currentUrl || anonKey != currentKey) {
                    currentUrl = url
                    currentKey = anonKey
                    _client = createSupabaseClient(
                        supabaseUrl = url,
                        supabaseKey = anonKey
                    ) {
                        install(Auth)
                        install(Postgrest)
                        install(Realtime)
                        install(Storage)
                    }
                }
            }
        }
        return _client!!
    }

    fun isConfigured(url: String, key: String): Boolean =
        url.isNotBlank() && key.isNotBlank()
}
