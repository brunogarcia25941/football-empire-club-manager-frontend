package com.brunogarcia.footballempireclubmanager.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.brunogarcia.footballempireclubmanager.presentation.screens.facilities.FacilitiesScreen
import com.brunogarcia.footballempireclubmanager.presentation.screens.market.TransferMarketScreen
import com.brunogarcia.footballempireclubmanager.presentation.screens.matchreport.MatchReportScreen
import com.brunogarcia.footballempireclubmanager.presentation.screens.squad.SquadScreen
import com.brunogarcia.footballempireclubmanager.presentation.screens.newseason.NewSeasonScreen
import com.brunogarcia.footballempireclubmanager.presentation.components.GlassCard
import com.brunogarcia.footballempireclubmanager.presentation.theme.MidnightBlue
import com.brunogarcia.footballempireclubmanager.presentation.theme.DarkNavy
import com.brunogarcia.footballempireclubmanager.presentation.theme.NeonGreen
import com.brunogarcia.footballempireclubmanager.presentation.theme.NeonCyan

class DashboardScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<DashboardScreenModel>()

        val state by screenModel.state.collectAsState()

        LaunchedEffect(Unit) { screenModel.loadDashboardData() }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(state.clubName.uppercase(), fontWeight = FontWeight.Black, letterSpacing = 1.5.sp) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkNavy,
                        titleContentColor = NeonCyan
                    )
                )
            },
            floatingActionButton = {
                val isEndOfSeason = state.nextMatchText == "Fim da Época"

                ExtendedFloatingActionButton(
                    onClick = {
                        if (!isEndOfSeason) {
                            screenModel.onAdvanceWeekClicked {
                                navigator.push(MatchReportScreen())
                            }
                        } else {
                            navigator.push(NewSeasonScreen())
                        }
                    },
                    icon = { Icon(Icons.Filled.PlayArrow, contentDescription = "Avançar") },
                    text = { Text(if (isEndOfSeason) "VER FIM DE ÉPOCA" else "AVANÇAR JORNADA", fontWeight = FontWeight.Bold) },
                    containerColor = if (isEndOfSeason) NeonGreen else NeonCyan,
                    contentColor = MidnightBlue
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
                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.DateRange, 
                                contentDescription = null, 
                                modifier = Modifier.size(32.dp),
                                tint = NeonCyan
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("CALENDÁRIO DA LIGA", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Semana ${state.currentWeek}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = NeonCyan)
                            }
                        }
                    }

                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("SALDO DO CLUBE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatBudget(state.budget),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = NeonGreen
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { navigator.push(FacilitiesScreen()) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
                    ) {
                        Text("GERIR INFRAESTRUTURAS", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { navigator.push(com.brunogarcia.footballempireclubmanager.presentation.screens.youthacademy.YouthAcademyScreen()) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
                    ) {
                        Text("ACADEMIA DE JUNIORES", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { navigator.push(TransferMarketScreen()) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = MidnightBlue
                        )
                    ) {
                        Text("MERCADO DE TRANSFERÊNCIAS", fontWeight = FontWeight.Black)
                    }

                    if (state.lastMatchResult != null) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("ÚLTIMO RESULTADO", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.lastMatchResult!!,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "A época vai começar. Prepara a equipa!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("PRÓXIMO JOGO", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(state.nextMatchText, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NeonCyan)
                            Text(state.nextMatchLoc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

fun formatBudget(budget: Double): String {
    val numberString = budget.toLong().toString()
    val reversed = numberString.reversed()
    val chunked = reversed.chunked(3)
    return chunked.joinToString(".").reversed() + " €"
}