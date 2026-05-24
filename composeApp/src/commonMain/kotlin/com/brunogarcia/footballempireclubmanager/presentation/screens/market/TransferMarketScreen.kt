package com.brunogarcia.footballempireclubmanager.presentation.screens.market

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

/**
 * Função utilitária para formatar valores monetários (ex: 1.000.000 €).
 */
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
            topBar = {
                TopAppBar(
                    title = { Text("Mercado de Transferências") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                
                // 1. Barra de Pesquisa por Nome
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { screenModel.onSearchQueryChanged(it) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Pesquisar jogador...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // 2. Filtros Rápidos por Posição (Horizontal)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedPosition == null,
                            onClick = { screenModel.onPositionFilterChanged(null) },
                            label = { Text("Todos") }
                        )
                    }
                    items(positions) { pos ->
                        FilterChip(
                            selected = state.selectedPosition == pos,
                            onClick = { screenModel.onPositionFilterChanged(pos) },
                            label = { Text(pos) }
                        )
                    }
                }

                // 3. Resumo Financeiro
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("O teu Orçamento:", fontWeight = FontWeight.Bold)
                        Text(formatPrice(state.myBudget), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // 4. Lista Principal de Jogadores
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredPlayers) { item ->
                        PlayerMarketCard(item = item, canAfford = state.myBudget >= item.price) {
                            screenModel.onPlayerClicked(item) // Abre os detalhes
                        }
                    }
                    
                    if (state.filteredPlayers.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Nenhum jogador encontrado.", color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // POPUP DE DETALHES E CONFIRMAÇÃO
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

    /**
     * Cartão individual de cada jogador na lista.
     */
    @Composable
    private fun PlayerMarketCard(item: MarketItem, canAfford: Boolean, onClick: () -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable { onClick() }, // Clicar no cartão abre os detalhes
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicador de Posição e Overall
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(50.dp)) {
                    Text(item.player.mainPosition.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(item.overall.toString(), fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Informação Principal
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.player.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Clube: ${item.sellerClubName}", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(formatPrice(item.price), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
                }

                // Ícone indicativo
                Icon(
                    Icons.Filled.ShoppingCart,
                    contentDescription = null,
                    tint = if (canAfford) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f)
                )
            }
        }
    }

    /**
     * Dialog detalhado que mostra todos os atributos do jogador antes da compra.
     */
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
            title = {
                Column {
                    Text(player.name, fontWeight = FontWeight.Bold)
                    Text("${player.age} anos | ${player.mainPosition.name}", style = MaterialTheme.typography.bodySmall)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Divider()
                    
                    // Secção de Atributos Físicos e Mentais
                    Text("Atributos de Campo", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    
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

                    // Atributos de Guarda-Redes (Só mostra se for relevante)
                    if (player.mainPosition == com.brunogarcia.footballempireclubmanager.domain.model.Position.GK) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Guarda-Redes", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            AttributeItem("Reflexos", player.gkReflexes)
                            AttributeItem("Mãos", player.gkHandling)
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Informação Financeira
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Preço de Venda:", fontWeight = FontWeight.Bold)
                        Text(formatPrice(item.price), color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    }

                    if (!canAfford) {
                        Text(
                            "Não tens orçamento suficiente para esta transferência.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Confirmar Compra")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        )
    }

    /**
     * Componente pequeno para mostrar um atributo (Nome: Valor)
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
                    value >= 85 -> Color(0xFF1B5E20) // Verde forte
                    value >= 70 -> Color(0xFF4CAF50) // Verde
                    value >= 50 -> Color(0xFFFBC02D) // Amarelo
                    else -> Color(0xFFD32F2F)        // Vermelho
                }
            )
        }
    }
}