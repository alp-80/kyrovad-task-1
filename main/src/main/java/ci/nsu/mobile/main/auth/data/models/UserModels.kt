package ci.nsu.mobile.main.auth.data.models

data class UserDto(
    val userId: Int,
    val login: String,
    val email: String,
    val phoneNumber: String,
    val person: PersonDto?
)

data class GroupDto(
    val groupId: Int,
    val groupName: String
)