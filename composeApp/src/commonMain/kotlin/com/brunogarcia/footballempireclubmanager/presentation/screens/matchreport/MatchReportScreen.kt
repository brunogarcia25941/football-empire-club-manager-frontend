package com.brunogarcia.footballempireclubmanager.presentation.screens.matchreport

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.brunogarcia.footballempireclubmanager.presentation.components.GlassCard
import com.brunogarcia.footballempireclubmanager.presentation.theme.MidnightBlue
import com.brunogarcia.footballempireclubmanager.presentation.theme.DarkNavy
import com.brunogarcia.footballempireclubmanager.presentation.theme.NeonCyan

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
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            text = if (homeClubId != null) "RELATÓRIO DE JOGO" else "RESULTADOS - JORNADA ${state.currentWeek}", 
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontSize = 18.sp
                        ) 
                    },
                    navigationIcon = {
                        // Se for um jogo individual vindo do calendário, permitimos voltar atrás
                        if (homeClubId != null) {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = NeonCyan)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = DarkNavy,
                        titleContentColor = NeonCyan
                    )
                )
            },
            floatingActionButton = {
                // Botão "Continuar" apenas no relatório da jornada completa
                if (homeClubId == null) {
                    ExtendedFloatingActionButton(
                        onClick = { navigator.pop() },
                        icon = { Icon(Icons.Default.Check, contentDescription = null) },
                        text = { Text("CONTINUAR", fontWeight = FontWeight.Black) },
                        containerColor = NeonCyan,
                        contentColor = MidnightBlue
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(MidnightBlue, DarkNavy)))
                    .padding(paddingValues)
            ) {
                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NeonCyan)
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        
                        if (homeClubId != null && state.singleMatchResult != null) {
                            // MODO INDIVIDUAL: Detalhes de um jogo com marcadores
                            SingleMatchView(state)
                        } else {
                            // MODO JORNADA: Lista de todos os jogos
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
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
    }

    /**
     * Vista detalhada de um único jogo.
     */
    @Composable
    private fun SingleMatchView(state: MatchReportState) {
        val result = state.singleMatchResult ?: return
        Column(modifier = Modifier.padding(16.dp)) {
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(result.homeClubName.uppercase(), fontWeight = FontWeight.Black, fontSize = 16.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = Color.White)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "${result.homeGoals} - ${result.awayGoals}", 
                        fontWeight = FontWeight.Black, 
                        fontSize = 28.sp,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(result.awayClubName.uppercase(), fontWeight = FontWeight.Black, fontSize = 16.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Start, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("EVENTOS DA PARTIDA", fontWeight = FontWeight.Black, color = NeonCyan, modifier = Modifier.padding(vertical = 8.dp), fontSize = 14.sp)

            if (result.events.isEmpty()) {
                Text("Nenhum evento importante a registar.", modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
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
            Text("${event.minute}'", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(36.dp))
            val iconText = when (event.type) {
                MatchEventType.GOAL -> "⚽"
                MatchEventType.YELLOW_CARD -> "🟨"
                MatchEventType.RED_CARD -> "🟥"
                MatchEventType.INJURY -> "🏥"
            }
            Text(iconText, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 8.dp))
            Text(event.playerName, fontWeight = FontWeight.Bold, color = Color.White)
            if (isHomeEvent) Spacer(modifier = Modifier.weight(1f))
        }
    }

    @Composable
    private fun MatchResultCard(item: MatchResultItem) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = com.brunogarcia.footballempireclubmanager.presentation.theme.GlassSurface
            ),
            border = BorderStroke(
                1.dp,
                com.brunogarcia.footballempireclubmanager.presentation.theme.GlassBorder.copy(alpha = 0.15f)
            )
        ) {
            Row(
                modifier = Modifier.padding(14.dp).fillMaxWidth(),
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
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    ClubMiniature(item.homeColor, modifier = Modifier.size(12.dp))
                }

                // Placar
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .width(60.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(NeonCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${item.homeGoals} - ${item.awayGoals}",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = NeonCyan,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Equipa Fora
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    ClubMiniature(item.awayColor, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = item.awayClubName,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White,
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