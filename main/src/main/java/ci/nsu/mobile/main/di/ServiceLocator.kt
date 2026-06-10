package ci.nsu.mobile.main.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ci.nsu.mobile.main.auth.data.AuthRepository
import ci.nsu.mobile.main.auth.data.TokenManager
import ci.nsu.mobile.main.auth.data.createApiService
import ci.nsu.mobile.main.auth.presentation.AuthViewModel
import ci.nsu.mobile.main.auth.presentation.RegisterViewModel
import ci.nsu.mobile.main.deposit.data.database.AppDatabase
import ci.nsu.mobile.main.deposit.data.repository.DepositRepository
import ci.nsu.mobile.main.deposit.presentation.DepositViewModel
import ci.nsu.mobile.main.users.data.UsersRepository
import ci.nsu.mobile.main.users.presentation.UsersViewModel

class ServiceLocator(private val context: Context) {

    val tokenManager by lazy {
        TokenManager(context.applicationContext)
    }

    private val apiService by lazy {
        createApiService(tokenManager)
    }

    private val database by lazy {
        AppDatabase.getDatabase(context.applicationContext)
    }

    val authRepository by lazy {
        AuthRepository(apiService, tokenManager)
    }

    val usersRepository by lazy {
        UsersRepository(apiService)
    }

    val depositRepository by lazy {
        DepositRepository(database.depositDao(), tokenManager)
    }
}

class ViewModelFactory(val serviceLocator: ServiceLocator) {

    fun createAuthViewModel(): AuthViewModel {
        return AuthViewModel(serviceLocator.authRepository)
    }

    fun createRegisterViewModel(): RegisterViewModel {
        return RegisterViewModel(serviceLocator.authRepository)
    }

    fun createUsersViewModel(): UsersViewModel {
        return UsersViewModel(serviceLocator.usersRepository, serviceLocator.authRepository)
    }

    fun createDepositViewModel(): DepositViewModel {
        return DepositViewModel(serviceLocator.depositRepository)
    }

    fun createAuthViewModelFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return createAuthViewModel() as T
        }
    }

    fun createRegisterViewModelFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return createRegisterViewModel() as T
        }
    }

    fun createUsersViewModelFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return createUsersViewModel() as T
        }
    }

    fun createDepositViewModelFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return createDepositViewModel() as T
        }
    }
}