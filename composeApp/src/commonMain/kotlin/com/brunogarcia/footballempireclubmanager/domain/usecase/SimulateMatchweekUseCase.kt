package com.brunogarcia.footballempireclubmanager.domain.usecase

import com.brunogarcia.footballempireclubmanager.domain.engine.MatchEngine
import com.brunogarcia.footballempireclubmanager.domain.engine.MatchResult
import com.brunogarcia.footballempireclubmanager.domain.engine.StartingPlayer
import com.brunogarcia.footballempireclubmanager.domain.model.Club
import com.brunogarcia.footballempireclubmanager.domain.model.Player
import com.brunogarcia.footballempireclubmanager.domain.model.Position
import com.brunogarcia.footballempireclubmanager.domain.model.Fixture

class SimulateMatchweekUseCase {

    /**
     * @param thisWeekFixtures A lista de Fixtures (jogos agendados) para simular nesta semana.
     * @param allClubs A lista de clubes na liga.
     * @param allPlayers A base de dados completa de jogadores no save atual.
     * @param userClubId O ID do clube controlado pelo utilizador.
     * @param userStarting11 O 11 inicial definido no ecrã de Táticas.
     */
    fun execute(
        thisWeekFixtures: List<Fixture>,
        allClubs: List<Club>,
        allPlayers: List<Player>,
        userClubId: String,
        userStarting11: List<StartingPlayer>
    ): List<MatchResult> {
        val weeklyResults = mutableListOf<MatchResult>()

        for (fixture in thisWeekFixtures) {
            val homeClub = allClubs.find { it.id == fixture.homeClubId } ?: continue
            val awayClub = allClubs.find { it.id == fixture.awayClubId } ?: continue

            // 1. Prepara a Equipa da Casa
            val homeTeam11 = if (homeClub.id == userClubId && userStarting11.isNotEmpty()) {
                userStarting11
            } else {
                autoPickStarting11(homeClub.id, allPlayers)
            }

            // 2. Prepara a Equipa de Fora
            val awayTeam11 = if (awayClub.id == userClubId && userStarting11.isNotEmpty()) {
                userStarting11
            } else {
                autoPickStarting11(awayClub.id, allPlayers)
            }

            // 3. Simula o Jogo
            val result = MatchEngine.simulateMatch(
                homeClubId = homeClub.id,
                homeTeam = homeTeam11,
                awayClubId = awayClub.id,
                awayTeam = awayTeam11,
                isCup = fixture.isCup
            )

            weeklyResults.add(result)
        }

        return weeklyResults
    }

    /**
     * Inteligência Artificial básica para o PC escolher o 11 inicial:
     * Escolhe 1 Guarda-Redes e os 10 melhores jogadores de campo baseados no Overall.
     */
    private fun autoPickStarting11(clubId: String, allPlayers: List<Player>): List<StartingPlayer> {
        val clubPlayers = allPlayers.filter { it.clubId == clubId }
        if (clubPlayers.isEmpty()) return emptyList()

        val starting11 = mutableListOf<StartingPlayer>()

        // Tenta encontrar o melhor GR
        val goalkeepers = clubPlayers.filter { it.mainPosition == Position.GK }
            .sortedByDescending { it.getEffectiveOverall(Position.GK) }

        if (goalkeepers.isNotEmpty()) {
            starting11.add(StartingPlayer(goalkeepers.first(), Position.GK))
        }

        // Tenta encontrar os 10 melhores jogadores de campo nas suas posições de origem
        val fieldPlayers = clubPlayers.filter { it.mainPosition != Position.GK }
            .sortedByDescending { it.getEffectiveOverall(it.mainPosition) }
            .take(10)

        fieldPlayers.forEach { player ->
            starting11.add(StartingPlayer(player, player.mainPosition))
        }

        return starting11
    }
}