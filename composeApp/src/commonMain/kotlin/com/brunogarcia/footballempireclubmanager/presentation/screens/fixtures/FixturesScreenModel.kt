package com.brunogarcia.footballempireclubmanager.presentation.screens.fixtures

import cafe.adriel.voyager.core.model.ScreenModel
import com.brunogarcia.footballempireclubmanager.domain.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Estrutura simples para a interface desenhar o cartão do jogo
data class MatchDisplayItem(
    val week: Int,
    val homeClubId: String,
    val awayClubId: String,
    val opponentName: String,
    val isHome: Boolean,
    val homeGoals: Int? = null,
    val awayGoals: Int? = null,
    val isPlayed: Boolean = false
)

class FixturesScreenModel(private val repository: GameRepository) : ScreenModel {
    private val _matches = MutableStateFlow<List<MatchDisplayItem>>(emptyList())
    val matches: StateFlow<List<MatchDisplayItem>> = _matches

    fun loadFixtures() {
        val userClubId = repository.getUserClubId()
        val allClubs = repository.getAllClubs()
        val fixtures = repository.getFixtures()
        val history = repository.getMatchHistory()


        val displayItems = mutableListOf<MatchDisplayItem>()

        // 1. Filtrar só os jogos da equipa do utilizador e ordenar por jornada
        val myFixtures = fixtures.filter { it.homeClubId == userClubId || it.awayClubId == userClubId }
            .sortedBy { it.week }

        for (fixture in myFixtures) {
            val isHome = fixture.homeClubId == userClubId
            val opponentId = if (isHome) fixture.awayClubId else fixture.homeClubId
            val opponentName = allClubs.find { it.id == opponentId }?.name ?: "Desconhecido"

            // 2. Verificar se este jogo já aconteceu (procurando no histórico pelas duas equipas)
            val playedMatch = history.find {
                it.homeClubId == fixture.homeClubId && it.awayClubId == fixture.awayClubId
            }

            if (playedMatch != null) {
                // Jogo já realizado
                displayItems.add(
                    MatchDisplayItem(
                        week = fixture.week,
                        homeClubId = fixture.homeClubId,
                        awayClubId = fixture.awayClubId,
                        opponentName = opponentName,
                        isHome = isHome,
                        homeGoals = playedMatch.homeGoals,
                        awayGoals = playedMatch.awayGoals,
                        isPlayed = true
                    )
                )
            } else {
                // Jogo futuro
                displayItems.add(
                    MatchDisplayItem(
                        week = fixture.week,
                        homeClubId = fixture.homeClubId,
                        awayClubId = fixture.awayClubId,
                        opponentName = opponentName,
                        isHome = isHome,
                        isPlayed = false
                    )
                )
            }
        }
        _matches.value = displayItems
    }
}