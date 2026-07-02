package com.brunogarcia.footballempireclubmanager.presentation.screens.mainmenu

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.brunogarcia.footballempireclubmanager.domain.model.InitialDataWrapper
import com.brunogarcia.footballempireclubmanager.domain.repository.GameRepository
import com.brunogarcia.footballempireclubmanager.domain.usecase.GenerateFixturesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ScreenModel responsável por gerir a inicialização do jogo com o clube escolhido pelo utilizador.
 */
class SelectTeamScreenModel(
    private val initialData: InitialDataWrapper,
    private val repository: GameRepository,
    private val generateFixturesUseCase: GenerateFixturesUseCase
) : ScreenModel {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Inicializa a partida com o clube escolhido, gera calendário e guarda o save.
     */
    fun selectTeam(userClubId: String, onFinished: () -> Unit) {
        screenModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Divide os clubes: top 10 com base no orçamento vão para a Divisão 1, restantes 8 para a Divisão 2
                val sortedClubs = initialData.clubs.sortedByDescending { it.budget }
                val clubsWithDivisions = sortedClubs.mapIndexed { index, club ->
                    val division = if (index < 10) 1 else 2
                    club.copy(divisionLevel = division)
                }

                repository.initializeGame(clubsWithDivisions, initialData.players, userClubId)

                // 2. Gera e guarda o calendário da liga (agora suportando as duas divisões e a taça)
                val fixtures = generateFixturesUseCase.execute(clubsWithDivisions)
                repository.saveFixtures(fixtures)

                // 3. Grava o estado de progresso inicial no armazenamento do telemóvel
                repository.saveGameToDisk()

                // 4. Conclui com sucesso
                onFinished()
            } catch (e: Exception) {
                println("Erro ao inicializar o clube escolhido: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
