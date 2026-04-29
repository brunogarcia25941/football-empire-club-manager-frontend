package com.brunogarcia.footballempireclubmanager.presentation.screens.facilities

import cafe.adriel.voyager.core.model.ScreenModel
import com.brunogarcia.footballempireclubmanager.domain.repository.GameRepository
import com.brunogarcia.footballempireclubmanager.domain.usecase.FacilityType
import com.brunogarcia.footballempireclubmanager.domain.usecase.UpgradeFacilityUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class FacilitiesState(
    val budget: Double = 0.0,
    val stadiumCapacity: Int = 0,
    val stadiumUpgradeCost: Double = 0.0,
    val trainingUpgradeCost: Double = 2500000.0
)

class FacilitiesScreenModel(
    private val repository: GameRepository,
    private val upgradeFacilityUseCase: UpgradeFacilityUseCase
) : ScreenModel {

    private val _state = MutableStateFlow(FacilitiesState())
    val state: StateFlow<FacilitiesState> = _state

    init {
        loadFacilities()
    }

    private fun loadFacilities() {
        val userClubId = repository.getUserClubId()
        val club = repository.getAllClubs().find { it.id == userClubId }

        if (club != null) {
            _state.value = FacilitiesState(
                budget = club.budget,
                stadiumCapacity = club.stadiumCapacity,
                stadiumUpgradeCost = upgradeFacilityUseCase.calculateStadiumUpgradeCost(club.stadiumCapacity)
            )
        }
    }

    fun upgradeFacility(type: FacilityType) {
        val success = upgradeFacilityUseCase.execute(type)
        if (success) {
            loadFacilities() // Atualiza os valores no ecrã (menos dinheiro, mais lugares)
        } else {
            // Aqui podemos meter um aviso de "Sem dinheiro"
        }
    }
}