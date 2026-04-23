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
    val isLoading: Boolean = false
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
        // Vai buscar o ID do clube que o jogador escolheu
        val userClubId = repository.getUserClubId()
        // Procura o clube na base de dados
        val userClub = repository.getAllClubs().find { it.id == userClubId }

        if (userClub != null) {
            _state.value = _state.value.copy(
                clubName = userClub.name,
                budget = userClub.budget,
                currentWeek = repository.getCurrentWeek()
            )
        }
    }

    fun onAdvanceWeekClicked() {
        _state.value = _state.value.copy(isLoading = true)

        // No futuro, passamos aqui o 11 inicial real do utilizador.
        // Por agora, passamos uma lista vazia só para testar o motor.
        advanceTimeUseCase.execute(userStarting11 = emptyList())

        // Recarrega os dados para a UI atualizar o dinheiro e a semana
        loadDashboardData()

        _state.value = _state.value.copy(isLoading = false)
    }
}