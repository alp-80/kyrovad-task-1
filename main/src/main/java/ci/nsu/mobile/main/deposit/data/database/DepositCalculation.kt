package ci.nsu.mobile.main.deposit.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deposit_calculations")
data class DepositCalculation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val startAmount: Double,
    val termMonths: Int,
    val interestRate: Double,
    val monthlyTopUp: Double,
    val finalAmount: Double,
    val earnedInterest: Double,
    val calculationDate: Long = System.currentTimeMillis()
) {
    val formattedDate: String get() = android.text.format.DateFormat.format("dd.MM.yyyy HH:mm", calculationDate).toString()
}