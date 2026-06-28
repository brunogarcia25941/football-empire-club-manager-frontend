package com.brunogarcia.footballempireclubmanager.presentation.screens.tactics

import cafe.adriel.voyager.core.model.ScreenModel
import com.brunogarcia.footballempireclubmanager.domain.engine.StartingPlayer
import com.brunogarcia.footballempireclubmanager.domain.model.Player
import com.brunogarcia.footballempireclubmanager.domain.model.Position
import com.brunogarcia.footballempireclubmanager.domain.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Representa um "buraco" na tática (ex: O lugar de Defesa Central)
data class TacticSlot(val id: Int, val role: Position, val player: Player? = null)

data class TacticsState(
    val slots: List<TacticSlot> = emptyList(),
    val squad: List<Player> = emptyList(),
    val selectedSlotId: Int? = null // Se tiver um número, significa que estamos a escolher um jogador para esse slot
)

class TacticsScreenModel(private val repository: GameRepository) : ScreenModel {

    private val _state = MutableStateFlow(TacticsState())
    val state: StateFlow<TacticsState> = _state

    init {
        loadTactics()
    }

    fun loadTactics() {
        val userClubId = repository.getUserClubId()
        val squad = repository.getAllPlayers().filter { it.clubId == userClubId }
        val saved11 = repository.getUserStarting11()

        // Vamos forçar um 4-3-3 clássico para já
        val defaultRoles = listOf(
            Position.GK,
            Position.RB, Position.CB, Position.CB, Position.LB,
            Position.CDM, Position.CM, Position.CAM,
            Position.RW, Position.LW, Position.ST
        )

        // Criar os 11 lugares e tentar preenchê-los com o que já estava guardado
        val initialSlots = defaultRoles.mapIndexed { index, position ->
            val savedPlayer = saved11.getOrNull(index)?.player
            TacticSlot(id = index, role = position, player = savedPlayer)
        }

        _state.value = TacticsState(slots = initialSlots, squad = squad)
    }

    // Abre o menu para escolher quem vai para aquela posição
    fun openPlayerSelection(slotId: Int) {
        _state.value = _state.value.copy(selectedSlotId = slotId)
    }

    fun closePlayerSelection() {
        _state.value = _state.value.copy(selectedSlotId = null)
    }

    // Seleciona automaticamente o melhor 11 possível para o utilizador
    fun autoPickStarting11() {
        val currentState = _state.value
        val availablePlayers = currentState.squad.toMutableList()
        val assignedSlots = mutableListOf<TacticSlot>()
        
        val roles = listOf(
            Position.GK,
            Position.RB, Position.CB, Position.CB, Position.LB,
            Position.CDM, Position.CM, Position.CAM,
            Position.RW, Position.LW, Position.ST
        )
        
        val selectedPlayerIds = mutableSetOf<String>()
        
        // 1ª Passagem: Tentar preencher com jogadores na sua posição natural (com melhor overall)
        roles.forEachIndexed { index, role ->
            val bestFit = availablePlayers
                .filter { it.mainPosition == role && !selectedPlayerIds.contains(it.id) }
                .maxByOrNull { it.getEffectiveOverall(role) }
                
            if (bestFit != null) {
                assignedSlots.add(TacticSlot(id = index, role = role, player = bestFit))
                selectedPlayerIds.add(bestFit.id)
            } else {
                assignedSlots.add(TacticSlot(id = index, role = role, player = null))
            }
        }
        
        // 2ª Passagem: Preencher posições vazias com os melhores suplentes restantes
        assignedSlots.forEachIndexed { index, slot ->
            if (slot.player == null) {
                val remainingPlayers = availablePlayers.filter { !selectedPlayerIds.contains(it.id) }
                val fallbackPlayer = remainingPlayers.maxByOrNull { it.getEffectiveOverall(slot.role) }
                
                if (fallbackPlayer != null) {
                    assignedSlots[index] = slot.copy(player = fallbackPlayer)
                    selectedPlayerIds.add(fallbackPlayer.id)
                }
            }
        }
        
        _state.value = currentState.copy(slots = assignedSlots)
        saveToRepository(assignedSlots)
    }

    // Coloca o jogador no campo
    fun assignPlayerToSlot(player: Player) {
        val currentState = _state.value
        val slotId = currentState.selectedSlotId ?: return

        // Mágica: Remove o jogador do slot antigo (se ele já estivesse noutra posição)
        // e coloca-o no novo slot.
        val updatedSlots = currentState.slots.map { slot ->
            when {
                slot.player?.id == player.id -> slot.copy(player = null) // Limpa o antigo
                slot.id == slotId -> slot.copy(player = player)          // Ocupa o novo
                else -> slot
            }
        }

        _state.value = currentState.copy(slots = updatedSlots, selectedSlotId = null)
        saveToRepository(updatedSlots)
    }

    private fun saveToRepository(slots: List<TacticSlot>) {
        // Converte os nossos "Slots" para os "StartingPlayer" que o MatchEngine entende
        val starting11 = slots.mapNotNull { slot ->
            slot.player?.let { StartingPlayer(player = it, playingPosition = slot.role) }
        }
        repository.saveUserStarting11(starting11)
    }
}