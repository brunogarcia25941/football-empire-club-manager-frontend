package com.brunogarcia.footballempireclubmanager.domain.usecase

import com.brunogarcia.footballempireclubmanager.domain.engine.MatchResult
import com.brunogarcia.footballempireclubmanager.domain.model.Club
import com.brunogarcia.footballempireclubmanager.domain.model.LeagueTableEntry

class CalculateLeagueTableUseCase {

    fun execute(clubs: List<Club>, results: List<MatchResult>): List<LeagueTableEntry> {
        // Criamos um mapa para facilitar a atualização de cada clube
        val tableMap = clubs.associate { it.id to LeagueTableEntry(it.id, it.name) }.toMutableMap()

        for (res in results) {
            val home = tableMap[res.homeClubId] ?: continue
            val away = tableMap[res.awayClubId] ?: continue

            val isHomeWin = res.homeGoals > res.awayGoals
            val isAwayWin = res.awayGoals > res.homeGoals
            val isDraw = res.homeGoals == res.awayGoals

            // Atualizar Equipa da Casa
            tableMap[res.homeClubId] = home.copy(
                played = home.played + 1,
                wins = home.wins + (if (isHomeWin) 1 else 0),
                draws = home.draws + (if (isDraw) 1 else 0),
                losses = home.losses + (if (isAwayWin) 1 else 0),
                goalsFor = home.goalsFor + res.homeGoals,
                goalsAgainst = home.goalsAgainst + res.awayGoals,
                points = home.points + (if (isHomeWin) 3 else if (isDraw) 1 else 0)
            )

            // Atualizar Equipa de Fora
            tableMap[res.awayClubId] = away.copy(
                played = away.played + 1,
                wins = away.wins + (if (isAwayWin) 1 else 0),
                draws = away.draws + (if (isDraw) 1 else 0),
                losses = away.losses + (if (isHomeWin) 1 else 0),
                goalsFor = away.goalsFor + res.awayGoals,
                goalsAgainst = away.goalsAgainst + res.homeGoals,
                points = away.points + (if (isAwayWin) 3 else if (isDraw) 1 else 0)
            )
        }

        // Ordenar por Pontos, depois Diferença de Golos, depois Golos Marcados
        return tableMap.values.toList().sortedWith(
            compareByDescending<LeagueTableEntry> { it.points }
                .thenByDescending { it.goalDifference }
                .thenByDescending { it.goalsFor }
        )
    }
}