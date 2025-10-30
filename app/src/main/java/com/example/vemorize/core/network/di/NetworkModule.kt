package com.example.vemorize.core.network.di

import com.example.vemorize.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.ktor.client.plugins.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.engine.android.Android
import kotlinx.serialization.json.Json
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            // Configure JSON serialization to include fields with default values
            defaultSerializer = KotlinXSerializer(Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
                isLenient = true
            })

            // Configure HTTP client with timeout settings for LLM requests
            httpEngine = Android.create {
                connectTimeout = 5_000   // 5 seconds to establish connection
                socketTimeout = 60_000   // 60 seconds for socket read/write (LLM calls can be slow)
            }

            // Configure request timeout (overrides Supabase's default 10s)
            requestTimeout = 60.seconds  // 60 seconds (1 minute) for LLM requests

            install(Auth)
            install(Postgrest)
        }
    }

    @Provides
    @Singleton
    fun provideSupabaseAuth(client: SupabaseClient): Auth {
        return client.pluginManager.getPlugin(Auth)
    }

    @Provides
    @Singleton
    fun provideSupabasePostgrest(client: SupabaseClient): Postgrest {
        return client.pluginManager.getPlugin(Postgrest)
    }
}
