package com.brunogarcia.footballempireclubmanager.presentation.screens.squad

import cafe.adriel.voyager.core.model.ScreenModel
import com.brunogarcia.footballempireclubmanager.domain.model.Player
import com.brunogarcia.footballempireclubmanager.domain.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Representa o estado do ecrã do Plantel.
 */
data class SquadState(
    val clubName: String = "",
    val players: List<Player> = emptyList(),
    val selectedPlayer: Player? = null // Jogador selecionado para ver os stats detalhados
)

class SquadScreenModel(
    private val repository: GameRepository
) : ScreenModel {

    private val _state = MutableStateFlow(SquadState())
    val state: StateFlow<SquadState> = _state.asStateFlow()

    init {
        loadSquad()
    }

    /**
     * Carrega e ordena os jogadores do clube do utilizador.
     */
    fun loadSquad() {
        val userClubId = repository.getUserClubId()
        val clubName = repository.getAllClubs().find { it.id == userClubId }?.name ?: "Plantel"

        // Filtra só os teus jogadores e ordena-os por Posição e depois por Overall
        val myPlayers = repository.getAllPlayers()
            .filter { it.clubId == userClubId }
            .sortedWith(
                compareBy<Player> { it.mainPosition }
                    .thenByDescending { it.getEffectiveOverall(it.mainPosition) }
            )

        _state.update {
            it.copy(
                clubName = clubName,
                players = myPlayers
            )
        }
    }

    /**
     * Seleciona um jogador para abrir o popup de detalhes.
     */
    fun onPlayerClicked(player: Player) {
        _state.update { it.copy(selectedPlayer = player) }
    }

    /**
     * Fecha o popup de detalhes.
     */
    fun onDismissDialog() {
        _state.update { it.copy(selectedPlayer = null) }
    }

    /**
     * Coloca ou retira um jogador da lista de transferências.
     */
    fun toggleListing(player: Player) {
        val allPlayers = repository.getAllPlayers().toMutableList()
        val index = allPlayers.indexOfFirst { it.id == player.id }
        if (index != -1) {
            val updatedPlayer = allPlayers[index].copy(isListed = !allPlayers[index].isListed)
            allPlayers[index] = updatedPlayer
            repository.updateClubsAndPlayers(repository.getAllClubs(), allPlayers)
            repository.saveGameToDisk()

            _state.update { it.copy(selectedPlayer = updatedPlayer) }
            loadSquad()
        }
    }

    /**
     * Renova o contrato do jogador por mais 3 anos.
     * Custa uma taxa de assinatura de 10% do valor do jogador (OVR^2 * 100 €).
     */
    fun renewContract(player: Player) {
        val userClubId = repository.getUserClubId()
        val allClubs = repository.getAllClubs().toMutableList()
        val allPlayers = repository.getAllPlayers().toMutableList()
        val clubIndex = allClubs.indexOfFirst { it.id == userClubId }
        val playerIndex = allPlayers.indexOfFirst { it.id == player.id }

        if (clubIndex != -1 && playerIndex != -1) {
            val club = allClubs[clubIndex]
            val renewalCost = player.getMarketValue() * 0.1

            if (club.budget >= renewalCost) {
                allClubs[clubIndex] = club.copy(budget = club.budget - renewalCost)
                val updatedPlayer = allPlayers[playerIndex].copy(contractYears = allPlayers[playerIndex].contractYears + 3)
                allPlayers[playerIndex] = updatedPlayer

                repository.updateClubsAndPlayers(allClubs, allPlayers)
                repository.saveGameToDisk()

                _state.update { it.copy(selectedPlayer = updatedPlayer) }
                loadSquad()
            }
        }
    }

    /**
     * Aceita a proposta de transferência da IA, vendendo o jogador.
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

            // 2. Transfere o jogador para o clube comprador
            val buyerClub = allClubs.find { it.name == player.offerClubName }
            val newClubId = buyerClub?.id ?: ""

            val updatedPlayer = allPlayers[playerIndex].copy(
                clubId = newClubId,
                isListed = false,
                transferOffer = null,
                offerClubName = null,
                contractYears = 2, // Novo contrato com o novo clube
                lastTransferWeek = repository.getCurrentWeek()
            )
            allPlayers[playerIndex] = updatedPlayer

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

            repository.updateClubsAndPlayers(allClubs, allPlayers)
            repository.saveGameToDisk()

            // Fecha o diálogo de detalhes (já não é nosso)
            _state.update { it.copy(selectedPlayer = null) }
            loadSquad()
        }
    }

    /**
     * Rejeita a proposta de transferência da IA.
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

            _state.update { it.copy(selectedPlayer = updatedPlayer) }
            loadSquad()
        }
    }
}