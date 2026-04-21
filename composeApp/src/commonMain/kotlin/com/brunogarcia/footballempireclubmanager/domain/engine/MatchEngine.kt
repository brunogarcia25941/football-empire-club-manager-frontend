package com.brunogarcia.footballempireclubmanager.domain.engine

import com.brunogarcia.footballempireclubmanager.domain.model.Position
import kotlin.random.Random
import kotlin.math.pow

object MatchEngine {

    /**
     * Simula um jogo de 90 minutos entre duas equipas.
     */
    fun simulateMatch(
        homeClubId: String,
        homeTeam: List<StartingPlayer>,
        awayClubId: String,
        awayTeam: List<StartingPlayer>
    ): MatchResult {
        val result = MatchResult(homeClubId, awayClubId)

        // 1. Calcular Força dos Setores (Média do Overall Efetivo)
        val homeAttack = calculateSectorStrength(homeTeam, isAttack = true)
        val homeMidfield = calculateSectorStrength(homeTeam, isMidfield = true)
        val homeDefense = calculateSectorStrength(homeTeam, isDefense = true)

        val awayAttack = calculateSectorStrength(awayTeam, isAttack = true)
        val awayMidfield = calculateSectorStrength(awayTeam, isMidfield = true)
        val awayDefense = calculateSectorStrength(awayTeam, isDefense = true)

        // Fator casa dá um pequeno bónus ao Meio-Campo (Posse de bola / Moral dos adeptos)
        val homeMidfieldAdvantage = homeMidfield * 1.05

        // 2. Loop dos 90 minutos
        for (minute in 1..90) {
            // Rolar um dado de 1 a 100
            val chance = Random.nextInt(1, 101)

            // Só há ~20% de probabilidade de haver uma jogada de perigo num minuto normal
            if (chance <= 20) {
                // Elevamos o meio-campo ao quadrado para o domínio ser mais realista
                val homeMidPower = homeMidfieldAdvantage.pow(2)
                val awayMidPower = awayMidfield.pow(2)

                val totalMidfieldPower = homeMidPower + awayMidPower
                val homePossessionChance = (homeMidPower / totalMidfieldPower) * 100.0

                // Quem tem posse de bola
                val possessionRoll = Random.nextDouble(0.0, 100.0)
                if (possessionRoll <= homePossessionChance) {
                    // Equipa da Casa Ataca!
                    if (isGoalScored(homeAttack, awayDefense)) {
                        result.homeGoals++
                        val scorer = pickGoalScorer(homeTeam)
                        result.events.add(MatchEvent(minute, MatchEventType.GOAL, scorer.player.id, scorer.player.name, homeClubId))
                    }
                } else {
                    // Equipa de Fora Ataca!
                    if (isGoalScored(awayAttack, homeDefense)) {
                        result.awayGoals++
                        val scorer = pickGoalScorer(awayTeam)
                        result.events.add(MatchEvent(minute, MatchEventType.GOAL, scorer.player.id, scorer.player.name, awayClubId))
                    }
                }
            }
        }

        return result
    }

    private fun isGoalScored(attackStrength: Double, defenseStrength: Double): Boolean {
        // Rácio base
        val baseRatio = attackStrength / defenseStrength.coerceAtLeast(1.0)

        // A Magia: Exponenciação para castigar o desnível!
        // ajustar este 2.0 se achares que há demasiadas goleadas (mudar para 1.5 ou 1.8)
        val exponentialRatio = baseRatio.pow(2.0)

        val goalProbability = (exponentialRatio * 15.0)

        return Random.nextDouble(0.0, 100.0) <= goalProbability
    }

    private fun calculateSectorStrength(team: List<StartingPlayer>, isAttack: Boolean = false, isMidfield: Boolean = false, isDefense: Boolean = false): Double {
        val filteredPlayers = team.filter {
            when {
                isAttack -> it.playingPosition in listOf(Position.ST, Position.LW, Position.RW)
                isMidfield -> it.playingPosition in listOf(Position.CAM, Position.CM, Position.CDM, Position.LM, Position.RM)
                isDefense -> it.playingPosition in listOf(Position.GK, Position.CB, Position.LB, Position.RB)
                else -> false
            }
        }

        if (filteredPlayers.isEmpty()) return 1.0 // Prevenir divisões por zero se faltarem jogadores

        // Faz a média do Overall Efetivo dos jogadores desse setor
        val sumOverall = filteredPlayers.sumOf { it.player.getEffectiveOverall(it.playingPosition) }
        return sumOverall.toDouble() / filteredPlayers.size
    }

    private fun pickGoalScorer(team: List<StartingPlayer>): StartingPlayer {
        // 1. Definimos o "peso" (probabilidade relativa) de cada posição marcar
        val positionWeights = mapOf(
            Position.ST to 30.0,
            Position.LW to 15.0,
            Position.RW to 15.0,
            Position.CAM to 15.0,
            Position.LM to 5.0,
            Position.RM to 5.0,
            Position.CM to 5.0,
            Position.CB to 3.0, // Golo de canto/bola parada
            Position.LB to 3.0,
            Position.RB to 3.0,
            Position.CDM to 1.0,
            Position.GK to 0.0  // Vamos manter o GR no zero para não haver absurdos
        )

        // 2. Associamos a cada jogador no campo o peso da posição onde está a jogar.
        // No futuro, se quiseres complicar, em vez de ser só o peso da posição,
        // podes multiplicar o peso da posição pelo atributo "finishing" do jogador!
        val playersWithWeights = team.map { startingPlayer ->
            val weight = positionWeights[startingPlayer.playingPosition] ?: 1.0
            Pair(startingPlayer, weight)
        }

        // 3. Somamos todos os pesos (ex: dá um total de 100 bilhetes)
        val totalWeight = playersWithWeights.sumOf { it.second }

        // Lançamos a roleta! Um número de 0 até ao peso total.
        val randomRoll = Random.nextDouble(0.0, totalWeight)

        // 4. Vamos percorrendo os jogadores e somando os pesos até "batermos" no número sorteado
        var currentSum = 0.0
        for ((player, weight) in playersWithWeights) {
            currentSum += weight
            if (randomRoll <= currentSum) {
                return player // É este o marcador do golo!
            }
        }

        // Fallback de segurança (teoricamente nunca chega aqui)
        return team.random()
    }
}