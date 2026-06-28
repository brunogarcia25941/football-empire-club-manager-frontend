package com.brunogarcia.footballempireclubmanager.presentation.screens.newseason

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.brunogarcia.footballempireclubmanager.presentation.screens.MainGameScreen
import com.brunogarcia.footballempireclubmanager.presentation.screens.facilities.formatMoney

/**
 * Ecrã de transição que mostra a tabela final de classificação,
 * o prémio monetário obtido pelo clube do jogador e um botão para avançar para o ano seguinte.
 */
class NewSeasonScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<NewSeasonScreenModel>()
        val state by screenModel.state.collectAsState()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Resumo da Época", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cartão Informativo de Fim de Época com os Prémios
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.clubName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Posição Final: ${state.finalPosition}º Lugar",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Prémio Concedido:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = formatMoney(state.prizeMoney),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Novo Orçamento: ${formatMoney(state.newBudget)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Text(
                    text = "Classificação Final da Liga",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                // Tabela de Classificação Final
                Card(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                            Text("#", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Bold)
                            Text("Clube", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Text("J", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Bold)
                            Text("DG", modifier = Modifier.width(40.dp), fontWeight = FontWeight.Bold)
                            Text("Pts", modifier = Modifier.width(40.dp), fontWeight = FontWeight.Bold)
                        }

                        HorizontalDivider()

                        LazyColumn {
                            itemsIndexed(state.finalTable) { index, entry ->
                                val isUser = entry.clubName == state.clubName
                                val weight = if (isUser) FontWeight.Bold else FontWeight.Normal
                                val color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                    Text("${index + 1}", modifier = Modifier.width(30.dp), fontSize = 13.sp, fontWeight = weight, color = color)
                                    Text(entry.clubName, modifier = Modifier.weight(1f), fontSize = 13.sp, fontWeight = weight, color = color)
                                    Text("${entry.played}", modifier = Modifier.width(30.dp), fontSize = 13.sp, fontWeight = weight, color = color)
                                    Text("${entry.goalDifference}", modifier = Modifier.width(40.dp), fontSize = 13.sp, fontWeight = weight, color = color)
                                    Text("${entry.points}", modifier = Modifier.width(40.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
                                }
                                HorizontalDivider(thickness = 0.5.dp)
                            }
                        }
                    }
                }

                // Botão para avançar de época
                Button(
                    onClick = {
                        screenModel.startNewSeason {
                            // Substitui todo o backstack pelo ecrã principal reiniciado na semana 1
                            navigator.replaceAll(MainGameScreen())
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Começar Nova Época", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
