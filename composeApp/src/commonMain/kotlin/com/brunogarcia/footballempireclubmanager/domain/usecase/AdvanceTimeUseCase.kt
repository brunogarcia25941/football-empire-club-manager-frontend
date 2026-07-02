package com.brunogarcia.footballempireclubmanager.domain.usecase

import com.brunogarcia.footballempireclubmanager.domain.engine.MatchResult
import com.brunogarcia.footballempireclubmanager.domain.engine.StartingPlayer
import com.brunogarcia.footballempireclubmanager.domain.model.Club
import com.brunogarcia.footballempireclubmanager.domain.model.Fixture
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

        // Se por acaso já não houver jogos (Fim da Época), não avança o tempo!
        if (thisWeekFixtures.isEmpty()) return

        // 2. Simular os jogos (O Motor entra em ação)
        val weeklyResults = simulateMatchweekUseCase.execute(
            thisWeekFixtures = thisWeekFixtures,
            allClubs = allClubs,
            allPlayers = allPlayers,
            userClubId = userClubId,
            userStarting11 = userStarting11
        )

        // 3. Processar as finanças e o cansaço
        processWeeklyUpdatesUseCase.execute(
            allClubs = allClubs,
            allPlayers = allPlayers,
            weeklyResults = weeklyResults,
            userClubId = userClubId,
            currentWeek = currentWeek
        )

        // 4. Sortear a eliminatória seguinte da Taça se o jogo que acabou de decorrer for de Taça
        drawNextCupRound(currentWeek, allClubs, weeklyResults)

        // 5. Gravar tudo de volta na "Base de Dados" (Repositório)
        repository.updateClubsAndPlayers(allClubs, allPlayers)
        repository.saveMatchResults(weeklyResults)
        repository.advanceWeek()
    }

    /**
     * Sorteia dinamicamente os jogos da eliminatória seguinte da Taça Nacional.
     */
    private fun drawNextCupRound(currentWeek: Int, allClubs: List<Club>, weeklyResults: List<MatchResult>) {
        val nextCupWeek = when (currentWeek) {
            3 -> 6   // Fim da Preliminar (Semana 3) -> Sorteia Oitavos (Semana 6)
            6 -> 10  // Fim dos Oitavos (Semana 6) -> Sorteia Quartos (Semana 10)
            10 -> 14 // Fim dos Quartos (Semana 10) -> Sorteia Meias (Semana 14)
            14 -> 18 // Fim das Meias (Semana 14) -> Sorteia Final (Semana 18)
            else -> return // Não é uma semana de sorteio da Taça
        }

        val currentWeekCupResults = weeklyResults.filter { it.isCup }
        val currentWinners = currentWeekCupResults.map { res ->
            if (res.homeGoals > res.awayGoals) res.homeClubId else res.awayClubId
        }

        val participatingTeams = mutableListOf<String>()

        if (currentWeek == 3) {
            // No fim da preliminar (Semana 3):
            // Juntamos os 2 vencedores da preliminar com as outras 14 equipas que tiveram isenção (byes)
            participatingTeams.addAll(currentWinners)
            
            // Descobrimos quais foram os 4 clubes que jogaram a preliminar
            val prelimClubIds = currentWeekCupResults.flatMap { listOf(it.homeClubId, it.awayClubId) }.toSet()
            // As restantes 14 equipas entram agora
            val remainingClubs = allClubs.filter { it.id !in prelimClubIds }.map { it.id }
            participatingTeams.addAll(remainingClubs)
        } else {
            // Para as restantes eliminatórias (R16, QF, SF):
            // Apenas participam os vencedores dos jogos desta semana!
            participatingTeams.addAll(currentWinners)
        }

        // 2. Sortear confrontos (baralhar e agrupar em pares)
        val shuffledTeams = participatingTeams.shuffled().toMutableList()
        val newCupFixtures = mutableListOf<Fixture>()

        while (shuffledTeams.size >= 2) {
            val home = shuffledTeams.removeFirst()
            val away = shuffledTeams.removeFirst()
            newCupFixtures.add(Fixture(week = nextCupWeek, homeClubId = home, awayClubId = away, isCup = true))
        }

        // 3. Adicionar estas novas fixtures às já existentes no repositório
        val currentFixtures = repository.getFixtures().toMutableList()
        currentFixtures.addAll(newCupFixtures)
        repository.saveFixtures(currentFixtures)
    }
}