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

    when (screen) {
        "main" -> MainScreen(
            onCalculate = { screen = "stage1" },
            onHistory = { Toast.makeText(context, "История расчётов", Toast.LENGTH_SHORT).show() },
            onClose = { (context as? android.app.Activity)?.finishAffinity() }
        )

        "stage1" -> Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Основные параметры", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = startAmount,
                onValueChange = { startAmount = it },
                label = { Text("Стартовый взнос") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = termMonths,
                onValueChange = { termMonths = it },
                label = { Text("Срок в месяцах") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row {
                Button({ screen = "main" }, modifier = Modifier.weight(1f)) { Text("В начало") }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = {
                        if (startAmount.toDoubleOrNull() != null && termMonths.toIntOrNull() != null) {
                            screen = "stage2"
                        } else {
                            Toast.makeText(context, "Заполните поля корректно", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Далее") }
            }
        }

        "stage2" -> {
            val months = termMonths.toIntOrNull()
            val rates = when {
                months == null -> emptyList()
                months < 6 -> listOf(15.0)
                months < 12 -> listOf(10.0)
                else -> listOf(5.0)
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Дополнительные параметры", fontSize = 24.sp)
                Spacer(modifier = Modifier.height(16.dp))

                if (months == null) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text("Ошибка: укажите корректный срок", modifier = Modifier.padding(12.dp))
                    }
                } else {
                    var expanded by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = selectedRate?.let { "${it}%" } ?: "Не выбрано",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Процентная ставка") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(expanded, { expanded = false }) {
                        rates.forEach { rate ->
                            DropdownMenuItem({ Text("$rate%") }, { selectedRate = rate; expanded = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = monthlyTopUp,
                    onValueChange = { monthlyTopUp = it },
                    label = { Text("Ежемесячное пополнение") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row {
                    Button({ screen = "stage1" }, modifier = Modifier.weight(1f)) { Text("Назад") }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (months != null && selectedRate != null) {
                                Toast.makeText(context, "Результат: ${startAmount.toDouble()} руб., ${selectedRate}%, срок $months мес.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Выберите ставку", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = months != null && selectedRate != null
                    ) { Text("Рассчитать") }
                }
            }
        }
    }
}

@Composable
fun MainScreen(onCalculate: () -> Unit, onHistory: () -> Unit, onClose: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Расчёт вкладов", fontSize = 32.sp)
        Spacer(modifier = Modifier.height(40.dp))
        Button(onCalculate, Modifier.fillMaxWidth(0.8f).height(56.dp)) { Text("Рассчитать") }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onHistory, Modifier.fillMaxWidth(0.8f).height(56.dp)) { Text("История расчётов") }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClose, Modifier.fillMaxWidth(0.8f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
            Text("Закрыть")
        }
    }
}