package ci.nsu.mobile.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ci.nsu.mobile.main.auth.presentation.LoginScreen
import ci.nsu.mobile.main.auth.presentation.RegisterScreen
import ci.nsu.mobile.main.di.ServiceLocator
import ci.nsu.mobile.main.di.ViewModelFactory
import ci.nsu.mobile.main.deposit.presentation.DepositHistoryScreen
import ci.nsu.mobile.main.deposit.presentation.DepositViewModel
import ci.nsu.mobile.main.deposit.presentation.NewCalculationScreen
import ci.nsu.mobile.main.ui.theme.MyAppTheme
import ci.nsu.mobile.main.users.presentation.UsersScreen

class MainActivity : ComponentActivity() {

    private lateinit var serviceLocator: ServiceLocator
    private lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serviceLocator = ServiceLocator(applicationContext)
        viewModelFactory = ViewModelFactory(serviceLocator)

        setContent {
            MyAppTheme {
                MyApp(
                    authRepository = serviceLocator.authRepository,
                    viewModelFactory = viewModelFactory
                )
            }
        }
    }
}

@Composable
fun MyApp(
    authRepository: ci.nsu.mobile.main.auth.data.AuthRepository,
    viewModelFactory: ViewModelFactory
) {
    var isAuthenticated by remember {
        mutableStateOf(authRepository.isLoggedIn())
    }

    if (!isAuthenticated) {
        AuthNavHost(
            viewModelFactory = viewModelFactory,
            onLoginSuccess = { isAuthenticated = true }
        )
    } else {
        MainAppNavHost(
            viewModelFactory = viewModelFactory,
            onLogout = {
                authRepository.logout()
                isAuthenticated = false
            }
        )
    }
}

@Composable
fun AuthNavHost(
    viewModelFactory: ViewModelFactory,
    onLoginSuccess: () -> Unit
) {
    var currentScreen by remember { mutableStateOf("login") }

    when (currentScreen) {
        "login" -> {
            LoginScreen(
                viewModelFactory = viewModelFactory,
                onSuccess = onLoginSuccess,
                onReg = { currentScreen = "register" }
            )
        }
        "register" -> {
            RegisterScreen(
                viewModelFactory = viewModelFactory,
                onSuccess = { currentScreen = "login" },
                onBack = { currentScreen = "login" }
            )
        }
    }
}

@Composable
fun MainAppNavHost(
    viewModelFactory: ViewModelFactory,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    "Пользователи" to Icons.Default.People,
                    "Мои расчёты" to Icons.Default.List,
                    "Новый расчёт" to Icons.Default.Add
                )

                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

                items.forEach { (route, icon) ->
                    NavigationBarItem(
                        selected = currentRoute == route,
                        onClick = { navController.navigate(route) },
                        icon = { Icon(icon, contentDescription = route) },
                        label = { Text(route) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "Пользователи",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("Пользователи") {
                UsersScreen(
                    viewModelFactory = viewModelFactory,
                    onLogout = onLogout
                )
            }

            composable("Мои расчёты") {
                val viewModel: DepositViewModel = viewModel(
                    factory = viewModelFactory.createDepositViewModelFactory()
                )
                DepositHistoryScreen(viewModel = viewModel)
            }

            composable("Новый расчёт") {
                val viewModel: DepositViewModel = viewModel(
                    factory = viewModelFactory.createDepositViewModelFactory()
                )
                NewCalculationScreen(viewModel = viewModel)
            }
        }
    }
}