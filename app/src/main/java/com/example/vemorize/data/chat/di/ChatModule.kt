package com.example.vemorize.data.chat.di

import com.example.vemorize.data.auth.AuthRepository
import com.example.vemorize.data.chat.*
import com.example.vemorize.data.courses.CoursesRepository
import com.example.vemorize.data.courses.CourseTreeRepository
import com.example.vemorize.domain.chat.ChatManager
import com.example.vemorize.domain.chat.actions.Actions
import com.example.vemorize.domain.chat.actions.ToolRegistry
import com.example.vemorize.domain.chat.managers.NavigationManager
import com.example.vemorize.domain.chat.managers.UserMemoryManager
import com.example.vemorize.domain.chat.managers.UserPreferencesManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindUserMemoryRepository(
        impl: UserMemoryRepositoryImpl
    ): UserMemoryRepository

    @Binds
    @Singleton
    abstract fun bindNavigationRepository(
        impl: NavigationRepositoryImpl
    ): NavigationRepository
}

@Module
@InstallIn(SingletonComponent::class)
object ChatUtilModule {

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    @Provides
    @Singleton
    fun provideChatApiClient(
        supabaseClient: SupabaseClient,
        json: Json
    ): ChatApiClient {
        return ChatApiClient(supabaseClient, json)
    }
}

@Module
@InstallIn(ViewModelComponent::class)
object ChatManagerModule {

    @Provides
    @ViewModelScoped
    fun provideUserId(authRepository: AuthRepository): String {
        return authRepository.getCurrentUser()?.id
            ?: throw IllegalStateException("No authenticated user")
    }

    @Provides
    @ViewModelScoped
    fun provideUserPreferencesManager(
        userPreferencesRepository: UserPreferencesRepository,
        userId: String
    ): UserPreferencesManager {
        return UserPreferencesManager(userPreferencesRepository, userId)
    }

    @Provides
    @ViewModelScoped
    fun provideUserMemoryManager(
        userMemoryRepository: UserMemoryRepository,
        userId: String
    ): UserMemoryManager {
        return UserMemoryManager(userMemoryRepository, userId)
    }

    @Provides
    @ViewModelScoped
    fun provideNavigationManager(
        navigationRepository: NavigationRepository,
        coursesRepository: CoursesRepository,
        courseTreeRepository: CourseTreeRepository,
        userId: String
    ): NavigationManager {
        return NavigationManager(navigationRepository, coursesRepository, courseTreeRepository, userId)
    }

    @Provides
    @ViewModelScoped
    fun provideActions(
        navigationManager: NavigationManager
    ): Actions {
        return Actions(navigationManager)
    }

    @Provides
    @ViewModelScoped
    fun provideToolRegistry(
        actions: Actions
    ): ToolRegistry {
        return ToolRegistry(actions)
    }

    @Provides
    @ViewModelScoped
    fun provideChatManager(
        navigationManager: NavigationManager,
        userMemoryManager: UserMemoryManager,
        userPreferencesManager: UserPreferencesManager,
        chatApiClient: ChatApiClient,
        actions: Actions,
        toolRegistry: ToolRegistry,
        userId: String
    ): ChatManager {
        return ChatManager(
            navigationManager,
            userMemoryManager,
            userPreferencesManager,
            chatApiClient,
            actions,
            toolRegistry,
            userId
        )
    }
}
