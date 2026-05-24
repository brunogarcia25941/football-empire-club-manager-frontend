package com.brunogarcia.footballempireclubmanager.presentation.screens.squad

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.brunogarcia.footballempireclubmanager.domain.model.Player

class SquadScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<SquadScreenModel>()
        val state by screenModel.state.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Plantel - ${state.clubName}") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.players) { player ->
                        PlayerRow(player) {
                            screenModel.onPlayerClicked(player)
                        }
                    }
                }

                // Popup de Detalhes do Jogador
                state.selectedPlayer?.let { player ->
                    PlayerDetailsDialog(
                        player = player,
                        onDismiss = { screenModel.onDismissDialog() }
                    )
                }
            }
        }
    }

    /**
     * Linha de cada jogador na lista do plantel.
     */
    @Composable
    private fun PlayerRow(player: Player, onClick: () -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable { onClick() },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Posição
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = player.mainPosition.name,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Informação Base (Nome e Barra de Stamina)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = player.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Físico:", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        val staminaColor = if (player.stamina < 60) Color.Red else Color(0xFF4CAF50)
                        LinearProgressIndicator(
                            progress = { player.stamina / 100f },
                            modifier = Modifier.height(6.dp).width(60.dp).clip(RoundedCornerShape(3.dp)),
                            color = staminaColor,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${player.stamina}%", fontSize = 12.sp)
                    }
                }

                // Overall Principal
                val overall = player.getEffectiveOverall(player.mainPosition)
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = overall.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }

    /**
     * Diálogo que exibe todos os atributos técnicos e físicos do jogador.
     */
    @Composable
    private fun PlayerDetailsDialog(player: Player, onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Column {
                    Text(text = player.name, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${player.age} anos | ${player.mainPosition.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Divider()

                    // Atributos de Campo
                    Text("Capacidades Técnicas", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        AttributeItem("Velocidade", player.pace)
                        AttributeItem("Força", player.strength)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        AttributeItem("Corte", player.tackling)
                        AttributeItem("Pos. Def", player.defensivePositioning)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        AttributeItem("Passe", player.passing)
                        AttributeItem("Visão", player.vision)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        AttributeItem("Drible", player.dribbling)
                        AttributeItem("Finalização", player.finishing)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        AttributeItem("Pos. Of", player.offensivePositioning)
                        AttributeItem("Cabeceamento", player.heading)
                    }

                    // Atributos de Guarda-Redes (Apenas se for GR)
                    if (player.mainPosition == com.brunogarcia.footballempireclubmanager.domain.model.Position.GK) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Específicos Guarda-Redes", fontWeight = FontWeight.Bold, color = Color(0xFFE91E63))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            AttributeItem("Reflexos", player.gkReflexes)
                            AttributeItem("Mãos", player.gkHandling)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            AttributeItem("Agilidade", player.gkAgility)
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Morale e Estado
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Moral:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${player.morale}%",
                            fontWeight = FontWeight.Bold,
                            color = if (player.morale > 70) Color(0xFF4CAF50) else Color.Red
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Fechar")
                }
            }
        )
    }

    /**
     * Componente pequeno para exibir um atributo individual com cores dinâmicas.
     */
    @Composable
    private fun AttributeItem(label: String, value: Int) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$label: ", style = MaterialTheme.typography.bodySmall)
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = when {
                    value >= 85 -> Color(0xFF1B5E20) // Craque
                    value >= 70 -> Color(0xFF4CAF50) // Bom
                    value >= 50 -> Color(0xFFFBC02D) // Médio
                    else -> Color(0xFFD32F2F)        // Fraco
                }
            )
        }
    }
}