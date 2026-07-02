package com.brunogarcia.footballempireclubmanager.presentation.screens.facilities

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.brunogarcia.footballempireclubmanager.domain.usecase.FacilityType
import com.brunogarcia.footballempireclubmanager.presentation.components.GlassCard
import com.brunogarcia.footballempireclubmanager.presentation.theme.MidnightBlue
import com.brunogarcia.footballempireclubmanager.presentation.theme.DarkNavy
import com.brunogarcia.footballempireclubmanager.presentation.theme.NeonGreen
import com.brunogarcia.footballempireclubmanager.presentation.theme.NeonCyan

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
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "INFRAESTRUTURAS",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar", tint = NeonCyan)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkNavy,
                        titleContentColor = NeonCyan
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(MidnightBlue, DarkNavy)))
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Saldo Atual
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("ORÇAMENTO DISPONÍVEL", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatMoney(state.budget), 
                                fontWeight = FontWeight.Black, 
                                fontSize = 24.sp, 
                                color = NeonGreen
                            )
                        }
                    }

                    // Cartão: Estádio
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Home, contentDescription = "Estádio", modifier = Modifier.size(32.dp), tint = NeonCyan)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Estádio Principal", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                                    Text("Lotação: ${state.stadiumCapacity} lugares", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            val isStadiumAffordable = state.budget >= state.stadiumUpgradeCost
                            Button(
                                onClick = { screenModel.upgradeFacility(FacilityType.STADIUM) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = isStadiumAffordable,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan,
                                    contentColor = MidnightBlue,
                                    disabledContainerColor = NeonCyan.copy(alpha = 0.12f),
                                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    text = "EXPANDIR (+5.000 LUGARES) - ${formatMoney(state.stadiumUpgradeCost)}",
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // Cartão: Centro de Treinos
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Build, contentDescription = "Treino", modifier = Modifier.size(32.dp), tint = NeonCyan)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Centro de Treinos", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                                    Text("Nível: ${state.trainingLevel}/10 (Recuperação física)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            val isTrainingAffordable = !state.isTrainingMaxed && state.budget >= state.trainingUpgradeCost
                            Button(
                                onClick = { screenModel.upgradeFacility(FacilityType.TRAINING_CENTER) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = isTrainingAffordable,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan,
                                    contentColor = MidnightBlue,
                                    disabledContainerColor = NeonCyan.copy(alpha = 0.12f),
                                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    text = if (state.isTrainingMaxed) "NÍVEL MÁXIMO ATINGIDO"
                                    else "MELHORAR CENTRO (+1 NÍVEL) - ${formatMoney(state.trainingUpgradeCost)}",
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // Cartão: Academia de Juniores
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Star, contentDescription = "Academia", modifier = Modifier.size(32.dp), tint = NeonCyan)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Academia de Juniores", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                                    Text("Nível: ${state.youthAcademyLevel}/10 (Gera melhores juniores)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            val isYouthAcademyAffordable = !state.isYouthAcademyMaxed && state.budget >= state.youthAcademyUpgradeCost
                            Button(
                                onClick = { screenModel.upgradeFacility(FacilityType.YOUTH_ACADEMY) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = isYouthAcademyAffordable,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan,
                                    contentColor = MidnightBlue,
                                    disabledContainerColor = NeonCyan.copy(alpha = 0.12f),
                                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    text = if (state.isYouthAcademyMaxed) "NÍVEL MÁXIMO ATINGIDO"
                                    else "MELHORAR ACADEMIA (+1 NÍVEL) - ${formatMoney(state.youthAcademyUpgradeCost)}",
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}