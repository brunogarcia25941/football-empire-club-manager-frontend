package com.brunogarcia.footballempireclubmanager.domain.model

import kotlinx.serialization.Serializable

/**
 * Representa um evento de transferência ocorrido na liga.
 * Usado para manter o histórico de contratações de cada temporada.
 */
@Serializable
data class TransferEvent(
    val week: Int,                // Semana em que ocorreu a transferência
    val playerName: String,       // Nome do jogador transferido
    val playerPosition: String,   // Posição principal do jogador
    val overall: Int,             // Classificação geral (OVR) do jogador
    val fromClubName: String,     // Clube de origem ("Agente Livre" ou clube vendedor)
    val toClubName: String,       // Clube de destino (comprador)
    val fee: Double               // Valor da transferência (0.0 para agentes livres)
)
