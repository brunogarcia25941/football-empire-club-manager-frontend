package com.brunogarcia.footballempireclubmanager.presentation.screens.tactics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.brunogarcia.footballempireclubmanager.domain.model.Player
import com.brunogarcia.footballempireclubmanager.domain.model.Position
import com.brunogarcia.footballempireclubmanager.presentation.theme.AlertRed
import com.brunogarcia.footballempireclubmanager.presentation.theme.DarkNavy
import com.brunogarcia.footballempireclubmanager.presentation.theme.MidnightBlue
import com.brunogarcia.footballempireclubmanager.presentation.theme.NeonCyan
import com.brunogarcia.footballempireclubmanager.presentation.theme.NeonGreen

class TacticsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<TacticsScreenModel>()
        val state by screenModel.state.collectAsState()

        // Garante que o 11 inicial e o plantel do ecrã tático são atualizados sempre que o ecrã fica visível
        LaunchedEffect(Unit) {
            screenModel.loadTactics()
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (state.selectedSlotId == null) "TÁTICAS (4-3-3)" else "ESCOLHER JOGADOR",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        // Se estiver na lista de seleção, mostra um botão para cancelar
                        if (state.selectedSlotId != null) {
                            IconButton(onClick = { screenModel.closePlayerSelection() }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Cancelar", tint = AlertRed)
                            }
                        }
                    },
                    actions = {
                        // Botão para auto-escalar o 11 inicial no menu principal das táticas
                        if (state.selectedSlotId == null) {
                            TextButton(onClick = { screenModel.autoPickStarting11() }) {
                                Text(
                                    text = "AUTO-ESCALAR",
                                    fontWeight = FontWeight.Black,
                                    color = NeonCyan
                                )
                            }
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
                if (state.selectedSlotId == null) {
                    // MODO 1: Ver a Tática
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.slots) { slot ->
                            TacticSlotItem(slot = slot, onClick = { screenModel.openPlayerSelection(slot.id) })
                        }
                    }
                } else {
                    // MODO 2: Lista do Plantel para Selecionar
                    val targetRole = state.slots.find { it.id == state.selectedSlotId }?.role ?: Position.GK
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.squad) { player ->
                            PlayerSelectionItem(
                                player = player, 
                                targetRole = targetRole, 
                                onClick = { screenModel.assignPlayerToSlot(player) }
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TacticSlotItem(slot: TacticSlot, onClick: () -> Unit) {
        val hasPlayer = slot.player != null

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = com.brunogarcia.footballempireclubmanager.presentation.theme.GlassSurface
            ),
            border = BorderStroke(
                1.dp,
                if (hasPlayer) com.brunogarcia.footballempireclubmanager.presentation.theme.GlassBorder else AlertRed.copy(alpha = 0.6f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Posição no campo (ex: GK, CB, ST)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (hasPlayer) NeonCyan else AlertRed.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = slot.role.name,
                        fontWeight = FontWeight.Black,
                        color = if (hasPlayer) MidnightBlue else AlertRed,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                if (slot.player != null) {
                    val player = slot.player
                    // Jogador escalado
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = player.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        // Barra de Stamina/Condição com cor dinâmica
                        val staminaColor = when {
                            player.stamina >= 80 -> NeonGreen
                            player.stamina >= 50 -> Color(0xFFFF9800)
                            else -> AlertRed
                        }
                        Text(
                            text = "Condição: ${player.stamina}%",
                            fontSize = 12.sp,
                            color = staminaColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Mostra o Overall EFETIVO
                    val effectiveOverall = player.getEffectiveOverall(slot.role)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = effectiveOverall.toString(),
                            fontWeight = FontWeight.Black,
                            color = NeonCyan,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    // Slot Vazio
                    Text(
                        text = "POSIÇÃO VAZIA",
                        modifier = Modifier.weight(1f),
                        color = AlertRed.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 13.sp
                    )
                    Icon(
                        imageVector = Icons.Filled.Add, 
                        contentDescription = "Adicionar", 
                        tint = AlertRed.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }

    @Composable
    private fun PlayerSelectionItem(player: Player, targetRole: Position, onClick: () -> Unit) {
        val overall = player.getEffectiveOverall(targetRole)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
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
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Posição de Origem
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = player.mainPosition.name,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = player.name,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Idade: ${player.age} anos | Stamina: ${player.stamina}%",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // OVR Efetivo
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = overall.toString(),
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}