package com.brunogarcia.footballempireclubmanager.domain.engine

import com.brunogarcia.footballempireclubmanager.domain.model.Player
import com.brunogarcia.footballempireclubmanager.domain.model.Position
import kotlin.random.Random

object YouthGenerator {
    private val firstNames = listOf(
        "Afonso", "Bernardo", "Diogo", "Duarte", "Francisco", "Gonçalo", "Guilherme", 
        "João", "José", "Martim", "Miguel", "Pedro", "Rodrigo", "Santiago", "Tomás", 
        "Tiago", "Gabriel", "Lucas", "Mateus", "Rafael", "Rui", "Manuel", "António", 
        "Carlos", "Luís", "Bruno", "Ricardo", "Vítor", "Hugo", "Nuno", "Jorge", "Filipe"
    )

    private val lastNames = listOf(
        "Silva", "Santos", "Ferreira", "Pereira", "Oliveira", "Costa", "Rodrigues", 
        "Martins", "Jesus", "Sousa", "Fernandes", "Gonçalves", "Gomes", "Lopes", 
        "Marques", "Cardoso", "Pinheiro", "Ribeiro", "Carvalho", "Teixeira", 
        "Almeida", "Dias", "Correia", "Pinto", "Mendes"
    )

    fun generateYouthPlayer(clubId: String, academyLevel: Int): Player {
        val id = "youth_${Random.nextInt(100000, 999999)}"
        val name = "${firstNames.random()} ${lastNames.random()}"
        val age = Random.nextInt(16, 18) // 16 ou 17 anos
        val position = Position.values().random()

        // Qualidade base baseada no nível da academia (1 a 10)
        // Nivel 1: média ~40 | Nivel 10: média ~70
        val baseAttribute = 38 + (academyLevel * 3.2).toInt() + Random.nextInt(-4, 5)
        val attributeValue = baseAttribute.coerceIn(15, 95)

        // Inicializa atributos com variação em torno do valor base
        var pace = Random.nextInt(attributeValue - 10, attributeValue + 10).coerceIn(10, 99)
        var strength = Random.nextInt(attributeValue - 10, attributeValue + 10).coerceIn(10, 99)
        var tackling = Random.nextInt(attributeValue - 10, attributeValue + 10).coerceIn(10, 99)
        var defensivePositioning = Random.nextInt(attributeValue - 10, attributeValue + 10).coerceIn(10, 99)
        var vision = Random.nextInt(attributeValue - 10, attributeValue + 10).coerceIn(10, 99)
        var passing = Random.nextInt(attributeValue - 10, attributeValue + 10).coerceIn(10, 99)
        var dribbling = Random.nextInt(attributeValue - 10, attributeValue + 10).coerceIn(10, 99)
        var offensivePositioning = Random.nextInt(attributeValue - 10, attributeValue + 10).coerceIn(10, 99)
        var finishing = Random.nextInt(attributeValue - 10, attributeValue + 10).coerceIn(10, 99)
        var heading = Random.nextInt(attributeValue - 10, attributeValue + 10).coerceIn(10, 99)

        var gkReflexes = Random.nextInt(5, 15)
        var gkHandling = Random.nextInt(5, 15)
        var gkAgility = Random.nextInt(5, 15)

        // Ajusta atributos com base na posição específica para ser realista
        when (position) {
            Position.GK -> {
                gkReflexes = Random.nextInt(attributeValue, attributeValue + 15).coerceIn(30, 99)
                gkHandling = Random.nextInt(attributeValue - 5, attributeValue + 10).coerceIn(30, 99)
                gkAgility = Random.nextInt(attributeValue, attributeValue + 15).coerceIn(30, 99)
                // Reduz atributos de campo
                pace = Random.nextInt(10, 35)
                strength = Random.nextInt(20, 50)
                tackling = Random.nextInt(5, 15)
                defensivePositioning = Random.nextInt(5, 15)
                vision = Random.nextInt(10, 30)
                passing = Random.nextInt(10, 40)
                dribbling = Random.nextInt(5, 20)
                offensivePositioning = Random.nextInt(5, 15)
                finishing = Random.nextInt(5, 15)
                heading = Random.nextInt(10, 30)
            }
            Position.CB -> {
                strength = (strength + 12).coerceIn(10, 99)
                tackling = (tackling + 15).coerceIn(10, 99)
                defensivePositioning = (defensivePositioning + 12).coerceIn(10, 99)
                heading = (heading + 10).coerceIn(10, 99)
                finishing = (finishing - 15).coerceIn(5, 99)
                dribbling = (dribbling - 10).coerceIn(5, 99)
            }
            Position.RB, Position.LB -> {
                pace = (pace + 15).coerceIn(10, 99)
                tackling = (tackling + 10).coerceIn(10, 99)
                defensivePositioning = (defensivePositioning + 8).coerceIn(10, 99)
                passing = (passing + 5).coerceIn(10, 99)
                finishing = (finishing - 10).coerceIn(5, 99)
            }
            Position.CDM -> {
                strength = (strength + 8).coerceIn(10, 99)
                tackling = (tackling + 12).coerceIn(10, 99)
                defensivePositioning = (defensivePositioning + 10).coerceIn(10, 99)
                passing = (passing + 8).coerceIn(10, 99)
                vision = (vision + 5).coerceIn(10, 99)
            }
            Position.CM -> {
                passing = (passing + 15).coerceIn(10, 99)
                vision = (vision + 12).coerceIn(10, 99)
                dribbling = (dribbling + 5).coerceIn(10, 99)
                tackling = (tackling + 5).coerceIn(10, 99)
            }
            Position.CAM -> {
                passing = (passing + 12).coerceIn(10, 99)
                vision = (vision + 15).coerceIn(10, 99)
                dribbling = (dribbling + 12).coerceIn(10, 99)
                finishing = (finishing + 5).coerceIn(10, 99)
                tackling = (tackling - 15).coerceIn(5, 99)
            }
            Position.RM, Position.LM, Position.RW, Position.LW -> {
                pace = (pace + 16).coerceIn(10, 99)
                dribbling = (dribbling + 12).coerceIn(10, 99)
                passing = (passing + 6).coerceIn(10, 99)
                finishing = (finishing + 4).coerceIn(10, 99)
                tackling = (tackling - 12).coerceIn(5, 99)
            }
            Position.ST -> {
                finishing = (finishing + 18).coerceIn(10, 99)
                offensivePositioning = (offensivePositioning + 15).coerceIn(10, 99)
                pace = (pace + 6).coerceIn(10, 99)
                strength = (strength + 5).coerceIn(10, 99)
                heading = (heading + 8).coerceIn(10, 99)
                tackling = (tackling - 20).coerceIn(5, 99)
                defensivePositioning = (defensivePositioning - 20).coerceIn(5, 99)
            }
        }

        return Player(
            id = id,
            clubId = "YOUTH_$clubId",
            name = name,
            age = age,
            mainPosition = position,
            stamina = 100,
            morale = 80,
            pace = pace,
            strength = strength,
            tackling = tackling,
            defensivePositioning = defensivePositioning,
            vision = vision,
            passing = passing,
            dribbling = dribbling,
            offensivePositioning = offensivePositioning,
            finishing = finishing,
            heading = heading,
            gkReflexes = gkReflexes,
            gkHandling = gkHandling,
            gkAgility = gkAgility,
            contractYears = 1,
            isListed = false,
            transferOffer = null,
            offerClubName = null
        )
    }
}
