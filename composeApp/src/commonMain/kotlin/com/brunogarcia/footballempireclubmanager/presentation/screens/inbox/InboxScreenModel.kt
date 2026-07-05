package com.brunogarcia.footballempireclubmanager.presentation.screens.inbox

import cafe.adriel.voyager.core.model.ScreenModel
import com.brunogarcia.footballempireclubmanager.domain.model.Player
import com.brunogarcia.footballempireclubmanager.domain.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Estado que representa os e-mails/notificações da caixa de entrada.
 */
data class InboxState(
    val offers: List<Player> = emptyList()
)

/**
 * ScreenModel que gere a lógica das propostas e e-mails recebidos na caixa de entrada do utilizador.
 */
class InboxScreenModel(
    private val repository: GameRepository
) : ScreenModel {

    private val _state = MutableStateFlow(InboxState())
    val state: StateFlow<InboxState> = _state.asStateFlow()

    init {
        loadOffers()
    }

    /**
     * Carrega as propostas ativas recebidas pelos jogadores do plantel do utilizador.
     */
    fun loadOffers() {
        val userClubId = repository.getUserClubId()
        val playersWithOffers = repository.getAllPlayers()
            .filter { it.clubId == userClubId && it.transferOffer != null }
        
        _state.update { it.copy(offers = playersWithOffers) }
    }

    /**
     * Aceita a proposta de transferência de um jogador.
     */
    fun acceptOffer(player: Player) {
        val userClubId = repository.getUserClubId()
        val allClubs = repository.getAllClubs().toMutableList()
        val allPlayers = repository.getAllPlayers().toMutableList()

        val clubIndex = allClubs.indexOfFirst { it.id == userClubId }
        val playerIndex = allPlayers.indexOfFirst { it.id == player.id }
        val offerAmount = player.transferOffer ?: 0.0

        if (clubIndex != -1 && playerIndex != -1 && offerAmount > 0.0) {
            val club = allClubs[clubIndex]

            // 1. Adiciona o dinheiro da venda ao orçamento do utilizador
            allClubs[clubIndex] = club.copy(budget = club.budget + offerAmount)

            // 2. Transfere o jogador para o clube comprador da IA
            val buyerClub = allClubs.find { it.name == player.offerClubName }
            val newClubId = buyerClub?.id ?: ""

            // Registar no Histórico de Transferências da Liga
            val transferEvent = com.brunogarcia.footballempireclubmanager.domain.model.TransferEvent(
                week = repository.getCurrentWeek(),
                playerName = player.name,
                playerPosition = player.mainPosition.name,
                overall = player.getBaseOverall(player.mainPosition),
                fromClubName = club.name,
                toClubName = buyerClub?.name ?: "Desconhecido",
                fee = offerAmount
            )
            val currentHistory = repository.getTransferHistory().toMutableList()
            currentHistory.add(transferEvent)
            repository.saveTransferHistory(currentHistory)

            val updatedPlayer = allPlayers[playerIndex].copy(
                clubId = newClubId,
                isListed = false,
                transferOffer = null,
                offerClubName = null,
                contractYears = 2, // Novo contrato de 2 anos no clube comprador
                lastTransferWeek = repository.getCurrentWeek()
            )
            allPlayers[playerIndex] = updatedPlayer

            repository.updateClubsAndPlayers(allClubs, allPlayers)
            repository.saveGameToDisk()

            loadOffers()
        }
    }

    /**
     * Rejeita a proposta de transferência de um jogador.
     */
    fun rejectOffer(player: Player) {
        val allPlayers = repository.getAllPlayers().toMutableList()
        val index = allPlayers.indexOfFirst { it.id == player.id }
        if (index != -1) {
            val updatedPlayer = allPlayers[index].copy(
                transferOffer = null,
                offerClubName = null
            )
            allPlayers[index] = updatedPlayer
            repository.updateClubsAndPlayers(repository.getAllClubs(), allPlayers)
            repository.saveGameToDisk()

            loadOffers()
        }
    }
}
