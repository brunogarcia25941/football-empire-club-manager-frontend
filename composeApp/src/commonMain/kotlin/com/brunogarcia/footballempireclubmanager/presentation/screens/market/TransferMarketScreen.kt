package com.brunogarcia.footballempireclubmanager.presentation.screens.market

import androidx.compose.foundation.background
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


// Função para formatar o dinheiro (ex: 1000000 -> 1.000.000 €)
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

                // 1. Barra de Pesquisa
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { screenModel.onSearchQueryChanged(it) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    placeholder = { Text("Pesquisar jogador...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // 2. Filtros de Posição (Chips)
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

                // 3. Orçamento
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("O teu Orçamento:", fontWeight = FontWeight.Bold)
                        Text(formatPrice(state.myBudget), fontWeight = FontWeight.ExtraBold)
                    }
                }

                // 4. Lista de Jogadores Filtrada
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredPlayers) { item ->
                        PlayerMarketCard(item = item, canAfford = state.myBudget >= item.price) {
                            screenModel.buyPlayer(item)
                        }
                    }

                    if (state.filteredPlayers.isEmpty()) {
                        item {
                            Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    "Nenhum jogador encontrado com estes filtros.",
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
            1
        }
    }

    @Composable
    private fun PlayerMarketCard(item: MarketItem, canAfford: Boolean, onBuy: () -> Unit) {
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(50.dp)
                )
                {
                    Text(
                        item.player.mainPosition.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Box(
                        modifier =
                            Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            item.overall.toString(),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.player.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(item.sellerClubName, fontSize = 12.sp, color = Color.Gray)
                    Text(
                        formatPrice(item.price), fontWeight = FontWeight.SemiBold, color =
                            MaterialTheme.colorScheme.secondary
                    )
                }
                IconButton(onClick = onBuy, enabled = canAfford) {
                    Icon(
                        Icons.Filled.ShoppingCart, contentDescription = "Comprar",
                        tint = if (canAfford) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
        }
    }
}