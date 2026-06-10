package ci.nsu.mobile.main.users.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ci.nsu.mobile.main.auth.data.AuthRepository
import ci.nsu.mobile.main.auth.data.models.UserDto
import ci.nsu.mobile.main.di.ViewModelFactory
import ci.nsu.mobile.main.users.data.UsersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UsersViewModel(
    private val usersRepository: UsersRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _users = MutableStateFlow<List<UserDto>>(emptyList())
    val users: StateFlow<List<UserDto>> = _users.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _users.value = usersRepository.getUsers()
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки пользователей: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}

@Composable
fun UsersScreen(
    viewModelFactory: ViewModelFactory,
    onLogout: () -> Unit
) {
    val usersViewModel: UsersViewModel = viewModel(factory = viewModelFactory.createUsersViewModelFactory())

    val users by usersViewModel.users.collectAsState()
    val loading by usersViewModel.loading.collectAsState()
    val error by usersViewModel.error.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Пользователи",
                style = MaterialTheme.typography.headlineSmall
            )
            Button(
                onClick = {
                    usersViewModel.logout()
                    onLogout()
                }
            ) {
                Text("Выйти")
            }
        }

        Divider()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when {
                loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { usersViewModel.loadUsers() }) {
                            Text("Повторить")
                        }
                    }
                }
                users.isEmpty() -> {
                    Text(
                        text = "Нет зарегистрированных пользователей",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn {
                        items(users) { user ->
                            UserCard(user = user)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserCard(user: UserDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = user.login,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = user.phoneNumber,
                style = MaterialTheme.typography.bodySmall
            )
            user.person?.let { person ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${person.lastName} ${person.firstName} ${person.middleName}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Дата рождения: ${person.birthDate}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Пол: ${if (person.gender == "MALE") "Мужской" else "Женский"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

fun ViewModelFactory.createUsersViewModelFactory(): androidx.lifecycle.ViewModelProvider.Factory {
    return object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return createUsersViewModel() as T
        }
    }
}