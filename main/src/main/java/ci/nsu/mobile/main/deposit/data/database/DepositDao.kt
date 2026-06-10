package ci.nsu.mobile.main.deposit.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DepositDao {
    @Query("SELECT * FROM deposit_calculations WHERE userId = :userId ORDER BY calculationDate DESC")
    fun getCalculationsByUser(userId: Long): Flow<List<DepositCalculation>>

    @Insert
    suspend fun insert(calculation: DepositCalculation)

    @Delete
    suspend fun delete(calculation: DepositCalculation)

    @Query("DELETE FROM deposit_calculations WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Long)
}