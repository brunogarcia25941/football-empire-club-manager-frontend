package com.brunogarcia.footballempireclubmanager.domain.usecase

import com.brunogarcia.footballempireclubmanager.domain.model.Club
import com.brunogarcia.footballempireclubmanager.domain.model.Fixture

class GenerateFixturesUseCase {

    fun execute(clubs: List<Club>): List<Fixture> {
        val fixtures = mutableListOf<Fixture>()
        val clubIds = clubs.map { it.id }.toMutableList()

        // Se por acaso tivermos um número ímpar de equipas, adicionamos uma equipa "Fantasma" (Bye)
        // (Mas no JSON deve se tentar ter sempre equipas pares)
        if (clubIds.size % 2 != 0) {
            clubIds.add("BYE")
        }

        val numTeams = clubIds.size
        val totalRounds = numTeams - 1

        // Algoritmo Round-Robin Padrão (1ª Volta)
        for (round in 0 until totalRounds) {
            for (i in 0 until numTeams / 2) {
                val home = clubIds[i]
                val away = clubIds[numTeams - 1 - i]

                if (home != "BYE" && away != "BYE") {
                    // Alternar casa/fora para equilibrar
                    if (i % 2 == 0) {
                        fixtures.add(Fixture(week = round + 1, homeClubId = home, awayClubId = away))
                    } else {
                        fixtures.add(Fixture(week = round + 1, homeClubId = away, awayClubId = home))
                    }
                }
            }
            // Rodar a lista: Manter o 1º fixo (índice 0) e mover o último para o índice 1
            val last = clubIds.removeLast()
            clubIds.add(1, last)
        }

        // Criar a 2ª Volta (Espelho da 1ª volta com os locais invertidos)
        val secondHalf = fixtures.map {
            Fixture(week = it.week + totalRounds, homeClubId = it.awayClubId, awayClubId = it.homeClubId)
        }

        return fixtures + secondHalf
    }
}