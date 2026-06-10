package ci.nsu.mobile.main.auth.data

import ci.nsu.mobile.main.auth.data.models.*

class AuthRepository(
    private val api: ApiService,
    private val tm: TokenManager
) {

    suspend fun login(login: String, password: String): Boolean {
        return try {
            val res = api.login(LoginRequest(login, password))
            if (res.isSuccessful) {
                val token = res.body()?.token
                tm.token = token
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