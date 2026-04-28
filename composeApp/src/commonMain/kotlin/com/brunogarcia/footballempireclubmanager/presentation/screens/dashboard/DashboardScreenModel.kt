package com.brunogarcia.footballempireclubmanager.presentation.screens.dashboard

import cafe.adriel.voyager.core.model.ScreenModel
import com.brunogarcia.footballempireclubmanager.domain.model.Club
import com.brunogarcia.footballempireclubmanager.domain.repository.GameRepository
import com.brunogarcia.footballempireclubmanager.domain.usecase.AdvanceTimeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// O que o ecrã precisa de mostrar
data class DashboardState(
    val clubName: String = "A Carregar...",
    val budget: Double = 0.0,
    val currentWeek: Int = 1,
    val isLoading: Boolean = false,
    val lastMatchResult: String? = null,
    val nextMatchText: String = "A carregar...",
    val nextMatchLoc: String = ""
)

class DashboardScreenModel(
    private val repository: GameRepository,
    private val advanceTimeUseCase: AdvanceTimeUseCase
) : ScreenModel {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val userClubId = repository.getUserClubId()
        val allClubs = repository.getAllClubs()
        val userClub = allClubs.find { it.id == userClubId }
        val currentWeek = repository.getCurrentWeek()
        val allFixtures = repository.getFixtures() // Lemos o Calendário!

        if (userClub != null) {

            // 1. DESCOBRIR O PRÓXIMO JOGO
            val nextFixture = allFixtures.find {
                it.week == currentWeek && (it.homeClubId == userClubId || it.awayClubId == userClubId)
            }

            var nextMatchStr = "Fim da Época"
            var nextLocStr = ""

            if (nextFixture != null) {
                // Temos Jogo!
                val isHome = nextFixture.homeClubId == userClubId
                val opponentId = if (isHome) nextFixture.awayClubId else nextFixture.homeClubId
                val opponentName = allClubs.find { it.id == opponentId }?.name ?: "Desconhecido"

                nextMatchStr = "VS $opponentName"
                nextLocStr = if (isHome) "Em Casa" else "Fora"
            } else if (allFixtures.any { it.week == currentWeek }) {
                // Outras equipas jogam, mas nós não
                nextMatchStr = "Folga (Descanso)"
                nextLocStr = "Sem Jogo"
            }

            // 2. LER O ÚLTIMO JOGO (Protegido contra Folgas)
            val lastWeekFixture = allFixtures.find {
                it.week == currentWeek - 1 && (it.homeClubId == userClubId || it.awayClubId == userClubId)
            }

            var lastMatchStr: String? = null

            if (lastWeekFixture != null) {
                // Jogámos mesmo na semana passada, podemos ir buscar o resultado
                val history = repository.getMatchHistory()
                val userLastMatch = history.lastOrNull { it.homeClubId == userClubId || it.awayClubId == userClubId }

                if (userLastMatch != null) {
                    val homeName = allClubs.find { it.id == userLastMatch.homeClubId }?.name ?: "Casa"
                    val awayName = allClubs.find { it.id == userLastMatch.awayClubId }?.name ?: "Fora"
                    lastMatchStr = "$homeName ${userLastMatch.homeGoals} - ${userLastMatch.awayGoals} $awayName"
                }
            } else if (currentWeek > 1) {
                lastMatchStr = "Folga na última semana"
            }

            _state.value = DashboardState(
                clubName = userClub.name,
                budget = userClub.budget,
                currentWeek = currentWeek,
                isLoading = false,
                lastMatchResult = lastMatchStr,
                nextMatchText = nextMatchStr,
                nextMatchLoc = nextLocStr
            )
        }
    }

    fun onAdvanceWeekClicked() {
        _state.value = _state.value.copy(isLoading = true)

        //  Vamos buscar a tática guardada no repositório
        val myStarting11 = repository.getUserStarting11()

        // enviamos para o motor de jogo
        advanceTimeUseCase.execute(userStarting11 = myStarting11)

        // Recarrega os dados para a UI atualizar o dinheiro e a semana
        loadDashboardData()
        // Auto-save no final da semana
        repository.saveGameToDisk()

        _state.value = _state.value.copy(isLoading = false)
    }
}