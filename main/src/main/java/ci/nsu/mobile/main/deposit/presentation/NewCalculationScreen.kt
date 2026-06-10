package ci.nsu.mobile.main.deposit.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import android.widget.Toast

@Composable
fun NewCalculationScreen(viewModel: DepositViewModel) {
    var stage by remember { mutableStateOf(1) }

    var startAmount by remember { mutableStateOf("") }
    var termMonths by remember { mutableStateOf("") }

    var selectedRate by remember { mutableStateOf<Double?>(null) }
    var monthlyTopUp by remember { mutableStateOf("") }

    val context = LocalContext.current

    val start = startAmount.toDoubleOrNull() ?: 0.0
    val months = termMonths.toIntOrNull() ?: 0
    val rate = selectedRate ?: 0.0
    val topUp = monthlyTopUp.toDoubleOrNull() ?: 0.0

    val totalInterest = if (months > 0 && rate > 0) {
        val monthlyRate = rate / 100 / 12
        var total = start
        var interest = 0.0
        repeat(months) { month ->
            val monthInterest = total * monthlyRate
            interest += monthInterest
            total += monthInterest
            if (month < months - 1) total += topUp
        }
        interest
    } else 0.0

    val finalAmount = start + totalInterest + (topUp * (months - 1).coerceAtLeast(0))

    when (stage) {
        1 -> Stage1Screen(
            startAmount = startAmount,
            onStartAmountChange = { startAmount = it },
            termMonths = termMonths,
            onTermMonthsChange = { termMonths = it },
            onNext = {
                if (startAmount.toDoubleOrNull() != null && termMonths.toIntOrNull() != null) {
                    stage = 2
                } else {
                    Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
                }
            }
        )

        2 -> Stage2Screen(
            months = months,
            selectedRate = selectedRate,
            onRateSelected = { selectedRate = it },
            monthlyTopUp = monthlyTopUp,
            onMonthlyTopUpChange = { monthlyTopUp = it },
            onBack = { stage = 1 },
            onCalculate = {
                if (months > 0 && selectedRate != null) {
                    stage = 3
                } else {
                    Toast.makeText(context, "Выберите процентную ставку", Toast.LENGTH_SHORT).show()
                }
            }
        )

        3 -> ResultScreen(
            startAmount = start,
            months = months,
            rate = rate,
            monthlyTopUp = topUp,
            finalAmount = finalAmount,
            earnedInterest = totalInterest,
            onSave = {
                viewModel.saveCalculation(
                    startAmount = start,
                    termMonths = months,
                    interestRate = rate,
                    monthlyTopUp = topUp,
                    finalAmount = finalAmount,
                    earnedInterest = totalInterest
                )
                Toast.makeText(context, "Расчёт сохранён", Toast.LENGTH_SHORT).show()
                stage = 1
                startAmount = ""
                termMonths = ""
                selectedRate = null
                monthlyTopUp = ""
            },
            onNew = {
                stage = 1
                startAmount = ""
                termMonths = ""
                selectedRate = null
                monthlyTopUp = ""
            }
        )
    }
}

@Composable
fun Stage1Screen(
    startAmount: String,
    onStartAmountChange: (String) -> Unit,
    termMonths: String,
    onTermMonthsChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Основные параметры",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = startAmount,
            onValueChange = onStartAmountChange,
            label = { Text("Стартовый взнос (руб)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = termMonths,
            onValueChange = onTermMonthsChange,
            label = { Text("Срок вклада (месяцев)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Далее")
        }
    }
}

@Composable
fun Stage2Screen(
    months: Int,
    selectedRate: Double?,
    onRateSelected: (Double) -> Unit,
    monthlyTopUp: String,
    onMonthlyTopUpChange: (String) -> Unit,
    onBack: () -> Unit,
    onCalculate: () -> Unit
) {
    val rates = when {
        months < 6 -> listOf(15.0)
        months < 12 -> listOf(10.0)
        else -> listOf(5.0, 6.0, 7.0)
    }

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Дополнительные параметры",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedRate?.let { "${it}%" } ?: "Выберите процентную ставку")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            rates.forEach { rate ->
                DropdownMenuItem(
                    text = { Text("$rate%") },
                    onClick = {
                        onRateSelected(rate)
                        expanded = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = monthlyTopUp,
            onValueChange = onMonthlyTopUpChange,
            label = { Text("Ежемесячное пополнение (руб, необязательно)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Назад")
            }

            Button(
                onClick = onCalculate,
                modifier = Modifier.weight(1f),
                enabled = selectedRate != null
            ) {
                Text("Рассчитать")
            }
        }
    }
}

@Composable
fun ResultScreen(
    startAmount: Double,
    months: Int,
    rate: Double,
    monthlyTopUp: Double,
    finalAmount: Double,
    earnedInterest: Double,
    onSave: () -> Unit,
    onNew: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Результат расчёта",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ResultRow("Стартовый взнос", formatMoney(startAmount))
                ResultRow("Срок вклада", "$months месяцев")
                ResultRow("Процентная ставка", "${rate}%")
                if (monthlyTopUp > 0) {
                    ResultRow("Ежемесячное пополнение", formatMoney(monthlyTopUp))
                }

                HorizontalDivider()

                ResultRow("Начисленные проценты", formatMoney(earnedInterest))
                ResultRow("Итоговая сумма", formatMoney(finalAmount), isBold = true)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f)
            ) {
                Text("Сохранить")
            }

            Button(
                onClick = onNew,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Новый расчёт")
            }
        }
    }
}

@Composable
fun ResultRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}