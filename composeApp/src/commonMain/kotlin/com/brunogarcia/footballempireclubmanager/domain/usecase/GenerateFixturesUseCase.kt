package com.brunogarcia.footballempireclubmanager.domain.usecase

import com.brunogarcia.footballempireclubmanager.domain.model.Club
import com.brunogarcia.footballempireclubmanager.domain.model.Fixture
import kotlin.random.Random

/**
 * Caso de Uso responsável por gerar o calendário de jogos para a Liga e a Taça de forma intercalada.
 */
class GenerateFixturesUseCase {

    fun execute(clubs: List<Club>): List<Fixture> {
        val fixtures = mutableListOf<Fixture>()

        // 1. Separar os clubes por divisões
        val div1Clubs = clubs.filter { it.divisionLevel == 1 }
        val div2Clubs = clubs.filter { it.divisionLevel == 2 }

        // 2. Gerar calendário da 1ª Divisão (10 equipas -> 18 jornadas)
        val div1Fixtures = generateLeagueFixtures(div1Clubs, 18)
        fixtures.addAll(div1Fixtures)

        // 3. Gerar calendário da 2ª Divisão (8 equipas -> 14 jornadas)
        val div2Fixtures = generateLeagueFixtures(div2Clubs, 14)
        fixtures.addAll(div2Fixtures)

        // 4. Gerar a Eliminatória Preliminar da Taça Nacional (Semana 3)
        // Como temos 18 equipas (não potência de 2), sorteamos 4 equipas da 2ª divisão
        // para jogarem um playoff preliminar. Os 2 vencedores juntam-se às outras 14 equipas nos Oitavos.
        if (div2Clubs.size >= 4) {
            val shuffledDiv2 = div2Clubs.shuffled()
            val prelimTeams = shuffledDiv2.take(4)
            
            fixtures.add(Fixture(week = 3, homeClubId = prelimTeams[0].id, awayClubId = prelimTeams[1].id, isCup = true))
            fixtures.add(Fixture(week = 3, homeClubId = prelimTeams[2].id, awayClubId = prelimTeams[3].id, isCup = true))
        }

        return fixtures
    }

    /**
     * Mapeia uma jornada teórica da liga (1 a N) para a semana real do calendário (intercalando as semanas da Taça).
     */
    private fun getWeekForLeagueRound(round: Int): Int {
        return when {
            round <= 2 -> round                   // Jornadas 1, 2 -> Semanas 1, 2
            round <= 4 -> round + 1               // Jornadas 3, 4 -> Semanas 4, 5 (Semana 3: Preliminar Taça)
            round <= 7 -> round + 2               // Jornadas 5..7 -> Semanas 7..9 (Semana 6: Oitavos Taça)
            round <= 10 -> round + 3              // Jornadas 8..10 -> Semanas 11..13 (Semana 10: Quartos Taça)
            round <= 13 -> round + 4              // Jornadas 11..13 -> Semanas 15..17 (Semana 14: Meias Taça)
            else -> round + 5                     // Jornadas 14..18 -> Semanas 19..23 (Semana 18: Final Taça)
        }
    }

    /**
     * Gera jogos em formato Round-Robin para um conjunto de clubes e ajusta as semanas de acordo com o calendário da Taça.
     */
    private fun generateLeagueFixtures(clubs: List<Club>, totalRounds: Int): List<Fixture> {
        val leagueFixtures = mutableListOf<Fixture>()
        val clubIds = clubs.map { it.id }.toMutableList()

        if (clubIds.size % 2 != 0) {
            clubIds.add("BYE")
        }

        val numTeams = clubIds.size
        val rounds = numTeams - 1

        val tempFixtures = mutableListOf<Fixture>()

        // 1ª Volta
        for (round in 0 until rounds) {
            for (i in 0 until numTeams / 2) {
                val home = clubIds[i]
                val away = clubIds[numTeams - 1 - i]

                if (home != "BYE" && away != "BYE") {
                    val isHome = if (i == 0) round % 2 == 0 else i % 2 == 0
                    if (isHome) {
                        tempFixtures.add(Fixture(week = round + 1, homeClubId = home, awayClubId = away))
                    } else {
                        tempFixtures.add(Fixture(week = round + 1, homeClubId = away, awayClubId = home))
                    }
                }
            }
            val last = clubIds.removeLast()
            clubIds.add(1, last)
        }

        // 2ª Volta (Espelho invertido)
        val secondHalf = tempFixtures.map {
            Fixture(week = it.week + rounds, homeClubId = it.awayClubId, awayClubId = it.homeClubId)
        }

        val allFixtures = tempFixtures + secondHalf

        // Mapeia cada jogo para a semana real ajustando a escala
        return allFixtures
            .filter { it.week <= totalRounds } // Garante que respeita o limite de jornadas (ex: 14 jornadas para a Div 2)
            .map { it.copy(week = getWeekForLeagueRound(it.week)) }
    }
}