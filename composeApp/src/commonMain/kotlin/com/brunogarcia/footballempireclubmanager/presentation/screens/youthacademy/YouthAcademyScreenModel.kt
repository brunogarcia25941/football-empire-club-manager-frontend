package com.brunogarcia.footballempireclubmanager.presentation.screens.youthacademy

import cafe.adriel.voyager.core.model.ScreenModel
import com.brunogarcia.footballempireclubmanager.domain.model.Player
import com.brunogarcia.footballempireclubmanager.domain.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class YouthAcademyState(
    val clubName: String = "",
    val youthPlayers: List<Player> = emptyList(),
    val selectedPlayer: Player? = null,
    val academyLevel: Int = 1
)

class YouthAcademyScreenModel(
    private val repository: GameRepository
) : ScreenModel {

    private val _state = MutableStateFlow(YouthAcademyState())
    val state: StateFlow<YouthAcademyState> = _state.asStateFlow()

    init {
        loadYouthAcademy()
    }

    fun loadYouthAcademy() {
        val userClubId = repository.getUserClubId()
        val club = repository.getAllClubs().find { it.id == userClubId }
        val clubName = club?.name ?: "Academia"
        val academyLevel = club?.youthAcademyLevel ?: 1

        val youthPlayers = repository.getAllPlayers()
            .filter { it.clubId == "YOUTH_$userClubId" }
            .sortedByDescending { it.getEffectiveOverall(it.mainPosition) }

        _state.update {
            it.copy(
                clubName = clubName,
                youthPlayers = youthPlayers,
                academyLevel = academyLevel
            )
        }
    }

    fun onPlayerClicked(player: Player) {
        _state.update { it.copy(selectedPlayer = player) }
    }

    fun onDismissDialog() {
        _state.update { it.copy(selectedPlayer = null) }
    }

    fun promotePlayer(player: Player) {
        val userClubId = repository.getUserClubId()
        val allPlayers = repository.getAllPlayers().toMutableList()
        val index = allPlayers.indexOfFirst { it.id == player.id }
        if (index != -1) {
            // Promove mudando o clubId para o do utilizador e definindo contrato padrão de 3 anos
            allPlayers[index] = allPlayers[index].copy(
                clubId = userClubId,
                contractYears = 3
            )
            repository.updateClubsAndPlayers(repository.getAllClubs(), allPlayers)
            repository.saveGameToDisk()

            _state.update { it.copy(selectedPlayer = null) }
            loadYouthAcademy()
        }
    }

    fun dismissPlayer(player: Player) {
        val allPlayers = repository.getAllPlayers().toMutableList()
        val index = allPlayers.indexOfFirst { it.id == player.id }
        if (index != -1) {
            // Remove o jogador da base de dados completamente
            allPlayers.removeAt(index)
            repository.updateClubsAndPlayers(repository.getAllClubs(), allPlayers)
            repository.saveGameToDisk()

            _state.update { it.copy(selectedPlayer = null) }
            loadYouthAcademy()
        }
    }
}
