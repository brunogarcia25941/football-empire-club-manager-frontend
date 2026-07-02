package com.brunogarcia.footballempireclubmanager.presentation.screens.matchreport

import cafe.adriel.voyager.core.model.ScreenModel
import com.brunogarcia.footballempireclubmanager.domain.engine.MatchEvent
import com.brunogarcia.footballempireclubmanager.domain.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Representa o estado do ecrã de Relatório de Jornada ou de Jogo Individual.
 */
data class MatchReportState(
    val results: List<MatchResultItem> = emptyList(), // Para o modo Jornada
    val singleMatchResult: SingleMatchData? = null,    // Para o modo Individual
    val currentWeek: Int = 0,
    val isLoading: Boolean = true
)

/**
 * Modelo para exibir o nome dos clubes na lista de jornada.
 */
data class MatchResultItem(
    val homeClubName: String,
    val awayClubName: String,
    val homeGoals: Int,
    val awayGoals: Int,
    val homeColor: String,
    val awayColor: String
)

/**
 * Modelo para exibir detalhes de um único jogo.
 */
data class SingleMatchData(
    val homeClubName: String,
    val awayClubName: String,
    val homeGoals: Int,
    val awayGoals: Int,
    val events: List<MatchEvent>
)

class MatchReportScreenModel(private val repository: GameRepository) : ScreenModel {

    private val _state = MutableStateFlow(MatchReportState())
    val state: StateFlow<MatchReportState> = _state.asStateFlow()

    /**
     * Carrega todos os resultados da jornada (semana) anterior.
     */
    fun loadMatchweekResults() {
        _state.update { it.copy(isLoading = true) }
        val allClubs = repository.getAllClubs()
        val currentWeek = repository.getCurrentWeek()
        val history = repository.getMatchHistory()
        val fixtures = repository.getFixtures()

        val lastWeek = currentWeek - 1
        val lastWeekFixtures = fixtures.filter { it.week == lastWeek }

        val results = lastWeekFixtures.mapNotNull { fixture ->
            val match = history.find { 
                it.homeClubId == fixture.homeClubId && it.awayClubId == fixture.awayClubId && it.isCup == fixture.isCup
            }
            val homeClub = allClubs.find { it.id == fixture.homeClubId }
            val awayClub = allClubs.find { it.id == fixture.awayClubId }

            if (match != null && homeClub != null && awayClub != null) {
                MatchResultItem(
                    homeClubName = homeClub.name,
                    awayClubName = awayClub.name,
                    homeGoals = match.homeGoals,
                    awayGoals = match.awayGoals,
                    homeColor = homeClub.primaryColor,
                    awayColor = awayClub.primaryColor
                )
            } else null
        }

        _state.update {
            it.copy(
                results = results,
                currentWeek = lastWeek,
                isLoading = false,
                singleMatchResult = null
            )
        }
    }

    /**
     * Carrega detalhes de um jogo específico (para o calendário).
     */
    fun loadSingleMatch(homeClubId: String, awayClubId: String, isCup: Boolean = false) {
        _state.update { it.copy(isLoading = true) }
        val allClubs = repository.getAllClubs()
        val homeClub = allClubs.find { it.id == homeClubId }
        val awayClub = allClubs.find { it.id == awayClubId }
        val history = repository.getMatchHistory()

        val matchResult = history.find { it.homeClubId == homeClubId && it.awayClubId == awayClubId && it.isCup == isCup }

        if (homeClub != null && awayClub != null && matchResult != null) {
            _state.update {
                it.copy(
                    singleMatchResult = SingleMatchData(
                        homeClubName = homeClub.name,
                        awayClubName = awayClub.name,
                        homeGoals = matchResult.homeGoals,
                        awayGoals = matchResult.awayGoals,
                        events = matchResult.events.sortedBy { e -> e.minute }
                    ),
                    isLoading = false
                )
            }
        }
    }
}