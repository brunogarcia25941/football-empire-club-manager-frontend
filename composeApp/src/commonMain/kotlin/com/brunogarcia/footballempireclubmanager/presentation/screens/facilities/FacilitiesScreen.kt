package com.brunogarcia.footballempireclubmanager.presentation.screens.facilities

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.brunogarcia.footballempireclubmanager.domain.usecase.FacilityType


fun formatMoney(value: Double): String {
    val numberString = value.toLong().toString()
    return numberString.reversed().chunked(3).joinToString(".").reversed() + " €"
}

class FacilitiesScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<FacilitiesScreenModel>()
        val state by screenModel.state.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Infraestruturas") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                )
            }
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {

                // Saldo Atual
                Text("Orçamento Disponível", style = MaterialTheme.typography.labelLarge)
                Text(formatMoney(state.budget), fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(24.dp))

                // Cartão: Estádio
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Home, contentDescription = "Estádio", modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Estádio Principal", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("Lotação: ${state.stadiumCapacity} lugares", color = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { screenModel.upgradeFacility(FacilityType.STADIUM) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state.budget >= state.stadiumUpgradeCost
                        ) {
                            Text("Expandir (+5.000 lugares) - ${formatMoney(state.stadiumUpgradeCost)}")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cartão: Centro de Treinos
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Build, contentDescription = "Treino", modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Centro de Treinos", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("Nível: ${state.trainingLevel}/10 (Acelera recuperação física)", color = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { screenModel.upgradeFacility(FacilityType.TRAINING_CENTER) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isTrainingMaxed && state.budget >= state.trainingUpgradeCost
                        ) {
                            Text(
                                if (state.isTrainingMaxed) "Nível Máximo Atingido"
                                else "Melhorar Centro (+1 Nível) - ${formatMoney(state.trainingUpgradeCost)}"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cartão: Academia de Juniores
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = "Academia", modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Academia de Juniores", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("Nível: ${state.youthAcademyLevel}/10 (Gera melhores juniores)", color = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { screenModel.upgradeFacility(FacilityType.YOUTH_ACADEMY) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isYouthAcademyMaxed && state.budget >= state.youthAcademyUpgradeCost
                        ) {
                            Text(
                                if (state.isYouthAcademyMaxed) "Nível Máximo Atingido"
                                else "Melhorar Academia (+1 Nível) - ${formatMoney(state.youthAcademyUpgradeCost)}"
                            )
                        }
                    }
                }
            }
        }
    }
}