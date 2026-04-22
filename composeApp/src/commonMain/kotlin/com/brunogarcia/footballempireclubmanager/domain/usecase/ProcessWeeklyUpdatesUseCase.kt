package com.brunogarcia.footballempireclubmanager.domain.usecase

import com.brunogarcia.footballempireclubmanager.domain.engine.MatchResult
import com.brunogarcia.footballempireclubmanager.domain.model.Club
import com.brunogarcia.footballempireclubmanager.domain.model.Player
import kotlin.math.max
import kotlin.math.min

class ProcessWeeklyUpdatesUseCase {

    fun execute(
        allClubs: List<Club>,
        allPlayers: List<Player>,
        weeklyResults: List<MatchResult>
    ) {
        // 1. Processar Cansaço (Stamina)
        updateStamina(allPlayers, weeklyResults)

        // 2. Processar Finanças (Bilhetes e Salários)
        updateFinances(allClubs, allPlayers, weeklyResults)
    }

    private fun updateStamina(allPlayers: List<Player>, weeklyResults: List<MatchResult>) {
        // Criar uma lista de todos os IDs de jogadores que participaram em golos ou eventos
        // (No futuro, o MatchEngine deve devolver exatamente quem jogou para sermos mais precisos,
        // mas para já usamos uma estimativa baseada no cansaço semanal)

        // Simulação Tycoon Padrão:
        // Todos perdem 15 de stamina por ser uma semana de treinos.
        // Quem recuperar infraestruturas de treino recupera mais.

        allPlayers.forEach { player ->
            // Assume que todos recuperam um valor base com o descanso/treino (ex: +20)
            // Se o clube tem boas infraestruturas, recupera mais rápido!
            val club = allPlayers.find { it.id == player.id }?.clubId // Simplificação
            val trainingBonus = 5 // Isto viria do club.trainingFacilities

            var newStamina = player.stamina + 15 + trainingBonus

            // Mas se ele for convocado/jogar, perde stamina!
            // (Assumimos que perdeu 30 se jogou os 90 minutos)
            // Vamos subtrair de forma genérica para já:
            newStamina -= 25

            // Garantir que a stamina fica entre 0 e 100
            player.stamina = max(0, min(100, newStamina))
        }
    }

    private fun updateFinances(allClubs: List<Club>, allPlayers: List<Player>, weeklyResults: List<MatchResult>) {
        allClubs.forEach { club ->
            var weeklyIncome = 0.0
            var weeklyExpenses = 0.0

            // A) Receitas de Bilheteira (Apenas se a equipa jogou em Casa esta semana)
            val playedAtHome = weeklyResults.any { it.homeClubId == club.id }
            if (playedAtHome) {
                // A percentagem do estádio cheio depende da lealdade e reputação
                val attendancePercentage = (club.fanBaseLoyalty + club.reputation) / 200.0 // ex: 0.85 (85%)
                val attendees = (club.stadiumCapacity * attendancePercentage).toInt()

                val matchIncome = attendees * club.ticketPrice
                weeklyIncome += matchIncome
            }

            // B) Receitas de Patrocínios semanais (Fixo para já)
            val sponsorshipIncome = club.reputation * 5000.0 // Equipas com 80 de rep ganham 400k/semana
            weeklyIncome += sponsorshipIncome

            // C) Despesas de Salários (Baseado na qualidade do jogador)
            val clubPlayers = allPlayers.filter { it.clubId == club.id }
            clubPlayers.forEach { player ->
                // Um jogador com muito overall exige mais salário.
                // Formula simples: Overall efetivo * 150€ por semana.
                val estimatedOverall = player.getEffectiveOverall(player.mainPosition)
                val weeklyWage = estimatedOverall * estimatedOverall * 10.0 // Formula exponencial para craques!
                weeklyExpenses += weeklyWage
            }

            // D) Atualizar o orçamento do Clube
            club.budget = club.budget + weeklyIncome - weeklyExpenses
        }
    }
}