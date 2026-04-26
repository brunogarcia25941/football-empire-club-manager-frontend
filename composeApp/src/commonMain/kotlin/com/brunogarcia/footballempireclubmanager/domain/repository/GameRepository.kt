package com.brunogarcia.footballempireclubmanager.domain.repository

import com.brunogarcia.footballempireclubmanager.domain.engine.MatchResult
import com.brunogarcia.footballempireclubmanager.domain.model.Club
import com.brunogarcia.footballempireclubmanager.domain.model.Player

interface GameRepository {
    // Carregar o jogo pela primeira vez
    fun initializeGame(clubs: List<Club>, players: List<Player>, userClubId: String)

    // Ler os dados
    fun getAllClubs(): List<Club>
    fun getAllPlayers(): List<Player>
    fun getUserClubId(): String
    fun getCurrentWeek(): Int

    // Escrever/Atualizar
    fun advanceWeek()
    fun saveMatchResults(results: List<MatchResult>)
    fun updateClubsAndPlayers(clubs: List<Club>, players: List<Player>)
    fun getMatchHistory(): List<MatchResult>

    // Táticas
    fun getUserStarting11(): List<com.brunogarcia.footballempireclubmanager.domain.engine.StartingPlayer>
    fun saveUserStarting11(starting11: List<com.brunogarcia.footballempireclubmanager.domain.engine.StartingPlayer>)

    // Calendário
    fun getFixtures(): List<com.brunogarcia.footballempireclubmanager.domain.model.Fixture>
    fun saveFixtures(fixtures: List<com.brunogarcia.footballempireclubmanager.domain.model.Fixture>)
}