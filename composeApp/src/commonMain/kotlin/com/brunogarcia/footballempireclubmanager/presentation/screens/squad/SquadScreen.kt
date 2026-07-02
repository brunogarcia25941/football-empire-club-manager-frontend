package com.brunogarcia.footballempireclubmanager.presentation.screens.squad

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.brunogarcia.footballempireclubmanager.domain.model.Player
import com.brunogarcia.footballempireclubmanager.presentation.screens.facilities.formatMoney
import com.brunogarcia.footballempireclubmanager.presentation.theme.MidnightBlue
import com.brunogarcia.footballempireclubmanager.presentation.theme.DarkNavy
import com.brunogarcia.footballempireclubmanager.presentation.theme.NeonGreen
import com.brunogarcia.footballempireclubmanager.presentation.theme.NeonCyan
import com.brunogarcia.footballempireclubmanager.presentation.theme.AlertRed

class SquadScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<SquadScreenModel>()
        val state by screenModel.state.collectAsState()

        // Garante que o plantel é atualizado sempre que o ecrã fica visível
        LaunchedEffect(Unit) {
            screenModel.loadSquad()
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "PLANTEL - ${state.clubName.uppercase()}",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontSize = 18.sp
                        )
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        onDismiss = { screenModel.onDismissDialog() },
                        onToggleListing = { screenModel.toggleListing(player) },
                        onRenewContract = { screenModel.renewContract(player) },
                        onAcceptOffer = { screenModel.acceptOffer(player) },
                        onRejectOffer = { screenModel.rejectOffer(player) }
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
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Posição
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

                // Informação Base (Nome e Barra de Stamina)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = player.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Físico:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(6.dp))
                        val staminaColor = when {
                            player.stamina >= 80 -> NeonGreen
                            player.stamina >= 50 -> Color(0xFFFF9800)
                            else -> AlertRed
                        }
                        LinearProgressIndicator(
                            progress = { player.stamina / 100f },
                            modifier = Modifier
                                .height(6.dp)
                                .width(70.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = staminaColor,
                            trackColor = staminaColor.copy(alpha = 0.15f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${player.stamina}%", 
                            fontSize = 12.sp, 
                            color = staminaColor, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Overall Principal
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
    private fun PlayerDetailsDialog(
        player: Player,
        onDismiss: () -> Unit,
        onToggleListing: () -> Unit,
        onRenewContract: () -> Unit,
        onAcceptOffer: () -> Unit,
        onRejectOffer: () -> Unit
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

                    // Atributos de Campo
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

                    // Atributos de Guarda-Redes (Apenas se for GR)
                    if (player.mainPosition == com.brunogarcia.footballempireclubmanager.domain.model.Position.GK) {
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
                    
                    // Morale e Estado
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Moral do Atleta:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val moraleColor = if (player.morale >= 70) NeonGreen else AlertRed
                        Text(
                            text = "${player.morale}%",
                            fontWeight = FontWeight.Bold,
                            color = moraleColor
                        )
                    }

                    HorizontalDivider(color = com.brunogarcia.footballempireclubmanager.presentation.theme.GlassBorder.copy(alpha = 0.2f))

                    // Contrato e Gestão de Mercado
                    Text(
                        text = "Contrato e Mercado".uppercase(), 
                        fontWeight = FontWeight.Black, 
                        color = NeonCyan, 
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Contrato: ${player.contractYears} anos", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        // Preço real de renovação: 10% do valor do jogador no novo mercado exponencial
                        val renewalCost = player.getMarketValue() * 0.1
                        TextButton(
                            onClick = onRenewContract,
                            colors = ButtonDefaults.textButtonColors(contentColor = NeonCyan)
                        ) {
                            Text("Renovar (+3a) - ${formatMoney(renewalCost)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (player.isListed) "Listado para Venda" else "Não Listado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (player.isListed) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = onToggleListing,
                            colors = ButtonDefaults.textButtonColors(contentColor = NeonCyan)
                        ) {
                            Text(
                                text = if (player.isListed) "Retirar da Lista" else "Colocar à Venda", 
                                fontSize = 12.sp, 
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Se houver propostas de transferência ativas da IA
                    player.transferOffer?.let { offer ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = com.brunogarcia.footballempireclubmanager.presentation.theme.GlassSurface
                            ),
                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("PROPOSTA DE COMPRA RECEBIDA!", fontWeight = FontWeight.Black, fontSize = 12.sp, color = NeonCyan)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Clube Proponente: ${player.offerClubName}", fontSize = 12.sp, color = Color.White)
                                Text("Valor Proposto: ${formatMoney(offer)}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = NeonGreen)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Button(
                                        onClick = onAcceptOffer,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = NeonGreen,
                                            contentColor = MidnightBlue
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("ACEITAR", fontWeight = FontWeight.Black, fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = onRejectOffer,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AlertRed,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("REJEITAR", fontWeight = FontWeight.Black, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = NeonCyan)
                ) {
                    Text("FECHAR", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    /**
     * Componente pequeno para exibir um atributo individual com cores dinâmicas de eSports.
     */
    @Composable
    private fun AttributeItem(label: String, value: Int) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$label: ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            val attrColor = when {
                value >= 85 -> NeonCyan     // Nível Estrela (Ciano elétrico)
                value >= 70 -> NeonGreen    // Nível Bom (Verde elétrico)
                value >= 50 -> Color(0xFFFBC02D) // Nível Médio (Laranja/Amarelo)
                else -> AlertRed            // Nível Fraco (Vermelho)
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