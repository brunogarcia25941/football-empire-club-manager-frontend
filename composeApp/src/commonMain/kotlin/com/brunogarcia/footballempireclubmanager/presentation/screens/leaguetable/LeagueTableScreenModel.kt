package com.brunogarcia.footballempireclubmanager.presentation.screens.leaguetable

import cafe.adriel.voyager.core.model.ScreenModel
import com.brunogarcia.footballempireclubmanager.domain.model.LeagueTableEntry
import com.brunogarcia.footballempireclubmanager.domain.repository.GameRepository
import com.brunogarcia.footballempireclubmanager.domain.usecase.CalculateLeagueTableUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LeagueTableScreenModel(
    private val repository: GameRepository,
    private val calculateLeagueTableUseCase: CalculateLeagueTableUseCase
) : ScreenModel {

    private val _table = MutableStateFlow<List<LeagueTableEntry>>(emptyList())
    val table: StateFlow<List<LeagueTableEntry>> = _table

    private val _selectedDivision = MutableStateFlow(1)
    val selectedDivision: StateFlow<Int> = _selectedDivision

    fun initSelectedDivision() {
        val userClubId = repository.getUserClubId()
        val clubs = repository.getAllClubs()
        val userClub = clubs.find { it.id == userClubId }
        val division = userClub?.divisionLevel ?: 1
        _selectedDivision.value = division
        refreshTable(division)
    }

    fun selectDivision(division: Int) {
        _selectedDivision.value = division
        refreshTable(division)
    }

    fun getUserClubId(): String = repository.getUserClubId()

    private fun refreshTable(division: Int) {
        val clubs = repository.getAllClubs()
        val results = repository.getMatchHistory()
        _table.value = calculateLeagueTableUseCase.execute(clubs, results, division)
    }
}