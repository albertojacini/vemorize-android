package com.example.vemorize.data.auth

import com.example.vemorize.core.di.ApplicationScope
import com.example.vemorize.domain.auth.User
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: Auth,
    @ApplicationScope private val scope: CoroutineScope
) : AuthRepository {

    override val authState: StateFlow<AuthState> = auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> AuthState.Authenticated
            is SessionStatus.NotAuthenticated -> AuthState.Unauthenticated
            else -> AuthState.Loading
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AuthState.Loading
    )

    override suspend fun signUp(email: String, password: String): Result<User> {
        return try {
            val userInfo = auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            if (userInfo != null) {
                Result.success(
                    User(
                        id = userInfo.id,
                        email = userInfo.email ?: email
                    )
                )
            } else {
                Result.failure(Exception("Sign up failed - no user returned"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val userInfo = auth.currentUserOrNull()
            if (userInfo != null) {
                Result.success(
                    User(
                        id = userInfo.id,
                        email = userInfo.email ?: email
                    )
                )
            } else {
                Result.failure(Exception("User not found after sign in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override fun getCurrentUser(): User? {
        val userInfo = auth.currentUserOrNull()
        return userInfo?.let {
            User(
                id = it.id,
                email = it.email ?: ""
            )
        }
    }
}
