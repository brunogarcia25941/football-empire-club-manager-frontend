package com.brunogarcia.footballempireclubmanager.presentation.screens.market

import cafe.adriel.voyager.core.model.ScreenModel
import com.brunogarcia.footballempireclubmanager.domain.model.Player
import com.brunogarcia.footballempireclubmanager.domain.repository.GameRepository
import com.brunogarcia.footballempireclubmanager.domain.usecase.BuyPlayerUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class MarketItem(
    val player: Player,
    val sellerClubName: String,
    val overall: Int,
    val price: Double
)

data class MarketState(
    val myBudget: Double = 0.0,
    val availablePlayers: List<MarketItem> = emptyList()
)

class TransferMarketScreenModel(
    private val repository: GameRepository,
    private val buyPlayerUseCase: BuyPlayerUseCase
) : ScreenModel {

    private val _state = MutableStateFlow(MarketState())
    val state: StateFlow<MarketState> = _state

    init {
        loadMarket()
    }

    private fun loadMarket() {
        val userClubId = repository.getUserClubId()
        val allClubs = repository.getAllClubs()
        val allPlayers = repository.getAllPlayers()

        val myClub = allClubs.find { it.id == userClubId } ?: return

        // Procura os jogadores de OUTROS clubes e cria os "MarketItems"
        val playersForSale = allPlayers
            .filter { it.clubId != userClubId } // Exclui os do utilizador
            .map { p ->
                val sellerName = allClubs.find { it.id == p.clubId }?.name ?: "Agente Livre"
                val ovr = p.getEffectiveOverall(p.mainPosition)
                MarketItem(
                    player = p,
                    sellerClubName = sellerName,
                    overall = ovr,
                    price = buyPlayerUseCase.calculatePlayerValue(ovr)
                )
            }
            .sortedByDescending { it.overall } // Ordenar do melhor para o pior

        _state.value = MarketState(
            myBudget = myClub.budget,
            availablePlayers = playersForSale
        )
    }

    fun buyPlayer(item: MarketItem) {
        val success = buyPlayerUseCase.execute(item.player.id, item.price)
        if (success) {
            loadMarket() // Recarrega a lista para o jogador desaparecer do mercado e atualizar o saldo
        }
    }
}