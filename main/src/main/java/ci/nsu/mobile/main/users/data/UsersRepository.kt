package ci.nsu.mobile.main.users.data

import ci.nsu.mobile.main.auth.data.ApiService
import ci.nsu.mobile.main.auth.data.models.UserDto
import ci.nsu.mobile.main.auth.data.models.GroupDto

class UsersRepository(
    private val api: ApiService
) {
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
}