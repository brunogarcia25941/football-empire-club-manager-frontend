package com.brunogarcia.footballempireclubmanager.presentation.screens.newseason

import cafe.adriel.voyager.core.model.ScreenModel
import com.brunogarcia.footballempireclubmanager.domain.model.LeagueTableEntry
import com.brunogarcia.footballempireclubmanager.domain.repository.GameRepository
import com.brunogarcia.footballempireclubmanager.domain.usecase.CalculateLeagueTableUseCase
import com.brunogarcia.footballempireclubmanager.domain.usecase.StartNewSeasonUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Estado que representa os dados a mostrar no ecrã de resumo de época.
 */
data class NewSeasonState(
    val clubName: String = "",
    val finalPosition: Int = 0,
    val prizeMoney: Double = 0.0,
    val oldBudget: Double = 0.0,
    val newBudget: Double = 0.0,
    val finalTable: List<LeagueTableEntry> = emptyList()
)

/**
 * ScreenModel responsável por carregar o resumo de final de época e coordenar a transição.
 */
class NewSeasonScreenModel(
    private val repository: GameRepository,
    private val calculateLeagueTableUseCase: CalculateLeagueTableUseCase,
    private val startNewSeasonUseCase: StartNewSeasonUseCase
) : ScreenModel {

    private val _state = MutableStateFlow(NewSeasonState())
    val state: StateFlow<NewSeasonState> = _state

    init {
        loadSeasonSummary()
    }

    /**
     * Carrega a classificação final da época que terminou e calcula a projeção de orçamentos.
     */
    private fun loadSeasonSummary() {
        val userClubId = repository.getUserClubId()
        val allClubs = repository.getAllClubs()
        val matchHistory = repository.getMatchHistory()

        val userClub = allClubs.find { it.id == userClubId }
        val division = userClub?.divisionLevel ?: 1
        val finalTable = calculateLeagueTableUseCase.execute(allClubs, matchHistory, division)
        val userPositionIndex = finalTable.indexOfFirst { it.clubId == userClubId }
        val userPosition = if (userPositionIndex != -1) userPositionIndex + 1 else 10
        val oldBudget = userClub?.budget ?: 0.0

        val prize = when (userPosition) {
            1 -> 10000000.0
            2 -> 8000000.0
            3 -> 6000000.0
            4 -> 5000000.0
            5 -> 4000000.0
            else -> 2000000.0
        }

        _state.value = NewSeasonState(
            clubName = userClub?.name ?: "O teu Clube",
            finalPosition = userPosition,
            prizeMoney = prize,
            oldBudget = oldBudget,
            newBudget = oldBudget + prize,
            finalTable = finalTable
        )
    }

    /**
     * Inicia a transição de época no caso de uso e notifica o ecrã.
     */
    fun startNewSeason(onFinished: () -> Unit) {
        startNewSeasonUseCase.execute()
        onFinished()
    }
}
