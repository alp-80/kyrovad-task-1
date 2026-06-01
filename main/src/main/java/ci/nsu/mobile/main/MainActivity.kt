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
    var token: String? get() = prefs.getString("token", null) set(value) { prefs.edit().putString("token", value).apply() }
    fun clear() { prefs.edit().clear().apply() }
}

class AuthRepository(val api: ApiService, val tm: TokenManager) {
    suspend fun login(login: String, password: String): Boolean = try {
        val res = api.login(LoginRequest(login, password))
        if (res.isSuccessful) { tm.token = res.body()?.token; true } else false
    } catch(e: Exception) { false }

    suspend fun register(req: RegisterRequest): Boolean = try { api.register(req).isSuccessful } catch(e: Exception) { false }
    suspend fun getUsers(): List<UserDto> = try { api.getUsers().body() ?: emptyList() } catch(e: Exception) { emptyList() }
    suspend fun getGroups(): List<GroupDto> = try { api.getGroups().body() ?: emptyList() } catch(e: Exception) { emptyList() }
    fun logout() { tm.clear() }
}

class LoginViewModel(val repo: AuthRepository) : ViewModel() {
    private val _loading = MutableStateFlow(false); val loading: StateFlow<Boolean> = _loading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null); val error: StateFlow<String?> = _error.asStateFlow()
    private val _success = MutableStateFlow(false); val success: StateFlow<Boolean> = _success.asStateFlow()

    fun login(login: String, pass: String) { viewModelScope.launch {
        _loading.value = true
        val result = repo.login(login, pass)
        if (result) _success.value = true else _error.value = "Ошибка входа"
        _loading.value = false
    }}
    fun clearError() { _error.value = null }
    fun clearSuccess() { _success.value = false }
}

class RegisterViewModel(val repo: AuthRepository) : ViewModel() {
    private val _loading = MutableStateFlow(false); val loading: StateFlow<Boolean> = _loading.asStateFlow()
    private val _groups = MutableStateFlow<List<GroupDto>>(emptyList()); val groups: StateFlow<List<GroupDto>> = _groups.asStateFlow()
    private val _success = MutableStateFlow(false); val success: StateFlow<Boolean> = _success.asStateFlow()
    private val _error = MutableStateFlow<String?>(null); val error: StateFlow<String?> = _error.asStateFlow()

    init { viewModelScope.launch { _groups.value = repo.getGroups() } }

    fun register(req: RegisterRequest) { viewModelScope.launch {
        _loading.value = true
        val result = repo.register(req)
        if (result) _success.value = true else _error.value = "Ошибка регистрации"
        _loading.value = false
    }}
    fun clearSuccess() { _success.value = false }
    fun clearError() { _error.value = null }
}

class UsersViewModel(val repo: AuthRepository) : ViewModel() {
    private val _users = MutableStateFlow<List<UserDto>>(emptyList()); val users: StateFlow<List<UserDto>> = _users.asStateFlow()
    private val _loading = MutableStateFlow(true); val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init { load() }
    fun load() { viewModelScope.launch { _loading.value = true; _users.value = repo.getUsers(); _loading.value = false } }
    fun logout() { repo.logout() }
}

fun getApiService(tm: TokenManager): ApiService {
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val tm = remember { TokenManager(context) }
    val api = remember { getApiService(tm) }
    val repo = remember { AuthRepository(api, tm) }
    val vm: LoginViewModel = viewModel(factory = androidx.lifecycle.ViewModelProvider.Factory { LoginViewModel(repo) })

    var login by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val success by vm.success.collectAsState()

    LaunchedEffect(success) { if (success) { vm.clearSuccess(); onSuccess() } }
    LaunchedEffect(error) { if (error != null) { kotlinx.coroutines.delay(2000); vm.clearError() } }

    Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center) {
        Text("Вход", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(login, { login = it }, label = { Text("Логин") }, Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(pass, { pass = it }, label = { Text("Пароль") }, visualTransformation = PasswordVisualTransformation(), Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
        Button({ vm.login(login, pass) }, Modifier.fillMaxWidth(), enabled = !loading) {
            if (loading) CircularProgressIndicator(Modifier.size(20.dp)) else Text("Войти")
        }
        TextButton({ onReg() }, Modifier.fillMaxWidth()) { Text("Нет аккаунта? Зарегистрироваться") }
        if (error != null) Snackbar(Modifier.padding(top = 16.dp)) { Text(error!!) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onSuccess: () -> Unit, onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val tm = remember { TokenManager(context) }
    val api = remember { getApiService(tm) }
    val repo = remember { AuthRepository(api, tm) }
    val vm: RegisterViewModel = viewModel(factory = androidx.lifecycle.ViewModelProvider.Factory { RegisterViewModel(repo) })

    var fn by remember { mutableStateOf("") }; var ln by remember { mutableStateOf("") }; var mn by remember { mutableStateOf("") }
    var bd by remember { mutableStateOf("") }; var gender by remember { mutableStateOf("") }; var gid by remember { mutableStateOf(0) }
    var login by remember { mutableStateOf("") }; var pass by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }; var phone by remember { mutableStateOf("") }

    val loading by vm.loading.collectAsState(); val groups by vm.groups.collectAsState()
    val success by vm.success.collectAsState(); val error by vm.error.collectAsState()

    LaunchedEffect(success) { if (success) { vm.clearSuccess(); onSuccess() } }
    LaunchedEffect(error) { if (error != null) { kotlinx.coroutines.delay(2000); vm.clearError() } }

    Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Регистрация", style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.height(16.dp))
        OutlinedTextField(ln, { ln = it }, { Text("Фамилия") }, Modifier.fillMaxWidth())
        OutlinedTextField(fn, { fn = it }, { Text("Имя") }, Modifier.fillMaxWidth())
        OutlinedTextField(mn, { mn = it }, { Text("Отчество") }, Modifier.fillMaxWidth())
        OutlinedTextField(bd, { bd = it }, { Text("Дата рождения (ГГГГ-ММ-ДД)") }, Modifier.fillMaxWidth())

        var ge by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(ge, { ge = it }) {
            TextField(value = when(gender){ "MALE"->"Мужской"; "FEMALE"->"Женский"; else->"" }, onValueChange = {}, readOnly = true, label = { Text("Пол") }, modifier = Modifier.fillMaxWidth().menuAnchor())
            DropdownMenu(ge, { ge = false }) {
                DropdownMenuItem({ Text("Мужской") }, onClick = { gender = "MALE"; ge = false })
                DropdownMenuItem({ Text("Женский") }, onClick = { gender = "FEMALE"; ge = false })
            }
        }

        var gex by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(gex, { gex = it }) {
            TextField(value = groups.find { it.groupId == gid }?.groupName ?: "", onValueChange = {}, readOnly = true, label = { Text("Группа") }, modifier = Modifier.fillMaxWidth().menuAnchor())
            DropdownMenu(gex, { gex = false }) { groups.forEach { DropdownMenuItem({ Text(it.groupName) }, onClick = { gid = it.groupId; gex = false }) } }
        }

        OutlinedTextField(login, { login = it }, { Text("Логин") }, Modifier.fillMaxWidth())
        OutlinedTextField(pass, { pass = it }, { Text("Пароль") }, Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        OutlinedTextField(email, { email = it }, { Text("Email") }, Modifier.fillMaxWidth())
        OutlinedTextField(phone, { phone = it }, { Text("Телефон") }, Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))

        Button({ vm.register(RegisterRequest(login, pass, email, phone, 1, true, PersonDto(fn, ln, mn, bd, gender, gid))) }, Modifier.fillMaxWidth(), enabled = !loading) {
            if (loading) CircularProgressIndicator(Modifier.size(20.dp)) else Text("Зарегистрироваться")
        }
        TextButton({ onBack() }, Modifier.fillMaxWidth()) { Text("Назад") }
        if (error != null) Snackbar(Modifier.padding(top = 16.dp)) { Text(error!!) }
    }
}

@Composable
fun UsersScreen(onLogout: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val tm = remember { TokenManager(context) }
    val api = remember { getApiService(tm) }
    val repo = remember { AuthRepository(api, tm) }
    val vm: UsersViewModel = viewModel(factory = androidx.lifecycle.ViewModelProvider.Factory { UsersViewModel(repo) })

    val users by vm.users.collectAsState(); val loading by vm.loading.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Пользователи") }, actions = { Button({ vm.logout(); onLogout() }) { Text("Выйти") } }) }) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            if (loading) CircularProgressIndicator(Modifier.align(Alignment.Center))
            else if (users.isEmpty()) Text("Нет пользователей", Modifier.align(Alignment.Center))
            else LazyColumn { items(users) { user ->
                Card(Modifier.fillMaxWidth().padding(8.dp)) { Column(Modifier.padding(16.dp)) {
                    Text(user.login, style = MaterialTheme.typography.titleMedium)
                    Text(user.email); Text(user.phoneNumber)
                    user.person?.let { Text("${it.lastName} ${it.firstName} ${it.middleName}") }
                } }
            } }
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
                    "login" -> LoginScreen({ screen = "users" }, { screen = "register" })
                    "register" -> RegisterScreen({ screen = "login" }, { screen = "login" })
                    "users" -> UsersScreen({ screen = "login" })
                }
            }
        }
    }
}