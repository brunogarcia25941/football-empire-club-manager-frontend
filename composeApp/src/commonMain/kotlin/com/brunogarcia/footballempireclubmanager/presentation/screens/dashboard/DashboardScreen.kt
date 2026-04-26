package com.brunogarcia.footballempireclubmanager.presentation.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.brunogarcia.footballempireclubmanager.presentation.screens.squad.SquadScreen

class DashboardScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        // Usamos o Koin para injetar o ScreenModel magicamente!
        val screenModel = getScreenModel<DashboardScreenModel>()

        // Observa o estado (se o budget mudar, a UI atualiza sozinha)
        val state by screenModel.state.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(state.clubName, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            floatingActionButton = {
                val isEndOfSeason = state.nextMatchText == "Fim da Época"

                ExtendedFloatingActionButton(
                    onClick = {
                        if (!isEndOfSeason) screenModel.onAdvanceWeekClicked()
                    },
                    icon = { Icon(Icons.Filled.PlayArrow, contentDescription = "Avançar") },
                    text = { Text(if (isEndOfSeason) "Época Terminada" else "Avançar Semana") },
                    // Fica cinzento se a época tiver acabado
                    containerColor = if (isEndOfSeason) Color.Gray else MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cartão da Época / Semana
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.DateRange, contentDescription = null, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Calendário", style = MaterialTheme.typography.labelMedium)
                            Text("Semana ${state.currentWeek}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Cartão Financeiro
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Orçamento do Clube", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        // Formatar o dinheiro (ex: 45000000 -> 45.000.000 €)
                        Text(
                            text = formatBudget(state.budget),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Cartão do Último Resultado
                if (state.lastMatchResult != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Último Resultado", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.lastMatchResult!!,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                } else {
                    Text(
                        text = "A época vai começar. Prepara a equipa!",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }
                //fim cartao ultimo Resultado

                // Cartão do Próximo Jogo
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Próximo Jogo", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.nextMatchText, fontWeight = FontWeight.Medium, fontSize = 18.sp)
                        Text(state.nextMatchLoc, style = MaterialTheme.typography.bodySmall)
                    }
                }
                //fim cartao proximo jogo


            }
        }
    }
}

// Função pura em Kotlin para formatar "45000000.0" em "45.000.000 €"
fun formatBudget(budget: Double): String {
    val numberString = budget.toLong().toString()
    val reversed = numberString.reversed()
    val chunked = reversed.chunked(3)
    return chunked.joinToString(".").reversed() + " €"
}