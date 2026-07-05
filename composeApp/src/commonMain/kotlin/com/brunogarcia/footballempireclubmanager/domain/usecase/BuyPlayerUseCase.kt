package com.brunogarcia.footballempireclubmanager.domain.usecase

import com.brunogarcia.footballempireclubmanager.domain.model.Player
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

            // 3. Transfere o jogador, dá-lhe um contrato de 3 anos e define a semana de transferência
            allPlayers[playerIndex] = player.copy(
                clubId = userClubId, 
                contractYears = 3, 
                isListed = false, 
                lastTransferWeek = repository.getCurrentWeek()
            )

            // Registar no Histórico de Transferências da Liga
            val sellerClubName = if (player.clubId.isEmpty()) "Agente Livre" else allClubs.find { it.id == player.clubId }?.name ?: "Desconhecido"
            val transferEvent = com.brunogarcia.footballempireclubmanager.domain.model.TransferEvent(
                week = repository.getCurrentWeek(),
                playerName = player.name,
                playerPosition = player.mainPosition.name,
                overall = player.getBaseOverall(player.mainPosition),
                fromClubName = sellerClubName,
                toClubName = buyerClub.name,
                fee = price
            )
            val currentHistory = repository.getTransferHistory().toMutableList()
            currentHistory.add(transferEvent)
            repository.saveTransferHistory(currentHistory)

            // 4. Guarda tudo na Base de Dados (e no disco)
            repository.updateClubsAndPlayers(allClubs, allPlayers)
            repository.saveGameToDisk()

            return true
        }

        return false // Não há dinheiro
    }

    // Fórmula para obter o preço dinâmico e realista do jogador
    fun calculatePlayerValue(player: Player): Double {
        return player.getMarketValue()
    }
}