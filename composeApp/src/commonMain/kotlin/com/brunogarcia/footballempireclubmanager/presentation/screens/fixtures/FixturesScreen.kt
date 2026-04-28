package com.brunogarcia.footballempireclubmanager.presentation.screens.fixtures

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.brunogarcia.footballempireclubmanager.presentation.screens.matchreport.MatchReportScreen
import androidx.compose.foundation.clickable

class FixturesScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<FixturesScreenModel>()
        val matches by screenModel.matches.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        // Garante que a lista atualiza sempre que abrimos a aba
        LaunchedEffect(Unit) { screenModel.loadFixtures() }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Calendário de Jogos") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(matches) { match ->
                    MatchItemCard(match, onClick = {
                        if (match.isPlayed) {
                            // Abre o relatório passando os IDs das equipas
                            navigator.push(MatchReportScreen(match.homeClubId, match.awayClubId))
                        }
                    })
                }
            }
        }
    }

    @Composable
    private fun MatchItemCard(match: MatchDisplayItem, onClick: () -> Unit) {
        // Se já foi jogado, fica com o fundo ligeiramente diferente
        val bgColor = if (match.isPlayed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface

        Card(
            modifier = Modifier.fillMaxWidth().clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = bgColor),
            elevation = CardDefaults.cardElevation(defaultElevation = if (match.isPlayed) 1.dp else 3.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Etiqueta da Semana (S1, S2, etc.)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text("S${match.week}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Adversário e Localização
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (match.isHome) "VS ${match.opponentName}" else "@ ${match.opponentName}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (match.isHome) "Em Casa" else "Fora",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Resultado ou Status
                if (match.isPlayed) {
                    // Contas para saber se ganhámos, empatámos ou perdemos
                    val myGoals = if (match.isHome) match.homeGoals else match.awayGoals
                    val oppGoals = if (match.isHome) match.awayGoals else match.homeGoals

                    val resultColor = when {
                        myGoals!! > oppGoals!! -> Color(0xFF4CAF50) // Vitória (Verde)
                        myGoals < oppGoals -> Color.Red             // Derrota
                        else -> Color.Gray                          // Empate
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(resultColor.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${match.homeGoals} - ${match.awayGoals}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = resultColor
                        )
                    }
                } else {
                    Text("Por Jogar", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}