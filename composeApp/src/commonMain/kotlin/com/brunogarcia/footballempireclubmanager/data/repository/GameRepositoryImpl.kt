package com.brunogarcia.footballempireclubmanager.data.repository

import com.brunogarcia.footballempireclubmanager.domain.engine.MatchResult
import com.brunogarcia.footballempireclubmanager.domain.model.Club
import com.brunogarcia.footballempireclubmanager.domain.model.Player
import com.brunogarcia.footballempireclubmanager.domain.repository.GameRepository

class GameRepositoryImpl : GameRepository {

    private val clubs = mutableListOf<Club>()
    private val players = mutableListOf<Player>()
    private val matchHistory = mutableListOf<MatchResult>()

    private var currentUserClubId: String = ""
    private var currentWeek: Int = 1

    override fun initializeGame(newClubs: List<Club>, newPlayers: List<Player>, userClubId: String) {
        clubs.clear()
        players.clear()
        matchHistory.clear()

        clubs.addAll(newClubs)
        players.addAll(newPlayers)
        currentUserClubId = userClubId
        currentWeek = 1
    }

    override fun getAllClubs(): List<Club> = clubs.toList() // Retorna uma cópia segura

    override fun getAllPlayers(): List<Player> = players.toList()

    override fun getUserClubId(): String = currentUserClubId

    override fun getCurrentWeek(): Int = currentWeek

    override fun advanceWeek() {
        currentWeek++
    }

    override fun saveMatchResults(results: List<MatchResult>) {
        matchHistory.addAll(results)
    }

    override fun updateClubsAndPlayers(updatedClubs: List<Club>, updatedPlayers: List<Player>) {
        clubs.clear()
        clubs.addAll(updatedClubs)

        players.clear()
        players.addAll(updatedPlayers)
    }
    override fun getMatchHistory(): List<MatchResult> = matchHistory.toList()
}