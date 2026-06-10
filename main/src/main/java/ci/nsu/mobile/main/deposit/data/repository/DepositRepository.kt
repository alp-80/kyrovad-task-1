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
        return tokenManager.getUserId() ?: 1L
    }

    fun getUserCalculations(): Flow<List<DepositCalculation>> {
        val userId = getCurrentUserId()
        return dao.getCalculationsByUser(userId)
    }

    suspend fun saveCalculation(calculation: DepositCalculation) {
        val calculationWithUser = calculation.copy(userId = getCurrentUserId())
        dao.insert(calculationWithUser)
    }

    suspend fun deleteCalculation(calculation: DepositCalculation) {
        dao.delete(calculation)
    }

    suspend fun clearUserCalculations() {
        dao.deleteAllForUser(getCurrentUserId())
    }
}