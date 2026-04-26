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

        // 1. Ler os jogos agendados para a semana atual
        val currentWeek = repository.getCurrentWeek()
        val allFixtures = repository.getFixtures()

        // Filtra só os jogos desta semana
        val thisWeekFixtures = allFixtures.filter { it.week == currentWeek }

        // Mapeia os IDs para os objetos Club
        val fixtures = thisWeekFixtures.mapNotNull { fixture ->
            val home = allClubs.find { it.id == fixture.homeClubId }
            val away = allClubs.find { it.id == fixture.awayClubId }
            if (home != null && away != null) Pair(home, away) else null
        }

        // Se por acaso já não houver jogos (Fim da Época), não avança o tempo!
        if (fixtures.isEmpty()) return

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