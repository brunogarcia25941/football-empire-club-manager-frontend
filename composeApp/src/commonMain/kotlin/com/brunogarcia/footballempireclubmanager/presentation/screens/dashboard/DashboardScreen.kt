package com.brunogarcia.footballempireclubmanager.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // SEÇÃO 1: Semana e Orçamento (Lado a Lado)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card Calendário / Semana
                        GlassCard(
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text("CALENDÁRIO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.DateRange, 
                                        contentDescription = null, 
                                        modifier = Modifier.size(18.dp),
                                        tint = NeonCyan
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Semana ${state.currentWeek}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NeonCyan)
                                }
                            }
                        }

                        // Card Saldo / Orçamento
                        GlassCard(
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text("SALDO DO CLUBE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatBudget(state.budget),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = NeonGreen,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // SEÇÃO 2: Botões de Ação (Grelha 2x2 para poupar espaço vertical)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { navigator.push(com.brunogarcia.footballempireclubmanager.presentation.screens.inbox.InboxScreen()) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.newOffersCount > 0) NeonGreen else com.brunogarcia.footballempireclubmanager.presentation.theme.GlassSurface,
                                contentColor = if (state.newOffersCount > 0) MidnightBlue else Color.White
                            ),
                            border = if (state.newOffersCount > 0) null else androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text(
                                text = if (state.newOffersCount > 0) "CORREIO (${state.newOffersCount})" else "CORREIO (0)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = { navigator.push(TransferMarketScreen()) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = MidnightBlue
                            ),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text("MERCADO", fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { navigator.push(FacilitiesScreen()) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text("INFRAESTRUTURAS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { navigator.push(com.brunogarcia.footballempireclubmanager.presentation.screens.youthacademy.YouthAcademyScreen()) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text("ACADEMIA", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // SEÇÃO 3: Estado de Resultados e Próximo Jogo
                    if (state.lastMatchResult != null) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("ÚLTIMO RESULTADO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = state.lastMatchResult!!,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "A época vai começar. Prepara a equipa!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp).align(Alignment.CenterHorizontally)
                        )
                    }

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("PRÓXIMO JOGO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(state.nextMatchText, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NeonCyan)
                            Spacer(modifier = Modifier.height(2.dp))
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