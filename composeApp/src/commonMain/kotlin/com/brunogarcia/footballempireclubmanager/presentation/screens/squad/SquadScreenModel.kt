package com.brunogarcia.footballempireclubmanager.presentation.screens.squad

import cafe.adriel.voyager.core.model.ScreenModel
import com.brunogarcia.footballempireclubmanager.domain.model.Player
import com.brunogarcia.footballempireclubmanager.domain.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SquadState(
    val clubName: String = "",
    val players: List<Player> = emptyList()
)

class SquadScreenModel(
    private val repository: GameRepository
) : ScreenModel {

    private val _state = MutableStateFlow(SquadState())
    val state: StateFlow<SquadState> = _state

    init {
        loadSquad()
    }

    private fun loadSquad() {
        val userClubId = repository.getUserClubId()
        val clubName = repository.getAllClubs().find { it.id == userClubId }?.name ?: "Plantel"

        // Filtra só os teus jogadores e ordena-os por Posição (do GR para o ST) e depois por Overall
        val myPlayers = repository.getAllPlayers()
            .filter { it.clubId == userClubId }
            .sortedWith(
                compareBy<Player> { it.mainPosition }
                    .thenByDescending { it.getEffectiveOverall(it.mainPosition) }
            )

        _state.value = SquadState(
            clubName = clubName,
            players = myPlayers
        )
    }
}