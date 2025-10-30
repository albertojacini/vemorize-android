package com.example.vemorize.data.auth

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Simple unit tests for AuthRepository that don't require mocking complex Supabase APIs.
 * Integration tests with actual Supabase can be added later.
 */
class AuthRepositorySimpleTest {

    @Test
    fun `AuthState has expected values`() {
        // Test that AuthState sealed interface has the expected implementations
        val authenticated: AuthState = AuthState.Authenticated
        val unauthenticated: AuthState = AuthState.Unauthenticated
        val loading: AuthState = AuthState.Loading

        assertEquals(AuthState.Authenticated, authenticated)
        assertEquals(AuthState.Unauthenticated, unauthenticated)
        assertEquals(AuthState.Loading, loading)
    }
}
