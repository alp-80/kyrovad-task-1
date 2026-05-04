package ci.nsu.mobile.main

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CalculatorApp()
            }
        }
    }
}

@Composable
fun CalculatorApp() {
    var screen by remember { mutableStateOf("main") }
    var startAmount by remember { mutableStateOf("") }
    var termMonths by remember { mutableStateOf("") }
    var selectedRate by remember { mutableStateOf<Double?>(null) }
    var monthlyTopUp by remember { mutableStateOf("") }
    var calculations by remember { mutableStateOf<List<CalculationRecord>>(emptyList()) }
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

    when (screen) {
        "main" -> Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("Расчёт вкладов", fontSize = 32.sp)
            Spacer(Modifier.height(40.dp))
            Button({ screen = "stage1" }, Modifier.fillMaxWidth(0.8f).height(56.dp)) {
                Text("Рассчитать")
            }
            Spacer(Modifier.height(12.dp))
            Button({ screen = "history" }, Modifier.fillMaxWidth(0.8f).height(56.dp)) {
                Text("История расчётов (${calculations.size})")
            }
            Spacer(Modifier.height(12.dp))
            Button({
                (context as? Activity)?.finish()
            }, Modifier.fillMaxWidth(0.8f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Закрыть")
            }
        }

        "stage1" -> Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("Основные параметры", fontSize = 24.sp)
            Spacer(Modifier.height(24.dp))
            TextField(startAmount, { startAmount = it }, label = { Text("Стартовый взнос") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            TextField(termMonths, { termMonths = it }, label = { Text("Срок в месяцах") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button({ screen = "main" }, Modifier.weight(1f)) { Text("В начало") }
                Button({
                    if (startAmount.toDoubleOrNull() != null && termMonths.toIntOrNull() != null) screen = "stage2"
                    else Toast.makeText(context, "Заполните поля", Toast.LENGTH_SHORT).show()
                }, Modifier.weight(1f)) { Text("Далее") }
            }
        }

        "stage2" -> {
            val monthsInt = termMonths.toIntOrNull()
            val rates = when {
                monthsInt == null -> emptyList()
                monthsInt < 6 -> listOf(15.0)
                monthsInt < 12 -> listOf(10.0)
                else -> listOf(5.0)
            }
            Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("Дополнительные параметры", fontSize = 24.sp)
                Spacer(Modifier.height(16.dp))

                if (monthsInt == null) {
                    Text("Укажите корректный срок", color = MaterialTheme.colorScheme.error)
                } else {
                    var expanded by remember { mutableStateOf(false) }

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
                        rates.forEach { rateValue ->
                            DropdownMenuItem(
                                text = { Text("$rateValue%") },
                                onClick = {
                                    selectedRate = rateValue
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                TextField(monthlyTopUp, { monthlyTopUp = it }, label = { Text("Пополнение (необяз.)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button({ screen = "stage1" }, Modifier.weight(1f)) { Text("Назад") }
                    Button({ if (monthsInt != null && selectedRate != null) screen = "result" else Toast.makeText(context, "Выберите ставку", Toast.LENGTH_SHORT).show() },
                        Modifier.weight(1f), enabled = monthsInt != null && selectedRate != null) { Text("Рассчитать") }
                }
            }
        }

        "result" -> Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("Результат расчёта", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Стартовый взнос")
                        Text(formatMoney(start))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Срок вклада")
                        Text("$months месяцев")
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Процентная ставка")
                        Text("${rate}%")
                    }
                    if (topUp > 0) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Ежемесячное пополнение")
                        Text(formatMoney(topUp))
                    }
                    Divider()
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Начисленные проценты")
                        Text(formatMoney(totalInterest))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Итоговая сумма", fontWeight = FontWeight.Bold)
                        Text(formatMoney(finalAmount), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    val newRecord = CalculationRecord(
                        date = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date()),
                        startAmount = start,
                        termMonths = months,
                        rate = rate,
                        monthlyTopUp = topUp,
                        finalAmount = finalAmount,
                        earnedInterest = totalInterest
                    )
                    calculations = listOf(newRecord) + calculations
                    Toast.makeText(context, "Сохранено в историю", Toast.LENGTH_SHORT).show()
                }, Modifier.weight(1f)) {
                    Text("Сохранить")
                }
                Button({
                    screen = "main"
                    startAmount = ""
                    termMonths = ""
                    selectedRate = null
                    monthlyTopUp = ""
                }, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                    Text("В начало")
                }
            }
        }

        "history" -> Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("История расчётов", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Button(
                    onClick = { screen = "main" }
                ) {
                    Text("← На главную")
                }
            }

            Spacer(Modifier.height(8.dp))
            Divider()

            if (calculations.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Нет сохранённых расчётов", fontSize = 16.sp)
                }
            } else {
                LazyColumn {
                    items(calculations) { record ->
                        Card(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(record.date, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(4.dp))
                                Text("Стартовый взнос: ${formatMoney(record.startAmount)}", fontSize = 14.sp)
                                Text("Итоговая сумма: ${formatMoney(record.finalAmount)}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class CalculationRecord(
    val date: String,
    val startAmount: Double,
    val termMonths: Int,
    val rate: Double,
    val monthlyTopUp: Double,
    val finalAmount: Double,
    val earnedInterest: Double
)

fun formatMoney(amount: Double) = String.format("%,.2f руб.", amount)