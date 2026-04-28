package com.brunogarcia.footballempireclubmanager.domain.model

import com.brunogarcia.footballempireclubmanager.domain.engine.MatchResult
import com.brunogarcia.footballempireclubmanager.domain.engine.StartingPlayer
import kotlinx.serialization.Serializable

@Serializable
data class SaveGameData(
    val clubs: List<Club>,
    val players: List<Player>,
    val fixtures: List<Fixture>,
    val matchHistory: List<MatchResult>,
    val currentWeek: Int,
    val userClubId: String,
    val starting11: List<StartingPlayer>
)