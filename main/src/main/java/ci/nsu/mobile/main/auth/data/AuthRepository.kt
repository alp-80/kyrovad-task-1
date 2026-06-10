package ci.nsu.mobile.main.auth.data

import ci.nsu.mobile.main.auth.data.models.*

class AuthRepository(
    private val api: ApiService,
    private val tm: TokenManager
) {

    suspend fun login(login: String, password: String): Boolean = try {
        val res = api.login(LoginRequest(login, password))
        if (res.isSuccessful) {
            val token = res.body()?.token
            tm.token = token

            token?.let {
                val meResponse = api.getMe()
                if (meResponse.isSuccessful) {
                    meResponse.body()?.userId?.toLong()?.let { userId ->
                        if (tm.getUserId() == null) {
                            tm.token = token
                        }
                    }
                }
            }
            true
        } else false
    } catch(e: Exception) {
        e.printStackTrace()
        false
    }

    suspend fun register(req: RegisterRequest): Boolean = try {
        api.register(req).isSuccessful
    } catch(e: Exception) {
        e.printStackTrace()
        false
    }

    suspend fun getUsers(): List<UserDto> = try {
        api.getUsers().body() ?: emptyList()
    } catch(e: Exception) {
        emptyList()
    }

    suspend fun getGroups(): List<GroupDto> = try {
        api.getGroups().body() ?: emptyList()
    } catch(e: Exception) {
        emptyList()
    }

    suspend fun getCurrentUser(): UserDto? = try {
        api.getMe().body()
    } catch(e: Exception) {
        null
    }

    fun logout() {
        tm.clear()
    }
}