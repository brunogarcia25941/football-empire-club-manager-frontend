package com.brunogarcia.footballempireclubmanager.presentation.screens.matchreport

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.brunogarcia.footballempireclubmanager.domain.engine.MatchEvent
import com.brunogarcia.footballempireclubmanager.domain.engine.MatchEventType

class MatchReportScreen(
    private val homeClubId: String,
    private val awayClubId: String
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        // Como o Voyager está a correr na raiz (App.kt), este ecrã vai deslizar por cima do Menu Principal e tapar o Rodapé
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<MatchReportScreenModel>()
        val state by screenModel.state.collectAsState()

        LaunchedEffect(Unit) {
            screenModel.loadMatch(homeClubId, awayClubId)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Relatório de Jogo") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { paddingValues ->
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    // Placar Gigante
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(state.homeClubName, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("${state.homeGoals} - ${state.awayGoals}", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(state.awayClubName, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
                        }
                    }

                    HorizontalDivider()

                    Text("Eventos da Partida", fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))

                    if (state.events.isEmpty()) {
                        Text("Nenhum evento de registo.", modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray)
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.events) { event ->
                                val isHomeEvent = event.clubId == homeClubId
                                EventRow(event, isHomeEvent)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun EventRow(event: MatchEvent, isHomeEvent: Boolean) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isHomeEvent) Arrangement.Start else Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isHomeEvent) Spacer(modifier = Modifier.weight(1f))

            Text("${event.minute}'", fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.width(32.dp))

            // Emoji conforme o tipo de evento
            val iconText = when (event.type) {
                MatchEventType.GOAL -> "⚽"
                MatchEventType.YELLOW_CARD -> "🟨"
                MatchEventType.RED_CARD -> "🟥"
                MatchEventType.INJURY -> "🏥"
            }

            Text(iconText, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 8.dp))
            Text(event.playerName, fontWeight = FontWeight.Medium)

            if (isHomeEvent) Spacer(modifier = Modifier.weight(1f))
        }
    }
}