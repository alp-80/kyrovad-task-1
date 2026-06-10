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

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadCalculations()
    }

    fun loadCalculations() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.getUserCalculations().collect { calculations ->
                    _calculations.value = calculations
                }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки: ${e.message}"
            } finally {
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
            try {
                val calculation = DepositCalculation(
                    userId = 0,
                    startAmount = startAmount,
                    termMonths = termMonths,
                    interestRate = interestRate,
                    monthlyTopUp = monthlyTopUp,
                    finalAmount = finalAmount,
                    earnedInterest = earnedInterest,
                    calculationDate = System.currentTimeMillis()
                )
                repository.saveCalculation(calculation)
                loadCalculations()
            } catch (e: Exception) {
                _error.value = "Ошибка сохранения: ${e.message}"
            }
        }
    }

    fun deleteCalculation(calculation: DepositCalculation) {
        viewModelScope.launch {
            try {
                repository.deleteCalculation(calculation)
                loadCalculations()
            } catch (e: Exception) {
                _error.value = "Ошибка удаления: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}