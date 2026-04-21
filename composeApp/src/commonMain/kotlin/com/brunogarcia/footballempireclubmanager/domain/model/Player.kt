package com.brunogarcia.footballempireclubmanager.domain.model

import kotlin.math.roundToInt
import com.brunogarcia.footballempireclubmanager.domain.engine.PositionWeightRules

data class Player(
    val id: String,
    val clubId: String,
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
    var gkAgility: Int   // Agilidade
) {
    /**
     * Calcula o Overall Efetivo do jogador se jogar na [targetPosition].
     * Utiliza uma Média Ponderada com base nos atributos do jogador.
     */
    fun getEffectiveOverall(targetPosition: Position): Int {
        // Vai buscar os pesos da posição. Se por algum motivo falhar, retorna 0.
        val w = PositionWeightRules.weights[targetPosition] ?: return 0

        val baseOverall = (
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

        var finalOverall = baseOverall
        if (isFieldPlayerAsGk || isGkAsFieldPlayer) {
            finalOverall *= 0.1 // Corta 90% das capacidades, é um desastre tático!
        }

        // A moral e o cansaço (Stamina) também afetam o rendimento no dia de jogo!
        // Um jogador a cair de cansaço (stamina = 50) não joga a 100%.
        val conditionMultiplier = (stamina.toDouble() / 100.0) * 0.8 + 0.2 // Pesa 80% do overall atual
        val moraleMultiplier = (morale.toDouble() / 100.0) * 0.2 + 0.8     // Pesa 20% do overall atual

        finalOverall = finalOverall * conditionMultiplier * moraleMultiplier

        return finalOverall.roundToInt()
    }
}

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