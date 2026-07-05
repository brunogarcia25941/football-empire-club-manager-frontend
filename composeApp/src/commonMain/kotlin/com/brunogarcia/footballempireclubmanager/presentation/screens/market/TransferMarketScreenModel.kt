package com.brunogarcia.footballempireclubmanager.presentation.screens.market

import cafe.adriel.voyager.core.model.ScreenModel
import com.brunogarcia.footballempireclubmanager.domain.model.Player
import com.brunogarcia.footballempireclubmanager.domain.repository.GameRepository
import com.brunogarcia.footballempireclubmanager.domain.usecase.BuyPlayerUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Representa um item no mercado, incluindo o jogador e informações de venda.
 */
data class MarketItem(
    val player: Player,
    val sellerClubName: String,
    val overall: Int,
    val price: Double
)

/**
 * Estado do ecrã do Mercado de Transferências.
 */
data class MarketState(
    val myBudget: Double = 0.0,
    val allMarketPlayers: List<MarketItem> = emptyList(), // Lista completa vinda da BD
    val filteredPlayers: List<MarketItem> = emptyList(),  // Lista filtrada para exibir
    val searchQuery: String = "",
    val selectedPosition: String? = null, // Filtro de posição (null = Todas)
    val selectedPlayerForDetails: MarketItem? = null, // Jogador selecionado para o popup de detalhes
    val transferHistory: List<com.brunogarcia.footballempireclubmanager.domain.model.TransferEvent> = emptyList()
)

class TransferMarketScreenModel(
    private val repository: GameRepository,
    private val buyPlayerUseCase: BuyPlayerUseCase
) : ScreenModel {

    private val _state = MutableStateFlow(MarketState())
    val state: StateFlow<MarketState> = _state.asStateFlow()

    init {
        loadMarket()
    }

    /**
     * Carrega os dados iniciais do mercado.
     */
    fun loadMarket() {
        val userClubId = repository.getUserClubId()
        val allClubs = repository.getAllClubs()
        val allPlayers = repository.getAllPlayers()
        val myClub = allClubs.find { it.id == userClubId } ?: return

        // Filtramos jogadores que não pertencem ao clube do utilizador
        val playersForSale = allPlayers
            .filter { it.clubId != userClubId }
            .map { p ->
                val sellerName = allClubs.find { it.id == p.clubId }?.name ?: "Agente Livre"
                val ovr = p.getBaseOverall(p.mainPosition)
                MarketItem(
                    player = p,
                    sellerClubName = sellerName,
                    overall = ovr,
                    price = buyPlayerUseCase.calculatePlayerValue(p)
                )
            }
            .sortedByDescending { it.overall }

        val history = repository.getTransferHistory().reversed() // Ordenar pelas mais recentes

        _state.update {
            it.copy(
                myBudget = myClub.budget,
                allMarketPlayers = playersForSale,
                filteredPlayers = playersForSale,
                transferHistory = history
            )
        }
    }

    /**
     * Define o jogador selecionado para abrir o popup de detalhes.
     */
    fun onPlayerClicked(item: MarketItem) {
        _state.update { it.copy(selectedPlayerForDetails = item) }
    }

    /**
     * Limpa a seleção para fechar o popup.
     */
    fun onDismissDialog() {
        _state.update { it.copy(selectedPlayerForDetails = null) }
    }

    /**
     * Atualiza o termo de pesquisa e reaplica os filtros.
     */
    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    /**
     * Atualiza o filtro de posição e reaplica os filtros.
     */
    fun onPositionFilterChanged(pos: String?) {
        _state.update { it.copy(selectedPosition = pos) }
        applyFilters()
    }

    /**
     * Filtra a lista principal com base na pesquisa e na posição selecionada.
     */
    private fun applyFilters() {
        _state.update { currentState ->
            val filtered = currentState.allMarketPlayers.filter { item ->
                val matchesSearch = item.player.name.contains(currentState.searchQuery, ignoreCase = true)
                val matchesPosition = currentState.selectedPosition == null || 
                                     item.player.mainPosition.name == currentState.selectedPosition
                matchesSearch && matchesPosition
            }
            currentState.copy(filteredPlayers = filtered)
        }
    }

    /**
     * Processa a compra de um jogador através do UseCase.
     */
    fun buyPlayer(item: MarketItem) {
        val success = buyPlayerUseCase.execute(item.player.id, item.price)
        if (success) {
            loadMarket() // Atualiza a lista e o orçamento após a compra
            onDismissDialog() // Fecha o popup
        }
    }
}