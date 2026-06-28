package com.brunogarcia.footballempireclubmanager.presentation.screens.youthacademy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.brunogarcia.footballempireclubmanager.domain.model.Player
import com.brunogarcia.footballempireclubmanager.domain.model.Position

class YouthAcademyScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<YouthAcademyScreenModel>()
        val state by screenModel.state.collectAsState()

        LaunchedEffect(Unit) {
            screenModel.loadYouthAcademy()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Academia de Juniores") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Info Card sobre o estado da Academia
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Academia de Juniores (${state.clubName})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Nível atual: ${state.academyLevel}/10",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Novos juniores locais (idade 16-17) são gerados no final de cada época desportiva. Níveis de academia superiores geram promessas com melhores atributos iniciais.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                if (state.youthPlayers.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Não há juniores sob observação",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aguarde pela transição de época para receber um novo lote de jovens promessas para avaliar.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Jovens Promessas Disponíveis (${state.youthPlayers.size})",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.youthPlayers) { player ->
                            YouthPlayerRow(player) {
                                screenModel.onPlayerClicked(player)
                            }
                        }
                    }
                }
            }

            // Popup de Detalhes do Junior
            state.selectedPlayer?.let { player ->
                YouthPlayerDetailsDialog(
                    player = player,
                    onDismiss = { screenModel.onDismissDialog() },
                    onPromote = { screenModel.promotePlayer(player) },
                    onDismissPlayer = { screenModel.dismissPlayer(player) }
                )
            }
        }
    }

    @Composable
    private fun YouthPlayerRow(player: Player, onClick: () -> Unit) {
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

                // Nome e idade
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = player.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${player.age} anos",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
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

    @Composable
    private fun YouthPlayerDetailsDialog(
        player: Player,
        onDismiss: () -> Unit,
        onPromote: () -> Unit,
        onDismissPlayer: () -> Unit
    ) {
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
                    HorizontalDivider()

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
                    if (player.mainPosition == Position.GK) {
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

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text("Decisão sobre o Junior", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Ao promover o jogador para a equipa principal, ele receberá um contrato padrão de 3 anos. Se for dispensado, ele sairá permanentemente da academia.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onPromote,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("Promover", color = Color.White)
                        }

                        Button(
                            onClick = onDismissPlayer,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Dispensar", color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        )
    }

    @Composable
    private fun AttributeItem(label: String, value: Int) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$label: ", style = MaterialTheme.typography.bodySmall)
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = when {
                    value >= 85 -> Color(0xFF1B5E20)
                    value >= 70 -> Color(0xFF4CAF50)
                    value >= 50 -> Color(0xFFFBC02D)
                    else -> Color(0xFFD32F2F)
                }
            )
        }
    }
}
