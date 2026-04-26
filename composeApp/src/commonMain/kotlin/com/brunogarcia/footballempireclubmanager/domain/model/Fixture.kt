package com.brunogarcia.footballempireclubmanager.domain.model

data class Fixture(
    val week: Int,
    val homeClubId: String,
    val awayClubId: String
)