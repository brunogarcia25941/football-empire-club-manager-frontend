package com.brunogarcia.footballempireclubmanager.domain.engine

import com.brunogarcia.footballempireclubmanager.domain.model.Position
import kotlin.random.Random
import kotlin.math.pow
import kotlin.math.max

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
        val result = MatchResult(
            homeClubId = homeClubId,
            awayClubId = awayClubId,
            homeLineup = homeTeam.map { it.player.id },
            awayLineup = awayTeam.map { it.player.id }
        )

        // Estruturas para controlar cartões e jogadores expulsos nesta partida
        val sentOffPlayers = mutableSetOf<String>()
        val yellowCards = mutableMapOf<String, Int>()

        // 2. Loop dos 90 minutos
        for (minute in 1..90) {
            // A) Simulação de Cartões (1.2% de chance de amarelo por minuto, 0.08% de vermelho direto)
            val cardRoll = Random.nextDouble(0.0, 100.0)
            if (cardRoll < 1.28) {
                val isHomeFoul = Random.nextBoolean()
                val activeTeam = if (isHomeFoul) homeTeam else awayTeam
                val activeClubId = if (isHomeFoul) homeClubId else awayClubId

                // Apenas jogadores de campo que ainda não foram expulsos podem receber cartões
                val eligiblePlayers = activeTeam.filter { it.player.id !in sentOffPlayers && it.playingPosition != Position.GK }
                if (eligiblePlayers.isNotEmpty()) {
                    val targetPlayer = eligiblePlayers.random()

                    if (cardRoll < 0.08) {
                        // Vermelho Direto
                        sentOffPlayers.add(targetPlayer.player.id)
                        result.events.add(MatchEvent(minute, MatchEventType.RED_CARD, targetPlayer.player.id, targetPlayer.player.name, activeClubId))
                    } else {
                        // Amarelo
                        val currentYellows = yellowCards.getOrElse(targetPlayer.player.id) { 0 } + 1
                        yellowCards[targetPlayer.player.id] = currentYellows

                        if (currentYellows == 2) {
                            // Segundo amarelo -> Vermelho por acumulação
                            sentOffPlayers.add(targetPlayer.player.id)
                            result.events.add(MatchEvent(minute, MatchEventType.YELLOW_CARD, targetPlayer.player.id, targetPlayer.player.name, activeClubId))
                            result.events.add(MatchEvent(minute, MatchEventType.RED_CARD, targetPlayer.player.id, targetPlayer.player.name, activeClubId))
                        } else {
                            result.events.add(MatchEvent(minute, MatchEventType.YELLOW_CARD, targetPlayer.player.id, targetPlayer.player.name, activeClubId))
                        }
                    }
                }
            }

            // B) Simulação de Lesões (0.08% de chance por minuto de um jogador se lesionar)
            val injuryRoll = Random.nextDouble(0.0, 100.0)
            if (injuryRoll < 0.08) {
                val isHomeInjury = Random.nextBoolean()
                val activeTeam = if (isHomeInjury) homeTeam else awayTeam
                val activeClubId = if (isHomeInjury) homeClubId else awayClubId

                val eligiblePlayers = activeTeam.filter { it.player.id !in sentOffPlayers }
                if (eligiblePlayers.isNotEmpty()) {
                    val targetPlayer = eligiblePlayers.random()
                    // Lesão retira stamina imediatamente (esgota o jogador) e afeta a moral
                    targetPlayer.player.stamina = Random.nextInt(0, 11)
                    targetPlayer.player.morale = max(10, targetPlayer.player.morale - 20)

                    result.events.add(MatchEvent(minute, MatchEventType.INJURY, targetPlayer.player.id, targetPlayer.player.name, activeClubId))
                }
            }

            // C) Simulação de Golos (influenciada pela posse e forças calculadas a cada minuto para refletir expulsões)
            val homeAttack = calculateSectorStrength(homeTeam, sentOffPlayers, isAttack = true)
            val homeMidfield = calculateSectorStrength(homeTeam, sentOffPlayers, isMidfield = true)
            val homeDefense = calculateSectorStrength(homeTeam, sentOffPlayers, isDefense = true)

            val awayAttack = calculateSectorStrength(awayTeam, sentOffPlayers, isAttack = true)
            val awayMidfield = calculateSectorStrength(awayTeam, sentOffPlayers, isMidfield = true)
            val awayDefense = calculateSectorStrength(awayTeam, sentOffPlayers, isDefense = true)

            // Fator casa dá um pequeno bónus ao Meio-Campo (Posse de bola / Moral dos adeptos)
            val homeMidfieldAdvantage = homeMidfield * 1.05

            // Rolar um dado de 1 a 100
            val chance = Random.nextInt(1, 101)

            // Só há ~20% de probabilidade de haver uma jogada de perigo num minuto normal
            if (chance <= 20) {
                // Elevamos o meio-campo ao quadrado para o domínio ser mais realista
                val homeMidPower = homeMidfieldAdvantage.pow(2)
                val awayMidPower = awayMidfield.pow(2)

                val totalMidfieldPower = homeMidPower + awayMidPower
                if (totalMidfieldPower > 0.0) {
                    val homePossessionChance = (homeMidPower / totalMidfieldPower) * 100.0

                    // Quem tem posse de bola
                    val possessionRoll = Random.nextDouble(0.0, 100.0)
                    if (possessionRoll <= homePossessionChance) {
                        // Equipa da Casa Ataca (Só ataca se tiver jogadores ativos em campo)
                        val activeAttackers = homeTeam.filter { it.player.id !in sentOffPlayers }
                        if (activeAttackers.isNotEmpty() && isGoalScored(homeAttack, awayDefense)) {
                            result.homeGoals++
                            val scorer = pickGoalScorer(homeTeam, sentOffPlayers)
                            result.events.add(MatchEvent(minute, MatchEventType.GOAL, scorer.player.id, scorer.player.name, homeClubId))
                        }
                    } else {
                        // Equipa de Fora Ataca (Só ataca se tiver jogadores ativos em campo)
                        val activeAttackers = awayTeam.filter { it.player.id !in sentOffPlayers }
                        if (activeAttackers.isNotEmpty() && isGoalScored(awayAttack, homeDefense)) {
                            result.awayGoals++
                            val scorer = pickGoalScorer(awayTeam, sentOffPlayers)
                            result.events.add(MatchEvent(minute, MatchEventType.GOAL, scorer.player.id, scorer.player.name, awayClubId))
                        }
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

        return Random.nextDouble(0.0, 100.0) < goalProbability
    }

    private fun calculateSectorStrength(
        team: List<StartingPlayer>,
        sentOffPlayerIds: Set<String>,
        isAttack: Boolean = false,
        isMidfield: Boolean = false,
        isDefense: Boolean = false
    ): Double {
        // Filtra apenas jogadores que NÃO foram expulsos
        val filteredPlayers = team.filter {
            it.player.id !in sentOffPlayerIds && when {
                isAttack -> it.playingPosition in listOf(Position.ST, Position.LW, Position.RW)
                isMidfield -> it.playingPosition in listOf(Position.CAM, Position.CM, Position.CDM, Position.LM, Position.RM)
                isDefense -> it.playingPosition in listOf(Position.GK, Position.CB, Position.LB, Position.RB)
                else -> false
            }
        }

        if (filteredPlayers.isEmpty()) return 1.0 // Prevenir divisões por zero se faltarem jogadores no setor

        // Faz a média do Overall Efetivo dos jogadores desse setor
        val sumOverall = filteredPlayers.sumOf { it.player.getEffectiveOverall(it.playingPosition) }
        return sumOverall.toDouble() / filteredPlayers.size
    }

    private fun pickGoalScorer(team: List<StartingPlayer>, sentOffPlayerIds: Set<String>): StartingPlayer {
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

        // 2. Associamos a cada jogador ativo no campo o peso da posição onde está a jogar
        val eligibleTeam = team.filter { it.player.id !in sentOffPlayerIds }
        val playersWithWeights = eligibleTeam.map { startingPlayer ->
            val weight = positionWeights[startingPlayer.playingPosition] ?: 1.0
            Pair(startingPlayer, weight)
        }

        // 3. Somamos todos os pesos
        val totalWeight = playersWithWeights.sumOf { it.second }

        // Se a soma dos pesos for zero (ex: equipa só tem guarda-redes em campo)
        if (totalWeight <= 0.0) {
            return if (eligibleTeam.isNotEmpty()) eligibleTeam.random() else team.random()
        }

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

        // Fallback de segurança
        return eligibleTeam.random()
    }
}