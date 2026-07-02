package com.ecoguardian.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val role: String
)

class AuthRepository {
    private val client = SupabaseClient.client

    suspend fun register(email: String, password: String){
        client.auth.signUpWith(Email){
            this.email = email
            this.password = password
        }
    }

    suspend fun login(email: String, password: String){
        client.auth.signInWith(Email){
            this.email = email
            this.password = password
        }
    }

    suspend fun logout(){
        client.auth.signOut()
    }

    suspend fun getUserRole(): String{
        val userId = client.auth.currentUserOrNull()?.id ?: return "user"
        val profile = client.postgrest["profiles"]
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeSingle<Profile>()
        return profile.role
    }

    fun isLoggedIn(): Boolean{
        return client.auth.currentUserOrNull() != null
    }
}