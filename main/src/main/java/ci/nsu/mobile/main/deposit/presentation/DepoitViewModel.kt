package ci.nsu.mobile.main.deposit.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ci.nsu.mobile.main.deposit.data.database.DepositCalculation
import ci.nsu.mobile.main.deposit.data.repository.DepositRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DepositViewModel(
    private val repository: DepositRepository
) : ViewModel() {

    private val _calculations = MutableStateFlow<List<DepositCalculation>>(emptyList())
    val calculations: StateFlow<List<DepositCalculation>> = _calculations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadCalculations()
    }

    fun loadCalculations() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getUserCalculations().collect { calculations ->
                _calculations.value = calculations
                _isLoading.value = false
            }
        }
    }

    fun saveCalculation(
        startAmount: Double,
        termMonths: Int,
        interestRate: Double,
        monthlyTopUp: Double,
        finalAmount: Double,
        earnedInterest: Double
    ) {
        viewModelScope.launch {
            val calculation = DepositCalculation(
                userId = 0,
                startAmount = startAmount,
                termMonths = termMonths,
                interestRate = interestRate,
                monthlyTopUp = monthlyTopUp,
                finalAmount = finalAmount,
                earnedInterest = earnedInterest
            )
            repository.saveCalculation(calculation)
        }
    }

    fun deleteCalculation(calculation: DepositCalculation) {
        viewModelScope.launch {
            repository.deleteCalculation(calculation)
        }
    }
}