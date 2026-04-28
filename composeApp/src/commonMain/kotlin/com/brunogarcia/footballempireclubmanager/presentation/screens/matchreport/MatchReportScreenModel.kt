package com.brunogarcia.footballempireclubmanager.presentation.screens.matchreport

import cafe.adriel.voyager.core.model.ScreenModel
import com.brunogarcia.footballempireclubmanager.domain.engine.MatchEvent
import com.brunogarcia.footballempireclubmanager.domain.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class MatchReportState(
    val homeClubName: String = "",
    val awayClubName: String = "",
    val homeGoals: Int = 0,
    val awayGoals: Int = 0,
    val events: List<MatchEvent> = emptyList(),
    val isLoading: Boolean = true
)

class MatchReportScreenModel(private val repository: GameRepository) : ScreenModel {

    private val _state = MutableStateFlow(MatchReportState())
    val state: StateFlow<MatchReportState> = _state

    fun loadMatch(homeClubId: String, awayClubId: String) {
        val allClubs = repository.getAllClubs()
        val homeClub = allClubs.find { it.id == homeClubId }
        val awayClub = allClubs.find { it.id == awayClubId }
        val history = repository.getMatchHistory()

        // Procurar o jogo exato no histórico
        val matchResult = history.find { it.homeClubId == homeClubId && it.awayClubId == awayClubId }

        if (homeClub != null && awayClub != null && matchResult != null) {
            _state.value = MatchReportState(
                homeClubName = homeClub.name,
                awayClubName = awayClub.name,
                homeGoals = matchResult.homeGoals,
                awayGoals = matchResult.awayGoals,
                events = matchResult.events.sortedBy { it.minute }, // Ordenar cronologicamente
                isLoading = false
            )
        }
    }
}