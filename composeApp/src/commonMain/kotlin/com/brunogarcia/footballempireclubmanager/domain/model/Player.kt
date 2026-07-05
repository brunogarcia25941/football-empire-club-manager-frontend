package com.brunogarcia.footballempireclubmanager.domain.model

import kotlin.math.roundToInt
import kotlin.math.pow
import com.brunogarcia.footballempireclubmanager.domain.engine.PositionWeightRules
import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val id: String,
    var clubId: String,
    val name: String,
    val age: Int,
    val mainPosition: Position, // Posição de origem

    // Status Flutuante
    var stamina: Int, // 0 a 100
    var morale: Int, // 0 a 100

    // --- Atributos de Campo (1 a 100) ---
    // Físico
    var pace: Int,      // Velocidade
    var strength: Int,  // Força
    // Defesa
    var tackling: Int,              // Corte
    var defensivePositioning: Int,  // Posicionamento Defensivo
    // Passe/Criação
    var vision: Int,    // Visão de Jogo
    var passing: Int,   // Passe
    var dribbling: Int, // Drible (Técnico)
    // Ataque
    var offensivePositioning: Int, // Posicionamento Ofensivo (Sem bola)
    var finishing: Int, // Remate
    var heading: Int,   // Cabeceamento

    // --- Atributos de Guarda-Redes (1 a 100) ---
    // (Jogadores de campo terão isto a um valor muito baixo, tipo 5 ou 10)
    var gkReflexes: Int, // Reflexos
    var gkHandling: Int, // Jogo de Mãos
    var gkAgility: Int,   // Agilidade

    // --- Gestão de Contratos e Transferências (Opção C) ---
    var contractYears: Int = 3,
    var isListed: Boolean = false,
    var transferOffer: Double? = null,
    var offerClubName: String? = null,
    var seasonMatches: Int = 0,
    var lastTransferWeek: Int = -1
) {
    /**
     * Calcula o Overall Base do jogador na [targetPosition] sem considerar cansaço ou moral.
     * Utiliza uma Média Ponderada com base nos atributos do jogador.
     */
    fun getBaseOverall(targetPosition: Position): Int {
        // Vai buscar os pesos da posição. Se por algum motivo falhar, retorna 0.
        val w = PositionWeightRules.weights[targetPosition] ?: return 0

        var baseOverall = (
                (pace * w.pace) +
                        (strength * w.strength) +
                        (tackling * w.tackling) +
                        (defensivePositioning * w.defensivePositioning) +
                        (vision * w.vision) +
                        (passing * w.passing) +
                        (dribbling * w.dribbling) +
                        (offensivePositioning * w.offensivePositioning) +
                        (finishing * w.finishing) +
                        (heading * w.heading) +
                        (gkReflexes * w.gkReflexes) +
                        (gkHandling * w.gkHandling) +
                        (gkAgility * w.gkAgility)
                )

        // Se quisermos ser exigentes, podemos aplicar uma pequena penalização
        // extra se o jogador for de campo a jogar a GR ou vice-versa.
        val isFieldPlayerAsGk = mainPosition != Position.GK && targetPosition == Position.GK
        val isGkAsFieldPlayer = mainPosition == Position.GK && targetPosition != Position.GK

        if (isFieldPlayerAsGk || isGkAsFieldPlayer) {
            baseOverall *= 0.1 // Corta 90% das capacidades, é um desastre tático!
        }

        return baseOverall.roundToInt()
    }

    /**
     * Calcula o Overall Efetivo do jogador se jogar na [targetPosition].
     * Leva em conta o cansaço (stamina) e a moral do jogador no rendimento em campo.
     */
    fun getEffectiveOverall(targetPosition: Position): Int {
        val baseOverall = getBaseOverall(targetPosition).toDouble()

        // A moral e o cansaço (Stamina) também afetam o rendimento no dia de jogo!
        // Um jogador a cair de cansaço (stamina = 50) não joga a 100%.
        val conditionMultiplier = (stamina.toDouble() / 100.0) * 0.8 + 0.2 // Pesa 80% do overall atual
        val moraleMultiplier = (morale.toDouble() / 100.0) * 0.2 + 0.8     // Pesa 20% do overall atual

        val finalOverall = baseOverall * conditionMultiplier * moraleMultiplier

        return finalOverall.roundToInt()
    }

    /**
     * Calcula o valor de mercado do jogador com base no seu Overall Base e na Idade.
     * Utiliza uma curva exponencial de overall e um multiplicador de idade.
     */
    fun getMarketValue(): Double {
        val overall = getBaseOverall(mainPosition)
        
        // Base value: exponencial com base de 1.23.
        // OVR 50 = 10k, OVR 70 = 628k, OVR 80 = 4.9M, OVR 83 = 9.2M, OVR 90 = 39M
        val safeOvrDiff = (overall - 50).coerceAtLeast(0)
        val baseValue = 10000.0 * 1.23.pow(safeOvrDiff.toDouble())
        
        // Multiplicador baseado na idade (veteranos desvalorizam e jovens têm bónus de potencial)
        val ageMultiplier = when {
            age <= 21 -> 1.4
            age <= 25 -> 1.2
            age <= 28 -> 1.0
            age <= 31 -> 0.7
            age <= 33 -> 0.4
            else -> 0.15
        }
        
        return baseValue * ageMultiplier
    }
}

@Serializable
enum class Position {
    GK, // Guarda-Redes
    CB, // Defesa Central
    RB, // Lateral Direito
    LB, // Lateral Esquerdo
    CDM, // Médio Centro Defensivo (Trinco)
    CM, // Médio Centro
    CAM, // Médio Centro Ofensivo (Nº 10)
    RM, // Médio Direito
    LM, // Médio Esquerdo
    RW, // Extremo Direito
    LW, // Extremo Esquerdo
    ST  // Avançado
}