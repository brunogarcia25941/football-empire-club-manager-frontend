package com.brunogarcia.footballempireclubmanager.data.repository

import com.brunogarcia.footballempireclubmanager.domain.engine.MatchResult
import com.brunogarcia.footballempireclubmanager.domain.model.Club
import com.brunogarcia.footballempireclubmanager.domain.model.Player
import com.brunogarcia.footballempireclubmanager.domain.repository.GameRepository
import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.brunogarcia.footballempireclubmanager.domain.model.SaveGameData

class GameRepositoryImpl : GameRepository {

    private val clubs = mutableListOf<Club>()
    private val players = mutableListOf<Player>()
    private val matchHistory = mutableListOf<MatchResult>()

    private var currentUserClubId: String = ""
    private var currentWeek: Int = 1

    private val userStarting11 = mutableListOf<com.brunogarcia.footballempireclubmanager.domain.engine.StartingPlayer>()

    private val fixtures = mutableListOf<com.brunogarcia.footballempireclubmanager.domain.model.Fixture>()

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

    override fun getUserStarting11(): List<com.brunogarcia.footballempireclubmanager.domain.engine.StartingPlayer> {
        return userStarting11.toList()
    }

    override fun saveUserStarting11(starting11: List<com.brunogarcia.footballempireclubmanager.domain.engine.StartingPlayer>) {
        userStarting11.clear()
        userStarting11.addAll(starting11)
    }

    override fun getFixtures(): List<com.brunogarcia.footballempireclubmanager.domain.model.Fixture> = fixtures.toList()

    override fun saveFixtures(newFixtures: List<com.brunogarcia.footballempireclubmanager.domain.model.Fixture>) {
        fixtures.clear()
        fixtures.addAll(newFixtures)
    }

    private val settings = Settings()
    private val SAVE_KEY = "football_empire_save_v1"

    override fun hasSavedGame(): Boolean {
        return settings.hasKey(SAVE_KEY)
    }

    override fun saveGameToDisk() {
        val saveData = SaveGameData(
            clubs = clubs,
            players = players,
            fixtures = fixtures,
            matchHistory = matchHistory,
            currentWeek = currentWeek,
            userClubId = currentUserClubId,
            starting11 = userStarting11
        )
        // Converte o jogo t0do para uma string JSON e guarda no telemóvel
        val jsonString = Json.encodeToString(saveData)
        settings.putString(SAVE_KEY, jsonString)
        println("Jogo Guardado com Sucesso!")
    }

    override fun loadGameFromDisk(): Boolean {
        val jsonString = settings.getStringOrNull(SAVE_KEY) ?: return false
        return try {
            val saveData = Json.decodeFromString<SaveGameData>(jsonString)

            // Limpa tudo e carrega os dados do save
            clubs.clear(); clubs.addAll(saveData.clubs)
            players.clear(); players.addAll(saveData.players)
            fixtures.clear(); fixtures.addAll(saveData.fixtures)
            matchHistory.clear(); matchHistory.addAll(saveData.matchHistory)
            userStarting11.clear(); userStarting11.addAll(saveData.starting11)

            currentWeek = saveData.currentWeek
            currentUserClubId = saveData.userClubId
            true
        } catch (e: Exception) {
            println("Erro ao carregar o jogo: ${e.message}")
            false
        }
    }
}