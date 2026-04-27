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

    fun refreshTable() {
        val clubs = repository.getAllClubs()
        val results = repository.getMatchHistory()
        _table.value = calculateLeagueTableUseCase.execute(clubs, results)
    }
}