package com.brunogarcia.footballempireclubmanager.domain.usecase

import com.brunogarcia.footballempireclubmanager.domain.engine.MatchResult
import com.brunogarcia.footballempireclubmanager.domain.model.Club
import com.brunogarcia.footballempireclubmanager.domain.model.Player
import com.brunogarcia.footballempireclubmanager.domain.model.Position
import com.brunogarcia.footballempireclubmanager.domain.repository.GameRepository
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Caso de Uso responsável por processar todas as atualizações que ocorrem no final de uma semana.
 * Inclui: Recuperação física, Finanças, Evolução Dinâmica de Atributos e Propostas.
 */
class ProcessWeeklyUpdatesUseCase(private val repository: GameRepository) {

    fun execute(
        allClubs: List<Club>,
        allPlayers: List<Player>,
        weeklyResults: List<MatchResult>,
        userClubId: String,
        currentWeek: Int
    ) {
        // 1. Processar Cansaço, Recuperação (Stamina) e incrementar jogos efetuados
        updateStaminaAndFitness(allClubs, allPlayers, weeklyResults)

        // 2. Processar Evolução de Atributos (Treinos dinâmicos com idade, moral e utilização)
        processPlayerEvolution(allClubs, allPlayers, currentWeek)

        // 3. Simular propostas de clubes IA para jogadores do utilizador
        simulateAIOffers(allClubs, allPlayers, userClubId)

        // 3b. Simular transferências e contratações automáticas entre clubes da IA (e agentes livres)
        simulateAIToAITransfers(allClubs, allPlayers, userClubId)

        // 4. Processar Finanças (Receitas e Despesas)
        updateFinances(allClubs, allPlayers, weeklyResults, currentWeek)
    }

    /**
     * Gere a recuperação de stamina dos jogadores e atualiza os jogos efetuados na época.
     */
    private fun updateStaminaAndFitness(allClubs: List<Club>, allPlayers: List<Player>, weeklyResults: List<MatchResult>) {
        allPlayers.forEach { player ->
            if (player.clubId.startsWith("YOUTH_")) return@forEach
            val club = allClubs.find { it.id == player.clubId }
            
            // Bónus de recuperação baseado nas infraestruturas (1 a 10)
            val trainingBonus = club?.trainingFacilities ?: 1
            
            // Base de recuperação semanal
            val recovery = 15 + trainingBonus 
            
            // Se o jogador jogou na jornada passada (foi titular em qualquer jogo), perde stamina (cerca de 30-35)
            val playedThisWeek = weeklyResults.any { 
                it.homeLineup.contains(player.id) || it.awayLineup.contains(player.id) 
            }
            val loss = if (playedThisWeek) 35 else 0

            // Se jogou esta semana, incrementamos o registo de jogos na época
            if (playedThisWeek) {
                player.seasonMatches++
            }

            player.stamina = max(0, min(100, player.stamina + recovery - loss))
        }
    }

    /**
     * Sistema de Evolução Dinâmico:
     * Jogadores melhoram ou pioram atributos com base na idade, tempo de jogo (seasonMatches/currentWeek) e moral.
     */
    private fun processPlayerEvolution(allClubs: List<Club>, allPlayers: List<Player>, currentWeek: Int) {
        // Evitamos divisão por zero se a semana for inválida
        val activeWeek = max(1, currentWeek)

        allPlayers.forEach { player ->
            if (player.clubId.startsWith("YOUTH_")) return@forEach
            val club = allClubs.find { it.id == player.clubId }
            val facilities = club?.trainingFacilities ?: 1 // 1 a 10
            
            // Taxa de utilização na época (0.0 a 1.0)
            val playRatio = player.seasonMatches.toDouble() / activeWeek.toDouble()

            // Fator Moral (Influência do ânimo no treino: 0 morale = 0.8x | 100 morale = 1.2x)
            val moraleFactor = player.morale.toDouble() / 100.0
            val moraleMultiplier = 0.8 + 0.4 * moraleFactor

            val evolutionRoll = Random.nextInt(1, 101)

            when {
                // 1. JOVENS PROMESSAS (16 a 21 anos) - Foco no Crescimento
                player.age <= 21 -> {
                    // Se jogar muito, cresce exponencialmente. Se não jogar, cresce a ritmo lento.
                    val playBonus = (playRatio * 15.0).toInt()
                    val baseChance = 10 + playBonus + facilities
                    val finalChance = (baseChance * moraleMultiplier).toInt()

                    if (evolutionRoll <= finalChance) {
                        improvePositionAttribute(player)
                    }
                }
                
                // 2. JOGADORES NO AUGE (22 a 29 anos) - Foco na Consolidação
                player.age in 22..29 -> {
                    // Jogar regularmente mantém a evolução estável de atributos técnicos/mentais
                    val playBonus = (playRatio * 5.0).toInt()
                    val baseChance = 3 + playBonus + (facilities / 2)
                    val finalChance = (baseChance * moraleMultiplier).toInt()

                    if (evolutionRoll <= finalChance) {
                        improvePositionAttribute(player)
                    }
                }

                // 3. TRANSIÇÃO / MADUROS (30 a 32 anos) - A antiga lacuna resolvida
                player.age in 30..32 -> {
                    // Se for titular regular (joga em >= 50% dos jogos), mantém a forma física e pode evoluir atributos mentais.
                    if (playRatio >= 0.5) {
                        // 5% de chance de melhorar atributos mentais/técnicos por experiência
                        val experienceChance = (5 * moraleMultiplier).toInt()
                        if (evolutionRoll <= experienceChance) {
                            improveMentalAttribute(player)
                        }
                    } else if (playRatio < 0.2) {
                        // Se quase não joga (menos de 20% dos jogos), perde ritmo físico por falta de minutos
                        // Inversamente proporcional à moral (desmotivação acelera a perda)
                        val declineChance = (10 * (1.5 - moraleFactor)).toInt()
                        if (evolutionRoll <= declineChance) {
                            declinePhysicalAttributes(player)
                        }
                    }
                }

                // 4. VETERANOS (>= 33 anos) - Foco em Mitigar o Declínio
                player.age >= 33 -> {
                    // Jogar reduz o declínio físico (manutenção física). Não jogar acelera.
                    val baseDeclineChance = 25
                    val playMitigation = (playRatio * 15.0).toInt()
                    val declineChance = (baseDeclineChance - playMitigation).coerceAtLeast(5)
                    val finalDeclineChance = (declineChance * (1.5 - moraleFactor)).toInt()

                    if (evolutionRoll <= finalDeclineChance) {
                        declinePhysicalAttributes(player)
                    }

                    // Veteranos regulares também ganham experiência mental em jogo
                    if (playRatio >= 0.4) {
                        val experienceChance = (5 * moraleMultiplier).toInt()
                        val experienceRoll = Random.nextInt(1, 101)
                        if (experienceRoll <= experienceChance) {
                            improveMentalAttribute(player)
                        }
                    }
                }
            }
        }
    }

    /**
     * Melhora um atributo de campo de forma ponderada pela posição do jogador.
     * Dá 75% de probabilidade de treinar um atributo chave da posição e 25% de ser qualquer outro atributo geral.
     */
    private fun improvePositionAttribute(player: Player) {
        val attrCandidates = when (player.mainPosition) {
            Position.GK -> listOf("gkReflexes", "gkHandling", "gkAgility", "strength", "passing")
            Position.CB -> listOf("tackling", "defensivePositioning", "heading", "strength", "passing")
            Position.LB, Position.RB -> listOf("pace", "tackling", "passing", "defensivePositioning", "strength")
            Position.CDM -> listOf("tackling", "defensivePositioning", "passing", "strength", "vision")
            Position.CM -> listOf("passing", "vision", "dribbling", "strength", "tackling")
            Position.CAM -> listOf("passing", "vision", "dribbling", "finishing", "offensivePositioning")
            Position.LM, Position.RM, Position.LW, Position.RW -> 
                listOf("pace", "dribbling", "passing", "finishing", "offensivePositioning")
            Position.ST -> listOf("finishing", "offensivePositioning", "pace", "strength", "heading")
        }

        val selectedAttr = if (Random.nextInt(1, 101) <= 75) {
            attrCandidates.random()
        } else {
            listOf(
                "pace", "strength", "tackling", "defensivePositioning", 
                "passing", "vision", "dribbling", "offensivePositioning", 
                "finishing", "heading", "gkReflexes", "gkHandling", "gkAgility"
            ).random()
        }
        applyAttributeIncrement(player, selectedAttr)
    }

    /**
     * Melhora apenas atributos de natureza mental ou técnica (experiência).
     */
    private fun improveMentalAttribute(player: Player) {
        val mentalAttrs = listOf("vision", "defensivePositioning", "offensivePositioning", "passing")
        applyAttributeIncrement(player, mentalAttrs.random())
    }

    /**
     * Aplica o incremento seguro (+1 até ao limite de 99) a um determinado atributo.
     */
    private fun applyAttributeIncrement(player: Player, attributeName: String) {
        when (attributeName) {
            "pace" -> player.pace = min(99, player.pace + 1)
            "strength" -> player.strength = min(99, player.strength + 1)
            "tackling" -> player.tackling = min(99, player.tackling + 1)
            "defensivePositioning" -> player.defensivePositioning = min(99, player.defensivePositioning + 1)
            "vision" -> player.vision = min(99, player.vision + 1)
            "passing" -> player.passing = min(99, player.passing + 1)
            "dribbling" -> player.dribbling = min(99, player.dribbling + 1)
            "offensivePositioning" -> player.offensivePositioning = min(99, player.offensivePositioning + 1)
            "finishing" -> player.finishing = min(99, player.finishing + 1)
            "heading" -> player.heading = min(99, player.heading + 1)
            "gkReflexes" -> player.gkReflexes = min(99, player.gkReflexes + 1)
            "gkHandling" -> player.gkHandling = min(99, player.gkHandling + 1)
            "gkAgility" -> player.gkAgility = min(99, player.gkAgility + 1)
        }
    }

    /**
     * Diminui atributos físicos devido à inatividade ou envelhecimento natural.
     */
    private fun declinePhysicalAttributes(player: Player) {
        player.pace = max(30, player.pace - 1)
        player.strength = max(40, player.strength - 1)
    }

    /**
     * Gere as finanças semanais (Bilheteira, Patrocínios, TV Rights) e salários mensais.
     */
    private fun updateFinances(
        allClubs: List<Club>, 
        allPlayers: List<Player>, 
        weeklyResults: List<MatchResult>, 
        currentWeek: Int
    ) {
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

            // B) Patrocínios e Direitos de Transmissão de TV
            // Sponsorship Semanal: Baseado na reputação
            val weeklySponsor = club.reputation * 2000.0
            
            // Direitos de TV Semanal: Base fixa por divisão + parte variável baseada na reputação
            val tvBase = if (club.divisionLevel == 1) 100000.0 else 50000.0
            val tvVariable = club.reputation * 1500.0
            
            weeklyIncome += weeklySponsor + tvBase + tvVariable

            // C) Salários (Pagamento Mensal: ocorre a cada 4 semanas, ex: semana 4, 8, 12, 16...)
            val isPayWeek = (currentWeek % 4 == 0)
            if (isPayWeek) {
                val clubPlayers = allPlayers.filter { it.clubId == club.id }
                clubPlayers.forEach { player ->
                    val ovr = player.getBaseOverall(player.mainPosition)
                    // Fórmula Mensal: OVR * OVR * 12.0
                    weeklyExpenses += (ovr * ovr) * 12.0
                }
            }

            club.budget = club.budget + weeklyIncome - weeklyExpenses
        }
    }

    /**
     * Simula propostas de transferência vindas de clubes controlados por IA.
     */
    private fun simulateAIOffers(allClubs: List<Club>, allPlayers: List<Player>, userClubId: String) {
        val userPlayers = allPlayers.filter { it.clubId == userClubId }
        val aiClubs = allClubs.filter { it.id != userClubId }

        if (aiClubs.isEmpty()) return

        userPlayers.forEach { player ->
            // Se o jogador já tem uma proposta ativa, há 15% de chance de expirar por falta de resposta
            if (player.transferOffer != null) {
                if (Random.nextInt(1, 101) <= 15) {
                    player.transferOffer = null
                    player.offerClubName = null
                }
                return@forEach
            }

            // Chance de receber proposta: 25% se listado, 3% se não listado (proposta não solicitada)
            val offerChance = if (player.isListed) 25 else 3
            if (Random.nextInt(1, 101) <= offerChance) {
                val biddingClub = aiClubs.random()
                
                // Valor de mercado base vindo diretamente do objeto do jogador (com overall e idade)
                val baseValue = player.getMarketValue()
                
                // A proposta varia entre 80% e 125% do valor base.
                // Se listado, a IA tenta comprar ligeiramente mais barato.
                val multiplier = if (player.isListed) {
                    Random.nextDouble(0.80, 1.10)
                } else {
                    Random.nextDouble(0.95, 1.25)
                }
                
                player.transferOffer = baseValue * multiplier
                player.offerClubName = biddingClub.name
            }
        }
    }

    // --- TRANSFERÊNCIAS DINÂMICAS ENTRE CLUBES IA ---

    /**
     * Enumeração para agrupar as posições de jogo em setores específicos mais detalhados.
     * Isto garante que cada posição tenha alternativas suficientes e que laterais
     * (direito/esquerdo) e médios sejam avaliados separadamente.
     */
    private enum class PositionGroup {
        GK,            // Guarda-redes
        CB,            // Defesas Centrais
        LB,            // Laterais Esquerdos
        RB,            // Laterais Direitos
        CDM,           // Médios Defensivos (Trinco)
        CM,            // Médios Centro
        CAM,           // Médios Centro Ofensivos (Nº 10)
        WINGER_LEFT,   // Extremos/Alas Esquerdos (LM, LW)
        WINGER_RIGHT,  // Extremos/Alas Direitos (RM, RW)
        STRIKER        // Avançados (ST)
    }

    /**
     * Mapeia uma Position específica para o seu respetivo PositionGroup.
     */
    private fun getPositionGroup(position: Position): PositionGroup {
        return when (position) {
            Position.GK -> PositionGroup.GK
            Position.CB -> PositionGroup.CB
            Position.LB -> PositionGroup.LB
            Position.RB -> PositionGroup.RB
            Position.CDM -> PositionGroup.CDM
            Position.CM -> PositionGroup.CM
            Position.CAM -> PositionGroup.CAM
            Position.LM, Position.LW -> PositionGroup.WINGER_LEFT
            Position.RM, Position.RW -> PositionGroup.WINGER_RIGHT
            Position.ST -> PositionGroup.STRIKER
        }
    }

    /**
     * Define o número mínimo ideal de jogadores que um clube IA deve ter por grupo de posição
     * para manter um plantel equilibrado de 22 jogadores (dois por posição, e 4 para defesas centrais).
     */
    private fun getIdealCount(group: PositionGroup): Int {
        return when (group) {
            PositionGroup.GK -> 2
            PositionGroup.CB -> 4           // Central: jogam dois de cada vez, precisamos de 4.
            PositionGroup.LB -> 2           // Lateral Esquerdo: 2 jogadores (titular + suplente)
            PositionGroup.RB -> 2           // Lateral Direito: 2 jogadores (titular + suplente)
            PositionGroup.CDM -> 2
            PositionGroup.CM -> 2
            PositionGroup.CAM -> 2
            PositionGroup.WINGER_LEFT -> 2
            PositionGroup.WINGER_RIGHT -> 2
            PositionGroup.STRIKER -> 2
        }
    }

    /**
     * Simula contratações e transferências autónomas e lógicas entre clubes controlados pela IA.
     * Os clubes analisam os seus plantéis, identificam carências por posição e contratam
     * jogadores listados, não listados (com excedente no vendedor) ou agentes livres.
     */
    private fun simulateAIToAITransfers(allClubs: List<Club>, allPlayers: List<Player>, userClubId: String) {
        // 0. Atualizar a lista de transferências dos clubes da IA de forma lógica (listando excedentes e retirando défices)
        updateAITransferList(allClubs, allPlayers, userClubId)

        val aiClubs = allClubs.filter { it.id != userClubId }
        if (aiClubs.isEmpty()) return

        aiClubs.forEach { buyerClub ->
            // Se o clube comprador já atingiu o limite de 5 contratações nesta época, não contrata mais
            val buyerIncomingCount = repository.getTransferHistory().count { it.toClubName == buyerClub.name }
            if (buyerIncomingCount >= 5) return@forEach

            // 1. Obter e agrupar os jogadores atuais do clube comprador
            val buyerPlayers = allPlayers.filter { it.clubId == buyerClub.id }
            if (buyerPlayers.isEmpty()) return@forEach
            
            // Contar quantos jogadores o comprador tem em cada grupo de posição
            val counts = PositionGroup.values().associateWith { group ->
                buyerPlayers.count { getPositionGroup(it.mainPosition) == group }
            }

            // Identificar as posições em défice real (menos que ideal)
            val deficits = counts.mapNotNull { (group, count) ->
                val ideal = getIdealCount(group)
                if (count < ideal) {
                    group to (ideal - count)
                } else {
                    null
                }
            }.sortedByDescending { it.second } // Ordena pelo maior défice primeiro

            val targetGroup: PositionGroup
            val isUpgradeSearch: Boolean

            if (deficits.isNotEmpty()) {
                // Cada clube da IA tem 40% de probabilidade de tentar suprir um défice semanalmente
                if (Random.nextInt(1, 101) > 40) return@forEach
                targetGroup = deficits.first().first
                isUpgradeSearch = false
            } else {
                // Se não há défice, o clube decide se quer fazer um Upgrade (melhorar a equipa)
                // Há 25% de probabilidade semanal de tentar fazer um upgrade para dar movimento ao mercado
                if (Random.nextInt(1, 101) > 25) return@forEach
                
                // Escolhe a posição mais fraca do clube (com base na média dos jogadores atuais)
                val groupAverages = PositionGroup.values().associateWith { group ->
                    val groupPlayers = buyerPlayers.filter { getPositionGroup(it.mainPosition) == group }
                    if (groupPlayers.isNotEmpty()) {
                        groupPlayers.map { it.getBaseOverall(it.mainPosition) }.average()
                    } else {
                        0.0
                    }
                }
                
                // A posição com menor média é a mais fraca
                val weakestGroup = groupAverages.minByOrNull { it.value }?.key ?: PositionGroup.values().random()
                
                // 30% de chance de ser um upgrade oportunista (posição aleatória), caso contrário atualiza a posição mais fraca
                targetGroup = if (Random.nextInt(1, 101) <= 30) {
                    PositionGroup.values().random()
                } else {
                    weakestGroup
                }
                isUpgradeSearch = true
            }

            // 2. Procurar candidatos disponíveis no mercado para essa posição
            val candidates = allPlayers.filter { player ->
                // Não pode ser do próprio clube comprador
                player.clubId != buyerClub.id &&
                // Não pode ser do clube do utilizador
                player.clubId != userClubId &&
                // Não pode ser um junior da academia que ainda não foi promovido
                !player.clubId.startsWith("YOUTH_") &&
                // O jogador não pode ter sido transferido esta época
                player.lastTransferWeek == -1 &&
                // Tem de pertencer ao grupo de posição desejado
                getPositionGroup(player.mainPosition) == targetGroup
            }.filter { player ->
                // Regras de disponibilidade para venda lógica:
                if (player.clubId.isEmpty()) {
                    // Agente Livre: sempre disponível para contratação
                    true
                } else {
                    // Jogador de outro clube da IA:
                    val sellerClub = allClubs.find { it.id == player.clubId }
                    val sellerOutgoingCount = if (sellerClub != null) {
                        repository.getTransferHistory().count { it.fromClubName == sellerClub.name }
                    } else {
                        0
                    }
                    
                    // Se o clube vendedor já atingiu o limite de 5 vendas nesta época, não vende
                    if (sellerOutgoingCount >= 5) {
                        false
                    } else if (player.isListed) {
                        // Se está na lista de transferências, está disponível
                        true
                    } else {
                        // Se não está listado, o vendedor aceita vender se tiver excedente
                        val sellerPlayers = allPlayers.filter { it.clubId == player.clubId }
                        val sellerCount = sellerPlayers.count { getPositionGroup(it.mainPosition) == targetGroup }
                        val sellerIdeal = getIdealCount(targetGroup)
                        
                        if (sellerCount > sellerIdeal) {
                            true
                        } else {
                            // 25% de chance de aceitar vender mesmo sendo essencial (titular/sem excedente)
                            // Isto simula propostas irrecusáveis. O clube vendedor ficará com défice e terá de contratar depois.
                            Random.nextInt(1, 101) <= 25
                        }
                    }
                }
            }

            if (candidates.isEmpty()) return@forEach

            // 3. Filtrar candidatos pelo orçamento do clube comprador (máximo 75% do orçamento num único jogador)
            val maxSpend = buyerClub.budget * 0.75
            val affordableCandidates = candidates.map { player ->
                val price = if (player.clubId.isEmpty()) {
                    0.0
                } else {
                    val baseValue = player.getMarketValue()
                    baseValue * Random.nextDouble(0.90, 1.10)
                }
                player to price
            }.filter { it.second <= maxSpend }

            if (affordableCandidates.isEmpty()) return@forEach

            // 4. Se for procura de upgrade, o candidato tem de ser superior ou próximo à média atual da posição no clube comprador
            val finalCandidates = if (isUpgradeSearch) {
                val currentAvg = buyerPlayers.filter { getPositionGroup(it.mainPosition) == targetGroup }
                    .map { it.getBaseOverall(it.mainPosition) }
                    .average()
                affordableCandidates.filter { it.first.getBaseOverall(it.first.mainPosition) >= currentAvg - 1 }
            } else {
                affordableCandidates
            }

            if (finalCandidates.isEmpty()) return@forEach

            // Escolhe o melhor jogador possível (maior overall) que consiga pagar
            val bestCandidatePair = finalCandidates.maxByOrNull { it.first.getBaseOverall(it.first.mainPosition) } ?: return@forEach
            val targetPlayer = bestCandidatePair.first
            val transferFee = bestCandidatePair.second

            // 30% de probabilidade de rutura contratual nas negociações com a IA
            if (Random.nextInt(1, 101) <= 30) {
                println("[MERCADO IA] As negociações salariais com ${targetPlayer.name} falharam. Transferência abortada.")
                return@forEach
            }

            // 5. Executar a Transferência
            val sellerClub = allClubs.find { it.id == targetPlayer.clubId }

            // Deduz o orçamento do comprador
            buyerClub.budget -= transferFee

            // Adiciona ao orçamento do vendedor (se não for agente livre)
            if (sellerClub != null) {
                sellerClub.budget += transferFee
            }

            // Atualiza os dados do jogador contratado
            val oldClubName = sellerClub?.name ?: "Agente Livre"
            
            // Registar no Histórico de Transferências da Liga
            val transferEvent = com.brunogarcia.footballempireclubmanager.domain.model.TransferEvent(
                week = repository.getCurrentWeek(),
                playerName = targetPlayer.name,
                playerPosition = targetPlayer.mainPosition.name,
                overall = targetPlayer.getBaseOverall(targetPlayer.mainPosition),
                fromClubName = oldClubName,
                toClubName = buyerClub.name,
                fee = transferFee
            )
            val currentHistory = repository.getTransferHistory().toMutableList()
            currentHistory.add(transferEvent)
            repository.saveTransferHistory(currentHistory)

            targetPlayer.clubId = buyerClub.id
            targetPlayer.isListed = false
            targetPlayer.transferOffer = null
            targetPlayer.offerClubName = null
            targetPlayer.contractYears = 3 // Recebe contrato padrão de 3 anos no novo clube
            targetPlayer.lastTransferWeek = repository.getCurrentWeek()

            println("[MERCADO IA] O clube ${buyerClub.name} contratou ${targetPlayer.name} ($targetGroup - OVR ${targetPlayer.getBaseOverall(targetPlayer.mainPosition)}) ao clube $oldClubName por ${transferFee.toInt()} €.")
        }
    }

    /**
     * Faz com que os clubes da IA façam a gestão da sua lista de transferências de forma lógica.
     * Se tiverem excedente num setor, colocam o jogador mais fraco desse setor na lista.
     * Se tiverem défice, removem os seus jogadores da lista.
     */
    private fun updateAITransferList(allClubs: List<Club>, allPlayers: List<Player>, userClubId: String) {
        val aiClubs = allClubs.filter { it.id != userClubId }
        
        aiClubs.forEach { club ->
            val clubPlayers = allPlayers.filter { it.clubId == club.id }
            
            PositionGroup.values().forEach { group ->
                val groupPlayers = clubPlayers.filter { getPositionGroup(it.mainPosition) == group }
                val currentCount = groupPlayers.size
                val ideal = getIdealCount(group)
                
                if (currentCount > ideal) {
                    // Tem excedente neste setor!
                    // Há 30% de chance semanal de listar o jogador com menor overall deste setor
                    if (Random.nextInt(1, 101) <= 30) {
                        val unlistedPlayers = groupPlayers.filter { !it.isListed }
                        if (unlistedPlayers.isNotEmpty()) {
                            val weakestPlayer = unlistedPlayers.minByOrNull { it.getBaseOverall(it.mainPosition) }
                            weakestPlayer?.isListed = true
                        }
                    }
                } else if (currentCount < ideal) {
                    // Tem défice neste setor!
                    // Retira os seus jogadores deste setor que estejam listados
                    groupPlayers.filter { it.isListed }.forEach { it.isListed = false }
                }
            }
        }
    }
}