package com.brunogarcia.footballempireclubmanager.domain.usecase

import com.brunogarcia.footballempireclubmanager.domain.repository.GameRepository

class BuyPlayerUseCase(private val repository: GameRepository) {

    fun execute(playerId: String, price: Double): Boolean {
        val userClubId = repository.getUserClubId()
        val allClubs = repository.getAllClubs().toMutableList()
        val allPlayers = repository.getAllPlayers().toMutableList()

        val buyerIndex = allClubs.indexOfFirst { it.id == userClubId }
        val playerIndex = allPlayers.indexOfFirst { it.id == playerId }

        if (buyerIndex == -1 || playerIndex == -1) return false

        val buyerClub = allClubs[buyerIndex]
        val player = allPlayers[playerIndex]
        val sellerIndex = allClubs.indexOfFirst { it.id == player.clubId }

        // Verifica se tem dinheiro suficiente
        if (buyerClub.budget >= price) {

            // 1. Tira dinheiro ao Comprador
            allClubs[buyerIndex] = buyerClub.copy(budget = buyerClub.budget - price)

            // 2. Dá o dinheiro ao Vendedor
            if (sellerIndex != -1) {
                val sellerClub = allClubs[sellerIndex]
                allClubs[sellerIndex] = sellerClub.copy(budget = sellerClub.budget + price)
            }

            // 3. Transfere o jogador e dá-lhe um contrato de 3 anos
            allPlayers[playerIndex] = player.copy(clubId = userClubId, contractYears = 3, isListed = false)

            // 4. Guarda tudo na Base de Dados (e no disco)
            repository.updateClubsAndPlayers(allClubs, allPlayers)
            repository.saveGameToDisk()

            return true
        }

        return false // Não há dinheiro
    }

    // Fórmula para o preço base (Ex: Overall 80 = 6.4M | Overall 90 = 8.1M)
    fun calculatePlayerValue(overall: Int): Double {
        return (overall * overall * 1000).toDouble()
    }
}