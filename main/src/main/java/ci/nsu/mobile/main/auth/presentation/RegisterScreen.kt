package ci.nsu.mobile.main.auth.presentation

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ci.nsu.mobile.main.auth.data.AuthRepository
import ci.nsu.mobile.main.auth.data.models.PersonDto
import ci.nsu.mobile.main.auth.data.models.RegisterRequest
import ci.nsu.mobile.main.auth.data.models.GroupDto
import ci.nsu.mobile.main.di.ViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repository: AuthRepository
) : ViewModel() {
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _groups = MutableStateFlow<List<GroupDto>>(emptyList())
    val groups: StateFlow<List<GroupDto>> = _groups.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadGroups()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            _groups.value = repository.getGroups()
        }
    }

    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            val result = repository.register(request)
            _success.value = result
            if (!result) _error.value = "Ошибка регистрации. Возможно, такой пользователь уже существует"
            _loading.value = false
        }
    }

    fun clear() {
        _error.value = null
        _success.value = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModelFactory: ViewModelFactory,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val registerViewModel: RegisterViewModel = viewModel(factory = viewModelFactory.createRegisterViewModelFactory())

    // Personal info
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var groupId by remember { mutableStateOf(0) }

    // Account info
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    val loading by registerViewModel.loading.collectAsState()
    val groups by registerViewModel.groups.collectAsState()
    val success by registerViewModel.success.collectAsState()
    val error by registerViewModel.error.collectAsState()

    LaunchedEffect(success) {
        if (success) {
            registerViewModel.clear()
            onSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Регистрация",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Личные данные", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Фамилия") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("Имя") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = middleName,
            onValueChange = { middleName = it },
            label = { Text("Отчество") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = birthDate,
            onValueChange = { birthDate = it },
            label = { Text("Дата рождения (ГГГГ-ММ-ДД)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        var genderExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = genderExpanded,
            onExpandedChange = { genderExpanded = it }
        ) {
            TextField(
                value = when(gender) {
                    "MALE" -> "Мужской"
                    "FEMALE" -> "Женский"
                    else -> ""
                },
                onValueChange = {},
                readOnly = true,
                label = { Text("Пол") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                enabled = !loading
            )
            DropdownMenu(
                expanded = genderExpanded,
                onDismissRequest = { genderExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Мужской") },
                    onClick = {
                        gender = "MALE"
                        genderExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Женский") },
                    onClick = {
                        gender = "FEMALE"
                        genderExpanded = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        var groupExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = groupExpanded,
            onExpandedChange = { groupExpanded = it }
        ) {
            val selectedGroup = groups.find { it.groupId == groupId }?.groupName ?: ""
            TextField(
                value = selectedGroup,
                onValueChange = {},
                readOnly = true,
                label = { Text("Группа") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                enabled = !loading
            )
            DropdownMenu(
                expanded = groupExpanded,
                onDismissRequest = { groupExpanded = false }
            ) {
                groups.forEach { group ->
                    DropdownMenuItem(
                        text = { Text(group.groupName) },
                        onClick = {
                            groupId = group.groupId
                            groupExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Данные для входа", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("Логин") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Телефон") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val person = PersonDto(
                    firstName = firstName,
                    lastName = lastName,
                    middleName = middleName,
                    birthDate = birthDate,
                    gender = gender,
                    groupId = groupId
                )
                val request = RegisterRequest(
                    login = login,
                    password = password,
                    email = email,
                    phoneNumber = phoneNumber,
                    roleId = 1,
                    authAllowed = true,
                    person = person
                )
                registerViewModel.register(request)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading && login.isNotBlank() && password.isNotBlank()
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Зарегистрироваться")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text("Назад к входу")
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error!!,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

fun ViewModelFactory.createRegisterViewModelFactory(): androidx.lifecycle.ViewModelProvider.Factory {
    return object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return createRegisterViewModel() as T
        }
    }
}