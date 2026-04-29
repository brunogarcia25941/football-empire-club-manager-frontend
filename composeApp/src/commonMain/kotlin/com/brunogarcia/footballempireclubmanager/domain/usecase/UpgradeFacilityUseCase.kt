package com.brunogarcia.footballempireclubmanager.domain.usecase

import com.brunogarcia.footballempireclubmanager.domain.model.Club
import com.brunogarcia.footballempireclubmanager.domain.repository.GameRepository

enum class FacilityType {
    STADIUM,
    TRAINING_CENTER
}

class UpgradeFacilityUseCase(private val repository: GameRepository) {

    // Retorna 'true' se o upgrade foi feito com sucesso, 'false' se não houver dinheiro
    fun execute(facilityType: FacilityType): Boolean {
        val userClubId = repository.getUserClubId()
        val allClubs = repository.getAllClubs().toMutableList()
        val clubIndex = allClubs.indexOfFirst { it.id == userClubId }

        if (clubIndex == -1) return false

        val club = allClubs[clubIndex]
        var success = false

        when (facilityType) {
            FacilityType.STADIUM -> {
                val upgradeCost = calculateStadiumUpgradeCost(club.stadiumCapacity)
                if (club.budget >= upgradeCost) {
                    val updatedClub = club.copy(
                        budget = club.budget - upgradeCost,
                        stadiumCapacity = club.stadiumCapacity + 5000 // Aumenta 5000 lugares
                    )
                    allClubs[clubIndex] = updatedClub
                    success = true
                }
            }
            FacilityType.TRAINING_CENTER -> {
                // Como não temos um "nível" explícito de treino, vamos usar a reputação ou
                // assumir um custo fixo para melhorar a "lealdade/reputação" que afeta o clube.
                // No futuro vamos ter de adicionar 'trainigLevel' ao modelo Club.kt e meter isto a funcionar como deve de ser
                val upgradeCost = 2500000.0 // 2.5 Milhões
                if (club.budget >= upgradeCost) {
                    val updatedClub = club.copy(
                        budget = club.budget - upgradeCost,
                        fanBaseLoyalty = club.fanBaseLoyalty + 5 // Mais lealdade = mais adeptos = mais dinheiro
                    )
                    allClubs[clubIndex] = updatedClub
                    success = true
                }
            }
        }

        if (success) {
            repository.updateClubsAndPlayers(allClubs, repository.getAllPlayers())
            repository.saveGameToDisk() // Guarda logo a compra
        }

        return success
    }

    // O custo aumenta quanto maior for o estádio
    fun calculateStadiumUpgradeCost(currentCapacity: Int): Double {
        return (currentCapacity / 1000) * 500000.0 // Ex: 50.000 lugares = 25 Milhões para expandir
    }
}