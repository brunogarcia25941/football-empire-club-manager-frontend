package com.brunogarcia.footballempireclubmanager.domain.model
import kotlinx.serialization.Serializable

@Serializable
data class Fixture(
    val week: Int,
    val homeClubId: String,
    val awayClubId: String
)