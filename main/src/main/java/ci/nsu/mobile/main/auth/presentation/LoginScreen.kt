package ci.nsu.mobile.main.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ci.nsu.mobile.main.auth.data.AuthRepository
import ci.nsu.mobile.main.di.ViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ci.nsu.mobile.main.auth.data.models.LoginRequest
import ci.nsu.mobile.main.auth.data.models.RegisterRequest
import ci.nsu.mobile.main.auth.data.models.PersonDto
import ci.nsu.mobile.main.auth.data.models.GroupDto

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success.asStateFlow()

    fun login(login: String, pass: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            val result = repository.login(login, pass)
            _success.value = result
            if (!result) _error.value = "Ошибка входа. Проверьте логин и пароль"
            _loading.value = false
        }
    }

    fun clear() {
        _error.value = null
        _success.value = false
    }

}

@Composable
fun LoginScreen(
    viewModelFactory: ViewModelFactory,
    onSuccess: () -> Unit,
    onReg: () -> Unit
) {
    val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory.createAuthViewModelFactory())

    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loading by authViewModel.loading.collectAsState()
    val error by authViewModel.error.collectAsState()
    val success by authViewModel.success.collectAsState()

    LaunchedEffect(success) {
        if (success) {
            authViewModel.clear()
            onSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Расчёт вкладов",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Вход в систему",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("Логин") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            enabled = !loading
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { authViewModel.login(login, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Войти")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onReg,
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text("Нет аккаунта? Зарегистрироваться")
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

fun ViewModelFactory.createAuthViewModelFactory(): androidx.lifecycle.ViewModelProvider.Factory {
    return object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return createAuthViewModel() as T
        }
    }
}