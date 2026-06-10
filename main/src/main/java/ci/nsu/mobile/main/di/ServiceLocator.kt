package ci.nsu.mobile.main.di

import android.content.Context
import ci.nsu.mobile.main.auth.data.AuthRepository
import ci.nsu.mobile.main.auth.data.TokenManager
import ci.nsu.mobile.main.auth.data.createApiService
import ci.nsu.mobile.main.deposit.data.database.AppDatabase
import ci.nsu.mobile.main.deposit.data.repository.DepositRepository
import ci.nsu.mobile.main.users.data.UsersRepository

class ServiceLocator(private val context: Context) {

    private val tokenManager by lazy {
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
    fun createAuthViewModel(): AuthViewModel = AuthViewModel(serviceLocator.authRepository)
    fun createRegisterViewModel(): RegisterViewModel = RegisterViewModel(serviceLocator.authRepository)
    fun createUsersViewModel(): UsersViewModel = UsersViewModel(serviceLocator.usersRepository, serviceLocator.authRepository)
    fun createDepositViewModel(): DepositViewModel = DepositViewModel(serviceLocator.depositRepository)
}