@Composable
fun DepositHistoryScreen(viewModel: DepositViewModel) {
    val calculations by viewModel.calculations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Мои расчёты", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (calculations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Нет сохранённых расчётов", fontSize = 16.sp)
            }
        } else {
            LazyColumn {
                items(calculations) { calc ->
                    CalculationCard(
                        calculation = calc,
                        onDelete = { viewModel.deleteCalculation(calc) }
                    )
                }
            }
        }
    }
}

@Composable
fun CalculationCard(calculation: DepositCalculation, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(calculation.formattedDate, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Стартовый взнос: ${formatMoney(calculation.startAmount)}", fontSize = 14.sp)
            Text("Срок: ${calculation.termMonths} месяцев", fontSize = 14.sp)
            Text("Ставка: ${calculation.interestRate}%", fontSize = 14.sp)
            if (calculation.monthlyTopUp > 0) {
                Text("Пополнение: ${formatMoney(calculation.monthlyTopUp)}/мес", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Divider()
            Text("Итоговая сумма: ${formatMoney(calculation.finalAmount)}",
                fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Начислено процентов: ${formatMoney(calculation.earnedInterest)}",
                fontSize = 14.sp, color = Color.Green)
        }
    }
}

fun formatMoney(amount: Double) = String.format("%,.2f руб.", amount)