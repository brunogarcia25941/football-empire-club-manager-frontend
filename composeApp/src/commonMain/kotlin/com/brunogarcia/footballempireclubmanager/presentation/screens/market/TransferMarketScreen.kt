package com.brunogarcia.footballempireclubmanager.presentation.screens.market

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.brunogarcia.footballempireclubmanager.presentation.components.GlassCard
import com.brunogarcia.footballempireclubmanager.presentation.theme.MidnightBlue
import com.brunogarcia.footballempireclubmanager.presentation.theme.DarkNavy
import com.brunogarcia.footballempireclubmanager.presentation.theme.NeonGreen
import com.brunogarcia.footballempireclubmanager.presentation.theme.NeonCyan
import com.brunogarcia.footballempireclubmanager.presentation.theme.AlertRed

fun formatPrice(value: Double): String {
    val numberString = value.toLong().toString()
    if (numberString.length <= 3) return "$numberString €"
    return numberString.reversed().chunked(3).joinToString(".").reversed() + " €"
}

class TransferMarketScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<TransferMarketScreenModel>()
        val state by screenModel.state.collectAsState()

        val positions = listOf("GK", "CB", "LB", "RB", "CDM", "CM", "CAM", "RW", "LW", "ST")

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "MERCADO DE TRANSFERÊNCIAS",
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
                Column(modifier = Modifier.fillMaxSize()) {
                    
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { screenModel.onSearchQueryChanged(it) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Pesquisar jogador...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyan) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = com.brunogarcia.footballempireclubmanager.presentation.theme.GlassBorder,
                            focusedContainerColor = DarkNavy.copy(alpha = 0.6f),
                            unfocusedContainerColor = DarkNavy.copy(alpha = 0.3f)
                        )
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = state.selectedPosition == null,
                                onClick = { screenModel.onPositionFilterChanged(null) },
                                label = { Text("TODOS", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan,
                                    selectedLabelColor = MidnightBlue,
                                    containerColor = DarkNavy.copy(alpha = 0.5f),
                                    labelColor = NeonCyan
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = state.selectedPosition == null,
                                    borderColor = NeonCyan.copy(alpha = 0.5f),
                                    selectedBorderColor = NeonCyan
                                )
                            )
                        }
                        items(positions) { pos ->
                            FilterChip(
                                selected = state.selectedPosition == pos,
                                onClick = { screenModel.onPositionFilterChanged(pos) },
                                label = { Text(pos, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan,
                                    selectedLabelColor = MidnightBlue,
                                    containerColor = DarkNavy.copy(alpha = 0.5f),
                                    labelColor = NeonCyan
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = state.selectedPosition == pos,
                                    borderColor = NeonCyan.copy(alpha = 0.5f),
                                    selectedBorderColor = NeonCyan
                                )
                            )
                        }
                    }

                    GlassCard(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ORÇAMENTO DISPONÍVEL:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Text(formatPrice(state.myBudget), fontWeight = FontWeight.Black, color = NeonGreen, fontSize = 18.sp)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.filteredPlayers) { item ->
                            PlayerMarketCard(item = item, canAfford = state.myBudget >= item.price) {
                                screenModel.onPlayerClicked(item)
                            }
                        }
                        
                        if (state.filteredPlayers.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Nenhum jogador listado de momento.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            state.selectedPlayerForDetails?.let { item ->
                PlayerDetailsDialog(
                    item = item,
                    canAfford = state.myBudget >= item.price,
                    onDismiss = { screenModel.onDismissDialog() },
                    onConfirm = { screenModel.buyPlayer(item) }
                )
            }
        }
    }

    @Composable
    private fun PlayerMarketCard(item: MarketItem, canAfford: Boolean, onClick: () -> Unit) {
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
                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(50.dp)) {
                    Text(item.player.mainPosition.name, fontWeight = FontWeight.Black, fontSize = 11.sp, color = NeonCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(NeonCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(item.overall.toString(), fontWeight = FontWeight.Black, color = NeonCyan, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(item.player.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    Text("Clube vendedor: ${item.sellerClubName} | Idade: ${item.player.age}a", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(formatPrice(item.price), fontWeight = FontWeight.Black, color = NeonGreen, fontSize = 15.sp)
                }

                Icon(
                    Icons.Filled.ShoppingCart,
                    contentDescription = "Comprar",
                    tint = if (canAfford) NeonCyan else AlertRed.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    @Composable
    private fun PlayerDetailsDialog(
        item: MarketItem,
        canAfford: Boolean,
        onDismiss: () -> Unit,
        onConfirm: () -> Unit
    ) {
        val player = item.player

        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = DarkNavy,
            title = {
                Column {
                    Text(player.name, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${player.age} anos | ${player.mainPosition.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(color = com.brunogarcia.footballempireclubmanager.presentation.theme.GlassBorder.copy(alpha = 0.2f))
                    
                    Text("Capacidades Técnicas".uppercase(), fontWeight = FontWeight.Black, color = NeonCyan, fontSize = 12.sp, letterSpacing = 1.sp)
                    
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

                    if (player.mainPosition == com.brunogarcia.footballempireclubmanager.domain.model.Position.GK) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Guarda-Redes", fontWeight = FontWeight.Black, color = NeonCyan, fontSize = 12.sp)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            AttributeItem("Reflexos", player.gkReflexes)
                            AttributeItem("Mãos", player.gkHandling)
                        }
                    }

                    HorizontalDivider(color = com.brunogarcia.footballempireclubmanager.presentation.theme.GlassBorder.copy(alpha = 0.2f))
                    
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Preço Solicitado:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatPrice(item.price), color = NeonGreen, fontWeight = FontWeight.Black)
                    }

                    if (!canAfford) {
                        Text(
                            text = "Não tens orçamento disponível para fechar este negócio.",
                            color = AlertRed,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = MidnightBlue,
                        disabledContainerColor = NeonGreen.copy(alpha = 0.12f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("CONFIRMAR COMPRA", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
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