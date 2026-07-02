package com.brunogarcia.footballempireclubmanager.presentation.screens.youthacademy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.brunogarcia.footballempireclubmanager.domain.model.Player
import com.brunogarcia.footballempireclubmanager.domain.model.Position
import com.brunogarcia.footballempireclubmanager.presentation.components.GlassCard
import com.brunogarcia.footballempireclubmanager.presentation.theme.AlertRed
import com.brunogarcia.footballempireclubmanager.presentation.theme.DarkNavy
import com.brunogarcia.footballempireclubmanager.presentation.theme.MidnightBlue
import com.brunogarcia.footballempireclubmanager.presentation.theme.NeonCyan
import com.brunogarcia.footballempireclubmanager.presentation.theme.NeonGreen

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
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "ACADEMIA DE JUNIORES",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar", tint = NeonCyan)
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Column {
                            Text(
                                text = "ACADEMIA DE JUNIORES (${state.clubName.uppercase()})",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Nível de Desenvolvimento: ${state.academyLevel}/10",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Novos juniores locais (idade 16-17) são gerados no final de cada época desportiva. Níveis de academia superiores geram promessas com melhores atributos iniciais.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Aguarde pela transição de época para receber um novo lote de jovens promessas para avaliar.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "JOVENS PROMESSAS DISPONÍVEIS (${state.youthPlayers.size})",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonCyan,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.youthPlayers) { player ->
                                YouthPlayerRow(player) {
                                    screenModel.onPlayerClicked(player)
                                }
                            }
                        }
                    }
                }
            }

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
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = com.brunogarcia.footballempireclubmanager.presentation.theme.GlassSurface
            ),
            border = BorderStroke(
                1.dp,
                com.brunogarcia.footballempireclubmanager.presentation.theme.GlassBorder.copy(alpha = 0.15f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = player.mainPosition.name,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = player.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${player.age} anos",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val overall = player.getEffectiveOverall(player.mainPosition)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = overall.toString(),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = NeonCyan
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
            containerColor = DarkNavy,
            title = {
                Column {
                    Text(text = player.name, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        text = "${player.age} anos | ${player.mainPosition.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(color = com.brunogarcia.footballempireclubmanager.presentation.theme.GlassBorder.copy(alpha = 0.2f))

                    Text(
                        text = "Capacidades Técnicas".uppercase(), 
                        fontWeight = FontWeight.Black, 
                        color = NeonCyan, 
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    
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

                    if (player.mainPosition == Position.GK) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Específicos Guarda-Redes".uppercase(), 
                            fontWeight = FontWeight.Black, 
                            color = NeonCyan, 
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            AttributeItem("Reflexos", player.gkReflexes)
                            AttributeItem("Mãos", player.gkHandling)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            AttributeItem("Agilidade", player.gkAgility)
                        }
                    }

                    HorizontalDivider(color = com.brunogarcia.footballempireclubmanager.presentation.theme.GlassBorder.copy(alpha = 0.2f))

                    Text(
                        text = "Decisão sobre o Junior".uppercase(), 
                        fontWeight = FontWeight.Black, 
                        color = NeonCyan, 
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Ao promover o jogador para a equipa principal, ele receberá um contrato padrão de 3 anos. Se for dispensado, ele sairá permanentemente da academia.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onPromote,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGreen,
                                contentColor = MidnightBlue
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("PROMOVER", fontWeight = FontWeight.Black)
                        }

                        Button(
                            onClick = onDismissPlayer,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AlertRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("DISPENSAR", fontWeight = FontWeight.Black)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = NeonCyan)
                ) {
                    Text("CANCELAR", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    @Composable
    private fun AttributeItem(label: String, value: Int) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$label: ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            val attrColor = when {
                value >= 85 -> NeonCyan
                value >= 70 -> NeonGreen
                value >= 50 -> Color(0xFFFBC02D)
                else -> AlertRed
            }
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Black,
                color = attrColor
            )
        }
    }
}
