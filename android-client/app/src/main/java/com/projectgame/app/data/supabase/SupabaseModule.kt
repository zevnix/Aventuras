package com.projectgame.app.data.supabase

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

import com.projectgame.app.BuildConfig
import kotlin.time.Duration.Companion.days

object SupabaseModule {

    lateinit var client: SupabaseClient
        private set

    fun initialize(context: Context) {
        client = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
        ) {
            install(Auth) {
                // Future production: Implement Settings implementation over EncryptedSharedPreferences.
                // For this Vertical Slice / Sandbox, the default memory cache allows the logic to proceed.
            }
            install(Postgrest)
        }
    }
}
