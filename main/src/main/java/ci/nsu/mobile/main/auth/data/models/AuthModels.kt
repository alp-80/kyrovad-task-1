package ci.nsu.mobile.main.auth.data.models

data class LoginRequest(
    val login: String,
    val password: String
)

data class LoginResponse(
    val token: String
)

data class RegisterRequest(
    val login: String,
    val password: String,
    val email: String,
    val phoneNumber: String,
    val roleId: Int = 1,
    val authAllowed: Boolean = true,
    val person: PersonDto
)

data class PersonDto(
    val firstName: String,
    val lastName: String,
    val middleName: String,
    val birthDate: String,
    val gender: String,
    val groupId: Int
)