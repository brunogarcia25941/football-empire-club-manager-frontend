package com.brunogarcia.footballempireclubmanager.domain.usecase

import com.brunogarcia.footballempireclubmanager.domain.engine.StartingPlayer
import com.brunogarcia.footballempireclubmanager.domain.repository.GameRepository

class AdvanceTimeUseCase(
    private val repository: GameRepository,
    private val simulateMatchweekUseCase: SimulateMatchweekUseCase,
    private val processWeeklyUpdatesUseCase: ProcessWeeklyUpdatesUseCase
) {

    /**
     * @param userStarting11 O 11 inicial que o utilizador escolheu no ecrã tático.
     */
    fun execute(userStarting11: List<StartingPlayer>) {
        val allClubs = repository.getAllClubs()
        val allPlayers = repository.getAllPlayers()
        val userClubId = repository.getUserClubId()

        // 1. Gerar o calendário da semana (Para testar, pomos o Clube 0 contra o Clube 1)
        // No futuro, teremos um CalendarGeneratorUseCase a sério!
        val fixtures = listOf(
            Pair(allClubs[0], allClubs[1]) // Águias vs Dragões (Baseado no teu JSON)
        )

        // 2. Simular os jogos (O Motor entra em ação)
        val weeklyResults = simulateMatchweekUseCase.execute(
            fixtures = fixtures,
            allPlayers = allPlayers,
            userClubId = userClubId,
            userStarting11 = userStarting11
        )

        // 3. Processar as finanças e o cansaço
        processWeeklyUpdatesUseCase.execute(
            allClubs = allClubs,
            allPlayers = allPlayers,
            weeklyResults = weeklyResults
        )

        // 4. Gravar tudo de volta na "Base de Dados" (Repositório)
        repository.updateClubsAndPlayers(allClubs, allPlayers)
        repository.saveMatchResults(weeklyResults)
        repository.advanceWeek()
    }
}