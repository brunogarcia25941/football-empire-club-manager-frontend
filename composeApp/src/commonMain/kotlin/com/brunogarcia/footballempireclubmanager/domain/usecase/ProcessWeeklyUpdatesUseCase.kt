package com.brunogarcia.footballempireclubmanager.domain.usecase

import com.brunogarcia.footballempireclubmanager.domain.engine.MatchResult
import com.brunogarcia.footballempireclubmanager.domain.model.Club
import com.brunogarcia.footballempireclubmanager.domain.model.Player
import com.brunogarcia.footballempireclubmanager.domain.model.Position
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Caso de Uso responsável por processar todas as atualizações que ocorrem no final de uma semana.
 * Inclui: Recuperação física, Finanças, Evolução Dinâmica de Atributos e Propostas.
 */
class ProcessWeeklyUpdatesUseCase {

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
}