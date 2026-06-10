package ci.nsu.mobile.main.auth.data

import android.content.SharedPreferences
import ci.nsu.mobile.main.auth.data.models.*

class AuthRepository(
    private val api: ApiService,
    private val tm: TokenManager
) {

    fun isLoggedIn(): Boolean {
        return tm.token != null
    }

    suspend fun login(login: String, password: String): Boolean {
        return try {
            val res = api.login(LoginRequest(login, password))
            if (res.isSuccessful) {
                val token = res.body()?.token
                tm.token = token

                val meResponse = api.getMe()
                if (meResponse.isSuccessful) {
                    val userId = meResponse.body()?.userId?.toLong()
                    if (userId != null && tm.getUserId() == null) {
                        token?.let {
                            tm.token = it
                        }
                    }
                }
                true
            } else false
        } catch(e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun register(req: RegisterRequest): Boolean {
        return try {
            api.register(req).isSuccessful
        } catch(e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getUsers(): List<UserDto> {
        return try {
            api.getUsers().body() ?: emptyList()
        } catch(e: Exception) {
            emptyList()
        }
    }

    suspend fun getGroups(): List<GroupDto> {
        return try {
            api.getGroups().body() ?: emptyList()
        } catch(e: Exception) {
            emptyList()
        }
    }

    suspend fun getCurrentUser(): UserDto? {
        return try {
            api.getMe().body()
        } catch(e: Exception) {
            null
        }
    }

    fun logout() {
        tm.clear()
    }
}