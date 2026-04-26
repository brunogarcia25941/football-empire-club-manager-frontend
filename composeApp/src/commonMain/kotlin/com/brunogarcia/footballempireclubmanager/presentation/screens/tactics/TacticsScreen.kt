package com.brunogarcia.footballempireclubmanager.presentation.screens.tactics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.brunogarcia.footballempireclubmanager.domain.model.Player

class TacticsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<TacticsScreenModel>()
        val state by screenModel.state.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(if (state.selectedSlotId == null) "Táticas (4-3-3)" else "Escolher Jogador")
                    },
                    navigationIcon = {
                        // Se estiver na lista de seleção, mostra um botão para cancelar
                        if (state.selectedSlotId != null) {
                            IconButton(onClick = { screenModel.closePlayerSelection() }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Cancelar")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (state.selectedSlotId == null) {
                    // MODO 1: Ver a Tática
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.slots) { slot ->
                            TacticSlotItem(slot = slot, onClick = { screenModel.openPlayerSelection(slot.id) })
                        }
                    }
                } else {
                    // MODO 2: Lista do Plantel para Selecionar
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.squad) { player ->
                            PlayerSelectionItem(player = player, onClick = { screenModel.assignPlayerToSlot(player) })
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TacticSlotItem(slot: TacticSlot, onClick: () -> Unit) {
        val hasPlayer = slot.player != null
        val bgColor = if (hasPlayer) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer

        Card(
            modifier = Modifier.fillMaxWidth().clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = bgColor)
        ) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Posição no campo
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(slot.role.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }

                Spacer(modifier = Modifier.width(12.dp))

                if (slot.player != null) {
                    // Jogador colocado
                    Column(modifier = Modifier.weight(1f)) {
                        Text(slot.player.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text("Condição: ${slot.player.stamina}%", fontSize = 12.sp, color = Color.Gray)
                    }

                    // Mostra o Overall EFETIVO (Se puser o GR a avançado, ele vai avisar aqui)
                    val effectiveOverall = slot.player.getEffectiveOverall(slot.role)
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(effectiveOverall.toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                } else {
                    // Slot Vazio
                    Text("Posição Vazia", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onErrorContainer)
                    Icon(Icons.Filled.Add, contentDescription = "Adicionar", tint = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }

    @Composable
    private fun PlayerSelectionItem(player: Player, onClick: () -> Unit) {
        Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
            Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(player.mainPosition.name, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(player.name, fontWeight = FontWeight.SemiBold)
                    Text("Origem: ${player.mainPosition.name} | Stamina: ${player.stamina}%", fontSize = 12.sp)
                }
            }
        }
    }
}