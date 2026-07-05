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

        // 1. Calcular classificações para ambas as divisões
        val tableDiv1 = calculateLeagueTableUseCase.execute(allClubs, matchHistory, 1)
        val tableDiv2 = calculateLeagueTableUseCase.execute(allClubs, matchHistory, 2)

        val userClub = allClubs.find { it.id == userClubId }
        val userDiv = userClub?.divisionLevel ?: 1
        val userTable = if (userDiv == 1) tableDiv1 else tableDiv2
        val userPositionIndex = userTable.indexOfFirst { it.clubId == userClubId }
        val userPosition = if (userPositionIndex != -1) userPositionIndex + 1 else 10

        // 2. Determinar o prémio monetário com base na divisão e posição final
        fun calculatePrize(position: Int, division: Int): Double {
            return if (division == 1) {
                when (position) {
                    1 -> 10000000.0
                    2 -> 8000000.0
                    3 -> 6000000.0
                    4 -> 5000000.0
                    5 -> 4000000.0
                    else -> 2000000.0
                }
            } else {
                when (position) {
                    1 -> 5000000.0
                    2 -> 4000000.0
                    3 -> 3000000.0
                    4 -> 2000000.0
                    5 -> 1500000.0
                    else -> 1000000.0
                }
            }
        }

        val userPrize = calculatePrize(userPosition, userDiv)

        // 3. Processar Promoção e Despromoção (Subidas/Descidas)
        // Top 2 da 2ª Divisão sobem para a 1ª
        val promotedClubIds = if (tableDiv2.size >= 2) listOf(tableDiv2[0].clubId, tableDiv2[1].clubId) else emptyList()
        // Bottom 2 da 1ª Divisão descem para a 2ª
        val relegatedClubIds = if (tableDiv1.size >= 2) listOf(tableDiv1[tableDiv1.size - 1].clubId, tableDiv1[tableDiv1.size - 2].clubId) else emptyList()

        // Atualizar saldo e divisões de todos os clubes
        val updatedClubs = allClubs.map { club ->
            var newDiv = club.divisionLevel
            if (club.id in promotedClubIds) {
                newDiv = 1
            } else if (club.id in relegatedClubIds) {
                newDiv = 2
            }

            val posIndex = if (club.divisionLevel == 1) {
                tableDiv1.indexOfFirst { it.clubId == club.id }
            } else {
                tableDiv2.indexOfFirst { it.clubId == club.id }
            }
            val pos = if (posIndex != -1) posIndex + 1 else 10
            val prize = calculatePrize(pos, club.divisionLevel)

            club.copy(
                divisionLevel = newDiv,
                budget = club.budget + prize
            )
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
                offerClubName = null,
                seasonMatches = 0, // Reinicia os jogos jogados na nova época
                lastTransferWeek = -1
            )
        }.toMutableList()

        // 4b. Gerar novos jovens da Academia (Juniores) no final da época para todos os clubes
        for (club in updatedClubs) {
            val academyLevel = club.youthAcademyLevel
            val numYouth = if (club.id == userClubId) 3 else 2 // Utilizador gera 3, IA gera 2 para equilíbrio
            for (i in 1..numYouth) {
                val youthPlayer = com.brunogarcia.footballempireclubmanager.domain.engine.YouthGenerator.generateYouthPlayer(club.id, academyLevel)
                val finalPlayer = if (club.id == userClubId) {
                    // Para o utilizador, inicia na academia (com o prefixo YOUTH_ no clubId)
                    youthPlayer
                } else {
                    // Para a IA, é promovido diretamente ao plantel profissional (com contrato padrão de 3 anos)
                    youthPlayer.copy(clubId = club.id, contractYears = 3)
                }
                updatedPlayers.add(finalPlayer)
            }
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
