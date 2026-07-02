package com.brunogarcia.footballempireclubmanager.presentation.screens.matchreport

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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

/**
 * Ecrã que mostra o relatório de um jogo individual ou de uma jornada completa.
 * @param homeClubId Se fornecido, mostra apenas este jogo. Se for null, mostra a jornada toda.
 * @param awayClubId Se fornecido, mostra apenas este jogo.
 */
class MatchReportScreen(
    private val homeClubId: String? = null,
    private val awayClubId: String? = null,
    private val isCup: Boolean = false
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<MatchReportScreenModel>()
        val state by screenModel.state.collectAsState()

        // Carrega os dados assim que o ecrã abre
        LaunchedEffect(Unit) {
            if (homeClubId != null && awayClubId != null) {
                // Se passámos IDs, é um relatório de jogo individual (detalhado)
                screenModel.loadSingleMatch(homeClubId, awayClubId, isCup)
            } else {
                // Se não passámos nada, é o resumo da jornada toda
                screenModel.loadMatchweekResults()
            }
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            if (homeClubId != null) "Relatório de Jogo" else "Resultados: Semana ${state.currentWeek}", 
                            fontWeight = FontWeight.Bold 
                        ) 
                    },
                    navigationIcon = {
                        // Se for um jogo individual vindo do calendário, permitimos voltar atrás
                        if (homeClubId != null) {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            },
            floatingActionButton = {
                // Botão "Continuar" apenas no relatório da jornada completa
                if (homeClubId == null) {
                    ExtendedFloatingActionButton(
                        onClick = { navigator.pop() },
                        icon = { Icon(Icons.Default.Check, contentDescription = null) },
                        text = { Text("Continuar") },
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        ) { paddingValues ->
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    
                    if (homeClubId != null && state.singleMatchResult != null) {
                        // MODO INDIVIDUAL: Detalhes de um jogo com marcadores
                        SingleMatchView(state)
                    } else {
                        // MODO JORNADA: Lista de todos os jogos
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.results) { result ->
                                MatchResultCard(result)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Vista detalhada de um único jogo (o que já tinhas antes).
     */
    @Composable
    private fun SingleMatchView(state: MatchReportState) {
        val result = state.singleMatchResult ?: return
        Column {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(result.homeClubName, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("${result.homeGoals} - ${result.awayGoals}", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(result.awayClubName, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
                }
            }

            Text("Eventos da Partida", fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))

            if (result.events.isEmpty()) {
                Text("Nenhum evento de registo.", modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(result.events) { event ->
                        val isHomeEvent = event.clubId == homeClubId
                        EventRow(event, isHomeEvent)
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

    /**
     * Desenha uma linha com o resultado de um jogo.
     * Ajustado para permitir nomes mais longos.
     */
    @Composable
    private fun MatchResultCard(item: MatchResultItem) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Equipa Casa
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = item.homeClubName,
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ClubMiniature(item.homeColor, modifier = Modifier.size(10.dp))
                }

                // Placar (Fixado no centro)
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp).width(55.dp)
                ) {
                    Text(
                        text = "${item.homeGoals} - ${item.awayGoals}",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Equipa Fora
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    ClubMiniature(item.awayColor, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.awayClubName,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    @Composable
    private fun ClubMiniature(hexColor: String, modifier: Modifier = Modifier) {
        val color = try { Color(parseColor(hexColor)) } catch (e: Exception) { Color.Gray }
        Box(modifier = modifier.background(color, CircleShape))
    }

    private fun parseColor(hex: String): Int {
        return if (hex.startsWith("#")) {
            val colorStr = if (hex.length == 7) "FF${hex.substring(1)}" else hex.substring(1)
            colorStr.toLong(16).toInt()
        } else 0xFFCCCCCC.toInt()
    }
}