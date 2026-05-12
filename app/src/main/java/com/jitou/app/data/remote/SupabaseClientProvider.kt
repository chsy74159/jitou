package com.jitou.app.data.remote

import com.jitou.app.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseClientProvider {
    val client by lazy {
        require(BuildConfig.SUPABASE_URL.isNotBlank()) { "SUPABASE_URL is missing in local.properties" }
        require(BuildConfig.SUPABASE_PUBLISHABLE_KEY.isNotBlank()) {
            "SUPABASE_PUBLISHABLE_KEY is missing in local.properties"
        }

        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY,
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    }
}
