package ci.nsu.mobile.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import retrofit2.Response

data class LoginRequest(val login: String, val password: String)
data class LoginResponse(val token: String)
data class RegisterRequest(val login: String, val password: String, val email: String, val phoneNumber: String, val roleId: Int = 1, val authAllowed: Boolean = true, val person: PersonDto)
data class PersonDto(val firstName: String, val lastName: String, val middleName: String, val birthDate: String, val gender: String, val groupId: Int)
data class UserDto(val userId: Int, val login: String, val email: String, val phoneNumber: String, val person: PersonDto?)
data class GroupDto(val groupId: Int, val groupName: String)

interface ApiService {
    @POST("auth/login") suspend fun login(@Body req: LoginRequest): Response<LoginResponse>
    @POST("auth/register") suspend fun register(@Body req: RegisterRequest): Response<Unit>
    @GET("users") suspend fun getUsers(): Response<List<UserDto>>
    @GET("groups") suspend fun getGroups(): Response<List<GroupDto>>
}

class TokenManager(context: android.content.Context) {
    private val prefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString("token", null)
        set(value) {
            prefs.edit().putString("token", value).apply()
        }

    fun clear() {
        prefs.edit().clear().apply()
    }
}

class AuthRepository(val api: ApiService, val tm: TokenManager) {
    suspend fun login(login: String, password: String): Boolean = try {
        val res = api.login(LoginRequest(login, password))
        if (res.isSuccessful) { tm.token = res.body()?.token; true } else false
    } catch(e: Exception) { false }

    suspend fun register(req: RegisterRequest): Boolean = try {
        api.register(req).isSuccessful
    } catch(e: Exception) { false }

    suspend fun getUsers(): List<UserDto> = try {
        api.getUsers().body() ?: emptyList()
    } catch(e: Exception) { emptyList() }

    suspend fun getGroups(): List<GroupDto> = try {
        api.getGroups().body() ?: emptyList()
    } catch(e: Exception) { emptyList() }

    fun logout() { tm.clear() }
}

class LoginViewModel(private val repo: AuthRepository) : ViewModel() {
    private val _loading = MutableStateFlow(false); val loading: StateFlow<Boolean> = _loading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null); val error: StateFlow<String?> = _error.asStateFlow()
    private val _success = MutableStateFlow(false); val success: StateFlow<Boolean> = _success.asStateFlow()

    fun login(login: String, pass: String) { viewModelScope.launch {
        _loading.value = true
        val result = repo.login(login, pass)
        _success.value = result
        if (!result) _error.value = "Ошибка входа"
        _loading.value = false
    }}
    fun clear() { _error.value = null; _success.value = false }
}

class RegisterViewModel(private val repo: AuthRepository) : ViewModel() {
    private val _loading = MutableStateFlow(false); val loading: StateFlow<Boolean> = _loading.asStateFlow()
    private val _groups = MutableStateFlow<List<GroupDto>>(emptyList()); val groups: StateFlow<List<GroupDto>> = _groups.asStateFlow()
    private val _success = MutableStateFlow(false); val success: StateFlow<Boolean> = _success.asStateFlow()
    private val _error = MutableStateFlow<String?>(null); val error: StateFlow<String?> = _error.asStateFlow()

    init { viewModelScope.launch { _groups.value = repo.getGroups() } }

    fun register(req: RegisterRequest) { viewModelScope.launch {
        _loading.value = true
        val result = repo.register(req)
        _success.value = result
        if (!result) _error.value = "Ошибка регистрации"
        _loading.value = false
    }}
    fun clear() { _error.value = null; _success.value = false }
}

class UsersViewModel(private val repo: AuthRepository) : ViewModel() {
    private val _users = MutableStateFlow<List<UserDto>>(emptyList()); val users: StateFlow<List<UserDto>> = _users.asStateFlow()
    private val _loading = MutableStateFlow(true); val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init { load() }
    fun load() { viewModelScope.launch { _loading.value = true; _users.value = repo.getUsers(); _loading.value = false } }
    fun logout() { repo.logout() }
}

fun createApiService(tm: TokenManager): ApiService {
    val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val req = chain.request().newBuilder().addHeader("Content-Type", "application/json")
            tm.token?.let { req.addHeader("Authorization", "Bearer $it") }
            chain.proceed(req.build())
        }
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()
    return Retrofit.Builder()
        .baseUrl("http://192.168.200.160:8080/api/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}

@Composable
fun LoginScreen(onSuccess: () -> Unit, onReg: () -> Unit) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val tm = remember { TokenManager(ctx.applicationContext) }
    val api = remember { createApiService(tm) }
    val repo = remember { AuthRepository(api, tm) }

    val vm: LoginViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(repo) as T
        }
    })

    var login by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val success by vm.success.collectAsState()

    LaunchedEffect(success) { if (success) { vm.clear(); onSuccess() } }

    Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center) {
        Text("Вход", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(value = login, onValueChange = { login = it }, label = { Text("Логин") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Пароль") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        Spacer(Modifier.height(24.dp))
        Button(onClick = { vm.login(login, pass) }, modifier = Modifier.fillMaxWidth(), enabled = !loading) {
            if (loading) CircularProgressIndicator(Modifier.size(20.dp)) else Text("Войти")
        }
        TextButton(onClick = onReg, modifier = Modifier.fillMaxWidth()) { Text("Нет аккаунта? Зарегистрироваться") }
        if (error != null) { Text(text = error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onSuccess: () -> Unit, onBack: () -> Unit) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val tm = remember { TokenManager(ctx.applicationContext) }
    val api = remember { createApiService(tm) }
    val repo = remember { AuthRepository(api, tm) }

    val vm: RegisterViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RegisterViewModel(repo) as T
        }
    })

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var groupId by remember { mutableStateOf(0) }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    val loading by vm.loading.collectAsState()
    val groups by vm.groups.collectAsState()
    val success by vm.success.collectAsState()
    val error by vm.error.collectAsState()

    LaunchedEffect(success) { if (success) { vm.clear(); onSuccess() } }

    Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Регистрация", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Фамилия") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Имя") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = middleName, onValueChange = { middleName = it }, label = { Text("Отчество") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = birthDate, onValueChange = { birthDate = it }, label = { Text("Дата рождения (ГГГГ-ММ-ДД)") }, modifier = Modifier.fillMaxWidth())

        var genderExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = genderExpanded, onExpandedChange = { genderExpanded = it }) {
            TextField(value = when(gender) { "MALE" -> "Мужской"; "FEMALE" -> "Женский"; else -> "" }, onValueChange = {}, readOnly = true, label = { Text("Пол") }, modifier = Modifier.fillMaxWidth().menuAnchor())
            DropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                DropdownMenuItem(text = { Text("Мужской") }, onClick = { gender = "MALE"; genderExpanded = false })
                DropdownMenuItem(text = { Text("Женский") }, onClick = { gender = "FEMALE"; genderExpanded = false })
            }
        }

        var groupExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = groupExpanded, onExpandedChange = { groupExpanded = it }) {
            val selectedGroup = groups.find { it.groupId == groupId }?.groupName ?: ""
            TextField(value = selectedGroup, onValueChange = {}, readOnly = true, label = { Text("Группа") }, modifier = Modifier.fillMaxWidth().menuAnchor())
            DropdownMenu(expanded = groupExpanded, onDismissRequest = { groupExpanded = false }) {
                groups.forEach { group ->
                    DropdownMenuItem(text = { Text(group.groupName) }, onClick = { groupId = group.groupId; groupExpanded = false })
                }
            }
        }

        OutlinedTextField(value = login, onValueChange = { login = it }, label = { Text("Логин") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Пароль") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Телефон") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))

        Button(onClick = {
            val person = PersonDto(firstName, lastName, middleName, birthDate, gender, groupId)
            val request = RegisterRequest(login, password, email, phoneNumber, 1, true, person)
            vm.register(request)
        }, modifier = Modifier.fillMaxWidth(), enabled = !loading) {
            if (loading) CircularProgressIndicator(Modifier.size(20.dp)) else Text("Зарегистрироваться")
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Назад") }
        if (error != null) { Text(text = error!!, color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
fun UsersScreen(onLogout: () -> Unit) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val tm = remember { TokenManager(ctx.applicationContext) }
    val api = remember { createApiService(tm) }
    val repo = remember { AuthRepository(api, tm) }

    val vm: UsersViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UsersViewModel(repo) as T
        }
    })

    val users by vm.users.collectAsState()
    val loading by vm.loading.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Пользователи", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = { vm.logout(); onLogout() }) { Text("Выйти") }
        }

        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (users.isEmpty()) {
                Text(text = "Нет пользователей", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn { items(users) { user ->
                    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = user.login, style = MaterialTheme.typography.titleMedium)
                            Text(text = user.email)
                            Text(text = user.phoneNumber)
                            user.person?.let {
                                Text(text = "${it.lastName} ${it.firstName} ${it.middleName}")
                            }
                        }
                    }
                } }
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var screen by remember { mutableStateOf("login") }
                when (screen) {
                    "login" -> LoginScreen(onSuccess = { screen = "users" }, onReg = { screen = "register" })
                    "register" -> RegisterScreen(onSuccess = { screen = "login" }, onBack = { screen = "login" })
                    "users" -> UsersScreen(onLogout = { screen = "login" })
                }
            }
        }
    }
}