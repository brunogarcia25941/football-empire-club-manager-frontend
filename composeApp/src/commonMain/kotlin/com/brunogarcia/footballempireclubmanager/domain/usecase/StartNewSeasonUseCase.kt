package com.brunogarcia.footballempireclubmanager.domain.usecase

import com.brunogarcia.footballempireclubmanager.domain.repository.GameRepository

/**
 * Caso de Uso responsável por processar a transição entre duas épocas desportivas.
 * Envolve: atribuição de prémios de classificação, envelhecimento dos jogadores (+1 ano),
 * reset da tabela classificativa e geração de um novo calendário (fixtures).
 */
class StartNewSeasonUseCase(
    private val repository: GameRepository,
    private val generateFixturesUseCase: GenerateFixturesUseCase,
    private val calculateLeagueTableUseCase: CalculateLeagueTableUseCase
) {

    /**
     * Executa a transição de época e devolve o prémio ganho pelo clube do utilizador.
     */
    fun execute(): Double {
        val userClubId = repository.getUserClubId()
        val allClubs = repository.getAllClubs()
        val matchHistory = repository.getMatchHistory()

        // 1. Calcular a classificação final da tabela para determinar a posição de cada clube
        val finalTable = calculateLeagueTableUseCase.execute(allClubs, matchHistory)
        val userPositionIndex = finalTable.indexOfFirst { it.clubId == userClubId }
        val userPosition = if (userPositionIndex != -1) userPositionIndex + 1 else 10

        // 2. Determinar o prémio monetário para cada clube com base na sua posição final
        // 1º lugar: 10M €, 2º: 8M €, 3º: 6M €, 4º: 5M €, 5º: 4M €, 6º em diante: 2M €
        fun calculatePrize(position: Int): Double {
            return when (position) {
                1 -> 10000000.0
                2 -> 8000000.0
                3 -> 6000000.0
                4 -> 5000000.0
                5 -> 4000000.0
                else -> 2000000.0
            }
        }

        val userPrize = calculatePrize(userPosition)

        // 3. Atualizar o saldo de todos os clubes com os respetivos prémios
        val updatedClubs = allClubs.map { club ->
            val posIndex = finalTable.indexOfFirst { it.clubId == club.id }
            val pos = if (posIndex != -1) posIndex + 1 else 10
            val prize = calculatePrize(pos)
            club.copy(budget = club.budget + prize)
        }

        // 4. Processar contratos e envelhecimento (+1 ano) com férias (stamina 100%)
        val basePlayersList = repository.getAllPlayers().filterNot { it.clubId == "YOUTH_$userClubId" }

        val updatedPlayers = basePlayersList.map { player ->
            val newAge = player.age + 1
            var newContractYears = player.contractYears - 1
            var newClubId = player.clubId
            var newIsListed = player.isListed

            if (newContractYears <= 0) {
                // Fim de contrato!
                if (player.clubId == userClubId) {
                    // Jogador do utilizador é libertado a custo zero (fica livre / clubId vazio)
                    newClubId = ""
                    newIsListed = false
                    newContractYears = 1 // Contrato padrão para jogador livre
                } else if (player.clubId.isNotEmpty()) {
                    // Clubes IA renovam se for jovem/auge (< 33 anos), libertam se for veterano (>= 33 anos)
                    if (player.age < 33) {
                        newContractYears = 2 // Renova por mais 2 anos
                    } else {
                        newClubId = "" // Libertado (fica livre)
                        newIsListed = false
                        newContractYears = 1
                    }
                }
            }

            player.copy(
                age = newAge,
                stamina = 100,
                morale = 80,
                contractYears = newContractYears,
                clubId = newClubId,
                isListed = newIsListed,
                transferOffer = null, // Limpa propostas pendentes do ano anterior
                offerClubName = null
            )
        }.toMutableList()

        // 4b. Gerar novos jovens da Academia (Juniores) no final da época
        val userClub = updatedClubs.find { it.id == userClubId }
        val academyLevel = userClub?.youthAcademyLevel ?: 1
        for (i in 1..3) {
            val youthPlayer = com.brunogarcia.footballempireclubmanager.domain.engine.YouthGenerator.generateYouthPlayer(userClubId, academyLevel)
            updatedPlayers.add(youthPlayer)
        }

        // 5. Reinicializar o jogo em memória com os dados limpos (mantendo clubes e jogadores atualizados)
        // Isso limpa o matchHistory, o 11 inicial guardado e reseta a semana para 1.
        repository.initializeGame(updatedClubs, updatedPlayers, userClubId)

        // 6. Gerar e salvar o calendário da nova época (novos confrontos alternando ordens)
        val newFixtures = generateFixturesUseCase.execute(updatedClubs)
        repository.saveFixtures(newFixtures)

        // 7. Gravar o novo estado do jogo no disco
        repository.saveGameToDisk()

        return userPrize
    }
}
