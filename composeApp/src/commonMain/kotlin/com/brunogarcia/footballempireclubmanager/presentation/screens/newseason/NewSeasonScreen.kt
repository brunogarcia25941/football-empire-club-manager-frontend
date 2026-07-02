package com.brunogarcia.footballempireclubmanager.presentation.screens.newseason

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.brunogarcia.footballempireclubmanager.presentation.screens.MainGameScreen
import com.brunogarcia.footballempireclubmanager.presentation.screens.facilities.formatMoney
import com.brunogarcia.footballempireclubmanager.presentation.components.GlassCard
import com.brunogarcia.footballempireclubmanager.presentation.theme.MidnightBlue
import com.brunogarcia.footballempireclubmanager.presentation.theme.DarkNavy
import com.brunogarcia.footballempireclubmanager.presentation.theme.NeonGreen
import com.brunogarcia.footballempireclubmanager.presentation.theme.NeonCyan
import com.brunogarcia.footballempireclubmanager.presentation.theme.AlertRed

class NewSeasonScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<NewSeasonScreenModel>()
        val state by screenModel.state.collectAsState()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            text = "RESUMO DA ÉPOCA", 
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontSize = 18.sp
                        ) 
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Cartão Informativo de Fim de Época com os Prémios
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.clubName.uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "POSIÇÃO FINAL: ${state.finalPosition}º LUGAR",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "PRÉMIO MONETÁRIO RECEBIDO:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatMoney(state.prizeMoney),
                                fontWeight = FontWeight.Black,
                                fontSize = 26.sp,
                                color = NeonGreen
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Novo Orçamento: ${formatMoney(state.newBudget)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }

                    Text(
                        text = "CLASSIFICAÇÃO FINAL DA LIGA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                        modifier = Modifier.align(Alignment.Start),
                        letterSpacing = 0.5.sp
                    )

                    // Tabela de Classificação Final em Vidro Fosco
                    GlassCard(
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                                Text("#", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Black, color = NeonCyan, fontSize = 13.sp)
                                Text("CLUBE", modifier = Modifier.weight(1f), fontWeight = FontWeight.Black, color = NeonCyan, fontSize = 13.sp)
                                Text("J", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Black, color = NeonCyan, fontSize = 13.sp)
                                Text("DG", modifier = Modifier.width(40.dp), fontWeight = FontWeight.Black, color = NeonCyan, fontSize = 13.sp)
                                Text("PTS", modifier = Modifier.width(40.dp), fontWeight = FontWeight.Black, color = NeonCyan, fontSize = 13.sp)
                            }

                            HorizontalDivider(color = com.brunogarcia.footballempireclubmanager.presentation.theme.GlassBorder.copy(alpha = 0.2f))

                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                itemsIndexed(state.finalTable) { index, entry ->
                                    val isUser = entry.clubName == state.clubName
                                    val weight = if (isUser) FontWeight.Black else FontWeight.Medium
                                    val rowColor = if (isUser) NeonCyan else Color.White

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(if (isUser) NeonCyan.copy(alpha = 0.12f) else Color.Transparent)
                                            .padding(vertical = 8.dp, horizontal = 4.dp)
                                    ) {
                                        Text("${index + 1}", modifier = Modifier.width(30.dp), fontSize = 13.sp, fontWeight = weight, color = rowColor)
                                        Text(entry.clubName, modifier = Modifier.weight(1f), fontSize = 13.sp, fontWeight = weight, color = rowColor)
                                        Text("${entry.played}", modifier = Modifier.width(30.dp), fontSize = 13.sp, fontWeight = weight, color = rowColor)
                                        Text("${entry.goalDifference}", modifier = Modifier.width(40.dp), fontSize = 13.sp, fontWeight = weight, color = rowColor)
                                        Text("${entry.points}", modifier = Modifier.width(40.dp), fontSize = 13.sp, fontWeight = FontWeight.Black, color = if (isUser) NeonCyan else NeonGreen)
                                    }
                                    HorizontalDivider(color = com.brunogarcia.footballempireclubmanager.presentation.theme.GlassBorder.copy(alpha = 0.1f))
                                }
                            }
                        }
                    }

                    // Botão para avançar de época
                    Button(
                        onClick = {
                            screenModel.startNewSeason {
                                navigator.replaceAll(MainGameScreen())
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = MidnightBlue
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("COMEÇAR NOVA ÉPOCA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
