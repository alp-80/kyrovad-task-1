package ci.nsu.mobile.main.deposit.data.repository

import ci.nsu.mobile.main.auth.data.TokenManager
import ci.nsu.mobile.main.deposit.data.database.DepositCalculation
import ci.nsu.mobile.main.deposit.data.database.DepositDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DepositRepository(
    private val dao: DepositDao,
    private val tokenManager: TokenManager
) {

    private fun getCurrentUserId(): Long {
        return tokenManager.getUserId() ?: throw IllegalStateException("Пользователь не авторизован")
    }

    fun getUserCalculations(): Flow<List<DepositCalculation>> {
        val userId = getCurrentUserId()
        return dao.getCalculationsByUser(userId)
    }

    suspend fun saveCalculation(calculation: DepositCalculation) {
        val userId = getCurrentUserId()
        val calculationWithUser = calculation.copy(userId = userId)
        dao.insert(calculationWithUser)
    }

    suspend fun deleteCalculation(calculation: DepositCalculation) {
        dao.delete(calculation)
    }

    suspend fun clearUserCalculations() {
        val userId = getCurrentUserId()
        dao.deleteAllForUser(userId)
    }
}