package com.brunogarcia.footballempireclubmanager.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Club(
    val id: String,
    val name: String,
    val primaryColor: String, // Ex: "#FF0000"
    val divisionLevel: Int,   // 1, 2, 3...
    var budget: Double,       // Saldo atual
    var stadiumCapacity: Int,
    var ticketPrice: Double,
    var fanBaseLoyalty: Int,  // 0 a 100
    var reputation: Int,      // 0 a 100
    var youthAcademyLevel: Int, // 1 a 10
    var trainingFacilities: Int // 1 a 10
)