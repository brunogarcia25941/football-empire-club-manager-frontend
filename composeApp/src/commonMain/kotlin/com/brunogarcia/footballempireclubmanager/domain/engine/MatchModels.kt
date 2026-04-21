package com.brunogarcia.footballempireclubmanager.domain.engine

import com.brunogarcia.footballempireclubmanager.domain.model.Player

enum class MatchEventType {
    GOAL,
    YELLOW_CARD,
    RED_CARD,
    INJURY
}

data class MatchEvent(
    val minute: Int,
    val type: MatchEventType,
    val playerId: String,
    val playerName: String,
    val clubId: String
)

data class MatchResult(
    val homeClubId: String,
    val awayClubId: String,
    var homeGoals: Int = 0,
    var awayGoals: Int = 0,
    val events: MutableList<MatchEvent> = mutableListOf()
)

// Uma estrutura temporária para ajudar o motor de jogo a saber em que posição
// cada jogador está a jogar neste jogo específico.
data class StartingPlayer(
    val player: Player,
    val playingPosition: com.brunogarcia.footballempireclubmanager.domain.model.Position
)