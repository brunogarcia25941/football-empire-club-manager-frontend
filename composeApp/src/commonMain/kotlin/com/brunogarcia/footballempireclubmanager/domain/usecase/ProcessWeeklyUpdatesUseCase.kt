package com.brunogarcia.footballempireclubmanager.domain.usecase

import com.brunogarcia.footballempireclubmanager.domain.engine.MatchResult
import com.brunogarcia.footballempireclubmanager.domain.model.Club
import com.brunogarcia.footballempireclubmanager.domain.model.Player
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Caso de Uso responsável por processar todas as atualizações que ocorrem no final de uma semana.
 * Inclui: Recuperação física, Finanças e agora Evolução de Atributos.
 */
class ProcessWeeklyUpdatesUseCase {

    fun execute(
        allClubs: List<Club>,
        allPlayers: List<Player>,
        weeklyResults: List<MatchResult>
    ) {
        // 1. Processar Cansaço e Recuperação (Stamina)
        updateStaminaAndFitness(allClubs, allPlayers, weeklyResults)

        // 2. Processar Evolução de Atributos (Treinos)
        processPlayerEvolution(allClubs, allPlayers)

        // 3. Processar Finanças (Receitas e Despesas)
        updateFinances(allClubs, allPlayers, weeklyResults)
    }

    /**
     * Gere a recuperação de stamina dos jogadores.
     */
    private fun updateStaminaAndFitness(allClubs: List<Club>, allPlayers: List<Player>, weeklyResults: List<MatchResult>) {
        allPlayers.forEach { player ->
            val club = allClubs.find { it.id == player.clubId }
            
            // Bónus de recuperação baseado nas infraestruturas (1 a 10)
            val trainingBonus = club?.trainingFacilities ?: 1
            
            // Base de recuperação semanal
            var recovery = 15 + trainingBonus 
            
            // Se o jogador jogou na jornada passada, perde stamina (cerca de 30-35)
            val playedThisWeek = weeklyResults.any { it.homeClubId == player.clubId || it.awayClubId == player.clubId }
            val loss = if (playedThisWeek) 35 else 0

            player.stamina = max(0, min(100, player.stamina + recovery - loss))
        }
    }

    /**
     * Sistema de Evolução: Jogadores melhoram ou pioram atributos com base na idade e infraestruturas.
     */
    private fun processPlayerEvolution(allClubs: List<Club>, allPlayers: List<Player>) {
        allPlayers.forEach { player ->
            val club = allClubs.find { it.id == player.clubId }
            val facilities = club?.trainingFacilities ?: 1 // 1 a 10
            
            // Probabilidade base de subir um atributo (mais alta para jovens)
            // Jovens (< 21): Alta probabilidade
            // Médios (21-29): Probabilidade moderada
            // Veteranos (> 32): Podem começar a perder atributos físicos
            
            val evolutionRoll = Random.nextInt(1, 101)
            
            when {
                // JOVENS PROMESSAS: Evoluem rápido
                player.age <= 21 -> {
                    if (evolutionRoll <= (15 + facilities)) { // Até 25% de chance com infraestruturas top
                        improveRandomAttribute(player)
                    }
                }
                
                // JOGADORES NO AUGE: Estabilizam ou crescem pouco
                player.age in 22..29 -> {
                    if (evolutionRoll <= (5 + facilities / 2)) { // Até 10% de chance
                        improveRandomAttribute(player)
                    }
                }

                // VETERANOS: Podem perder atributos físicos (Velocidade)
                player.age >= 33 -> {
                    if (evolutionRoll <= 10) { // 10% de chance de declínio
                        declinePhysicalAttributes(player)
                    }
                }
            }
        }
    }

    /**
     * Melhora um atributo aleatório do jogador.
     */
    private fun improveRandomAttribute(player: Player) {
        val attrToImprove = Random.nextInt(1, 11)
        when (attrToImprove) {
            1 -> player.pace = min(99, player.pace + 1)
            2 -> player.strength = min(99, player.strength + 1)
            3 -> player.finishing = min(99, player.finishing + 1)
            4 -> player.passing = min(99, player.passing + 1)
            5 -> player.dribbling = min(99, player.dribbling + 1)
            6 -> player.vision = min(99, player.vision + 1)
            7 -> player.tackling = min(99, player.tackling + 1)
            8 -> player.defensivePositioning = min(99, player.defensivePositioning + 1)
            9 -> player.offensivePositioning = min(99, player.offensivePositioning + 1)
            10 -> player.heading = min(99, player.heading + 1)
        }
        
        // Se for guarda-redes, também pode melhorar reflexos
        if (player.mainPosition.name == "GK") {
            player.gkReflexes = min(99, player.gkReflexes + 1)
        }
    }

    /**
     * Diminui atributos físicos devido à idade.
     */
    private fun declinePhysicalAttributes(player: Player) {
        player.pace = max(30, player.pace - 1)
        player.strength = max(40, player.strength - 1)
        // Mas a visão e o posicionamento podem subir com a experiência!
        if (Random.nextBoolean()) {
            player.vision = min(99, player.vision + 1)
        }
    }

    /**
     * Gere as finanças semanais (Bilheteira, Patrocínios e Salários).
     */
    private fun updateFinances(allClubs: List<Club>, allPlayers: List<Player>, weeklyResults: List<MatchResult>) {
        allClubs.forEach { club ->
            var weeklyIncome = 0.0
            var weeklyExpenses = 0.0

            // A) Receitas de Bilheteira (Se jogou em casa)
            val playedAtHome = weeklyResults.any { it.homeClubId == club.id }
            if (playedAtHome) {
                val attendanceRatio = (club.fanBaseLoyalty + club.reputation) / 200.0
                val attendees = (club.stadiumCapacity * attendanceRatio).toInt()
                weeklyIncome += attendees * club.ticketPrice
            }

            // B) Patrocínios (Baseado na reputação)
            weeklyIncome += club.reputation * 3000.0

            // C) Salários (Baseado no Overall)
            val clubPlayers = allPlayers.filter { it.clubId == club.id }
            clubPlayers.forEach { player ->
                val ovr = player.getEffectiveOverall(player.mainPosition)
                // Formula: Jogador de 80 OVR custa ~64k/semana. Craque de 90 OVR custa ~81k/semana.
                weeklyExpenses += (ovr * ovr) * 10.0 
            }

            club.budget = club.budget + weeklyIncome - weeklyExpenses
        }
    }
}