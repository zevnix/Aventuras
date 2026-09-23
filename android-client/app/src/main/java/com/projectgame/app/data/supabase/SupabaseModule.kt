package com.projectgame.app.data.supabase

import android.content.Context
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
                // Configures Supabase Auth to use Settings / DataStore for session persistence
                // Supabase-kt v2.x requires Settings implementation for offline persistence.
                // We'll rely on the default engine provided by the library.
            }
            install(Postgrest)
        }
    }
}
