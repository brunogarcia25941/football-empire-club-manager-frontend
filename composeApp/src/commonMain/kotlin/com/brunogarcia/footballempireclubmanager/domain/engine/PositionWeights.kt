package com.brunogarcia.footballempireclubmanager.domain.engine

import com.brunogarcia.footballempireclubmanager.domain.model.Position

/**
 * Define o peso (de 0.0 a 1.0) de cada atributo para o cálculo do Overall.
 * A soma de todos os pesos numa posição deve ser sempre 1.0 (100%).
 */
data class AttributeWeights(
    val pace: Double = 0.0,
    val strength: Double = 0.0,
    val tackling: Double = 0.0,
    val defensivePositioning: Double = 0.0,
    val vision: Double = 0.0,
    val passing: Double = 0.0,
    val dribbling: Double = 0.0,
    val offensivePositioning: Double = 0.0,
    val finishing: Double = 0.0,
    val heading: Double = 0.0,
    val gkReflexes: Double = 0.0,
    val gkHandling: Double = 0.0,
    val gkAgility: Double = 0.0
)

object PositionWeightRules {
    val weights: Map<Position, AttributeWeights> = mapOf(

        // --- GUARDA-REDES ---
        Position.GK to AttributeWeights(
            gkReflexes = 0.30, gkHandling = 0.24, gkAgility = 0.24, passing = 0.10, vision = 0.10, pace = 0.02
        ),

        // --- DEFESAS ---
        Position.CB to AttributeWeights(
            tackling = 0.35, defensivePositioning = 0.30, strength = 0.20, heading = 0.10, pace = 0.05
        ),
        Position.LB to AttributeWeights(
            pace = 0.25, tackling = 0.20, defensivePositioning = 0.15, passing = 0.15, dribbling = 0.10, strength = 0.05, vision = 0.05, offensivePositioning = 0.05
        ),
        Position.RB to AttributeWeights( // Igual ao LB
            pace = 0.25, tackling = 0.20, defensivePositioning = 0.15, passing = 0.15, dribbling = 0.10, strength = 0.05, vision = 0.05, offensivePositioning = 0.05
        ),

        // --- MÉDIOS ---
        Position.CDM to AttributeWeights(
            tackling = 0.25, defensivePositioning = 0.20, passing = 0.20, strength = 0.10, vision = 0.10, pace = 0.05, dribbling = 0.05, heading = 0.05
        ),
        Position.CM to AttributeWeights(
            passing = 0.25, vision = 0.15, dribbling = 0.15, defensivePositioning = 0.05, offensivePositioning = 0.10, pace = 0.10, strength = 0.10, heading = 0.05, finishing = 0.05
        ),
        Position.CAM to AttributeWeights(
            vision = 0.20, passing = 0.20, dribbling = 0.25, offensivePositioning = 0.15, finishing = 0.10, pace = 0.10
        ),
        Position.LM to AttributeWeights(
            pace = 0.25, dribbling = 0.20, passing = 0.20, vision = 0.10, offensivePositioning = 0.10, finishing = 0.10, defensivePositioning = 0.05
        ),
        Position.RM to AttributeWeights( // Igual ao LM
            pace = 0.25, dribbling = 0.20, passing = 0.20, vision = 0.10, offensivePositioning = 0.10, finishing = 0.10, defensivePositioning = 0.05
        ),

        // --- AVANÇADOS ---
        Position.LW to AttributeWeights(
            pace = 0.30, dribbling = 0.25, offensivePositioning = 0.15, finishing = 0.15, passing = 0.10, vision = 0.05
        ),
        Position.RW to AttributeWeights( // Igual ao LW
            pace = 0.30, dribbling = 0.25, offensivePositioning = 0.15, finishing = 0.15, passing = 0.10, vision = 0.05
        ),
        Position.ST to AttributeWeights(
            finishing = 0.30, offensivePositioning = 0.20, pace = 0.15, heading = 0.15, strength = 0.10, dribbling = 0.10
        )
    )
}