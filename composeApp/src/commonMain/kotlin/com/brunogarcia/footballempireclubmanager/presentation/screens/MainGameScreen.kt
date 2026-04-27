package com.brunogarcia.footballempireclubmanager.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.brunogarcia.footballempireclubmanager.presentation.screens.dashboard.DashboardScreen
import com.brunogarcia.footballempireclubmanager.presentation.screens.leaguetable.LeagueTableScreen
import com.brunogarcia.footballempireclubmanager.presentation.screens.squad.SquadScreen
import com.brunogarcia.footballempireclubmanager.presentation.screens.tactics.TacticsScreen
import com.brunogarcia.footballempireclubmanager.presentation.screens.fixtures.FixturesScreen

class MainGameScreen : Screen {

    @Composable
    override fun Content() {
        // O "Cérebro" das abas. Guarda qual a aba ativa (0 = Clube, 1 = Plantel, 2 = Táticas)
        var selectedTab by remember { mutableStateOf(0) }

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    // Botão 1: O Clube (Dashboard)
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Clube") },
                        label = { Text("Clube") }
                    )

                    // Botão 2: O Plantel
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Plantel") },
                        label = { Text("Plantel") }
                    )

                    // Botão 3: Táticas
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Filled.Star, contentDescription = "Táticas") },
                        label = { Text("Táticas") }
                    )

                    // Botão 4: Classificação
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Filled.List, contentDescription = "Tabela") }, // Importa Icons.Filled.List
                        label = { Text("Tabela") }
                    )

                    // Botão 5: Calendário / Resultados
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = { Icon(Icons.Filled.DateRange, contentDescription = "Jogos") },
                        label = { Text("Jogos") }
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                // Desenhamos o conteúdo do ecrã diretamente sem usar o Voyager.
                when (selectedTab) {
                    0 -> DashboardScreen().Content()
                    1 -> SquadScreen().Content()
                    2 -> TacticsScreen().Content()
                    3 -> LeagueTableScreen().Content()
                    4 -> FixturesScreen().Content()
                }
            }
        }
    }
}