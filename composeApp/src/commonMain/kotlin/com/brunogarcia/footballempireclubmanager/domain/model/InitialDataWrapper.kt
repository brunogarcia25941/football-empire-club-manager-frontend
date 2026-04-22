package com.brunogarcia.footballempireclubmanager.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class InitialDataWrapper(
    val clubs: List<Club>,
    val players: List<Player>
)