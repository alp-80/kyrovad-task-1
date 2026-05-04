package ci.nsu.mobile.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.platform.LocalContext

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
    val context = LocalContext.current

    val start = startAmount.toDoubleOrNull() ?: 0.0
    val months = termMonths.toIntOrNull() ?: 0
    val rate = selectedRate ?: 0.0
    val topUp = monthlyTopUp.toDoubleOrNull() ?: 0.0

    // Расчёт результатов
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
        "main" -> Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Расчёт вкладов", fontSize = 32.sp)
            Spacer(modifier = Modifier.height(40.dp))
            Button({ screen = "stage1" }, Modifier.fillMaxWidth(0.8f).height(56.dp)) { Text("Рассчитать") }
            Spacer(modifier = Modifier.height(12.dp))
            Button({ Toast.makeText(context, "История расчётов", Toast.LENGTH_SHORT).show() }, Modifier.fillMaxWidth(0.8f).height(56.dp)) { Text("История расчётов") }
            Spacer(modifier = Modifier.height(12.dp))
            Button({ (context as? android.app.Activity)?.finishAffinity() }, Modifier.fillMaxWidth(0.8f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Закрыть") }
        }

        "stage1" -> Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Основные параметры", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(startAmount, { startAmount = it }, label = { Text("Стартовый взнос") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(termMonths, { termMonths = it }, label = { Text("Срок в месяцах") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Spacer(modifier = Modifier.height(24.dp))
            Row {
                Button({ screen = "main" }, Modifier.weight(1f)) { Text("В начало") }
                Spacer(modifier = Modifier.width(12.dp))
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
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Дополнительные параметры", fontSize = 24.sp)
                Spacer(modifier = Modifier.height(16.dp))
                if (monthsInt == null) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text("Укажите корректный срок", Modifier.padding(12.dp))
                    }
                } else {
                    var expanded by remember { mutableStateOf(false) }
                    OutlinedTextField(selectedRate?.let { "${it}%" } ?: "Не выбрано", {}, readOnly = true, label = { Text("Ставка") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.fillMaxWidth())
                    DropdownMenu(expanded, { expanded = false }) {
                        rates.forEach { rate -> DropdownMenuItem({ Text("$rate%") }, { selectedRate = rate; expanded = false }) }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(monthlyTopUp, { monthlyTopUp = it }, label = { Text("Пополнение (необяз.)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(modifier = Modifier.height(24.dp))
                Row {
                    Button({ screen = "stage1" }, Modifier.weight(1f)) { Text("Назад") }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button({ if (monthsInt != null && selectedRate != null) screen = "result" else Toast.makeText(context, "Выберите ставку", Toast.LENGTH_SHORT).show() },
                        Modifier.weight(1f), enabled = monthsInt != null && selectedRate != null) { Text("Рассчитать") }
                }
            }
        }

        "result" -> Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Результат расчёта", fontSize = 28.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            Card(Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Стартовый взнос"); Text(formatMoney(start))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Срок вклада"); Text("$months месяцев")
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Процентная ставка"); Text("${rate}%")
                    }
                    if (topUp > 0) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Ежемесячное пополнение"); Text(formatMoney(topUp))
                    }
                    Divider()
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Начисленные проценты", color = MaterialTheme.colorScheme.primary); Text(formatMoney(totalInterest), color = MaterialTheme.colorScheme.primary)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Итоговая сумма", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold); Text(formatMoney(finalAmount), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button({ Toast.makeText(context, "Сохранено в историю", Toast.LENGTH_SHORT).show() }, Modifier.weight(1f)) { Text("Сохранить") }
                Button({
                    screen = "main"
                    startAmount = ""; termMonths = ""; selectedRate = null; monthlyTopUp = ""
                }, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Text("В начало") }
            }
        }
    }
}

fun formatMoney(amount: Double) = String.format("%,.2f руб.", amount)