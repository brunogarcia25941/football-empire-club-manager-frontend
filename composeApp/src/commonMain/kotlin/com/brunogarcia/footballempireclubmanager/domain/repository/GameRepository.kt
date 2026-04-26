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
    // Adiciona esta linha junto às outras funções de "Ler os dados"
    fun getMatchHistory(): List<MatchResult>
}