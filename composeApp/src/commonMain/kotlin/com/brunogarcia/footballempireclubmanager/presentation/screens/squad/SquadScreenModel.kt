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
}