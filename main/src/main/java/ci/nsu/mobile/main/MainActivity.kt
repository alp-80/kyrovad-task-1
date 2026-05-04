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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

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
    var currentScreen by remember { mutableStateOf(Screen.MAIN) }

    var startAmount by remember { mutableStateOf("") }
    var termMonths by remember { mutableStateOf("") }

    when (currentScreen) {
        Screen.MAIN -> MainScreen(
            onCalculateClick = { currentScreen = Screen.STAGE_1 },
            onHistoryClick = { Toast.makeText(context, "История расчётов", Toast.LENGTH_SHORT).show() },
            onCloseClick = { finishAffinity() }
        )
        Screen.STAGE_1 -> Stage1Screen(
            startAmount = startAmount,
            onStartAmountChange = { startAmount = it },
            termMonths = termMonths,
            onTermMonthsChange = { termMonths = it },
            onBackToMain = { currentScreen = Screen.MAIN },
            onNext = {
                if (validateStage1(startAmount, termMonths)) {
                    currentScreen = Screen.STAGE_2
                } else {
                    Toast.makeText(context, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show()
                }
            }
        )
        Screen.STAGE_2 -> { }
    }
}

@Composable
fun getContext() = androidx.compose.ui.platform.LocalContext.current

@Composable
fun Stage1Screen(
    startAmount: String,
    onStartAmountChange: (String) -> Unit,
    termMonths: String,
    onTermMonthsChange: (String) -> Unit,
    onBackToMain: () -> Unit,
    onNext: () -> Unit
) {
    val context = getContext()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Этап 1: Основные параметры",
                fontSize = 24.sp,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = startAmount,
                onValueChange = onStartAmountChange,
                label = { Text("Стартовый взнос") },
                placeholder = { Text("Введите сумму") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = startAmount.isNotBlank() && startAmount.toDoubleOrNull() == null
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = termMonths,
                onValueChange = onTermMonthsChange,
                label = { Text("Срок вклада в месяцах") },
                placeholder = { Text("Введите количество месяцев") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = termMonths.isNotBlank() && termMonths.toIntOrNull() == null
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onBackToMain,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("В начало")
                }

                Button(
                    onClick = onNext,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Text("Далее")
                }
            }
        }
    }
}


fun validateStage1(startAmount: String, termMonths: String): Boolean {
    if (startAmount.isBlank() || termMonths.isBlank()) return false
    val amount = startAmount.toDoubleOrNull()
    val months = termMonths.toIntOrNull()
    return amount != null && amount > 0 && months != null && months > 0
}


@Composable
fun finishAffinity() {
    val context = getContext()
    (context as? android.app.Activity)?.finishAffinity()
}


enum class Screen {
    MAIN, STAGE_1, STAGE_2
}

@Composable
fun MainScreen(
    onCalculateClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Расчёт вкладов",
                fontSize = 32.sp,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(80.dp))

            Button(
                onClick = onCalculateClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(56.dp)
            ) {
                Text(text = "Рассчитать", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onHistoryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(56.dp)
            ) {
                Text(text = "История расчётов", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onCloseClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(text = "Закрыть приложение", fontSize = 18.sp)
            }
        }
    }
}