package com.brunogarcia.footballempireclubmanager.presentation.screens.fixtures

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.brunogarcia.footballempireclubmanager.presentation.screens.matchreport.MatchReportScreen
import androidx.compose.foundation.clickable
import com.brunogarcia.footballempireclubmanager.presentation.theme.MidnightBlue
import com.brunogarcia.footballempireclubmanager.presentation.theme.DarkNavy
import com.brunogarcia.footballempireclubmanager.presentation.theme.NeonGreen
import com.brunogarcia.footballempireclubmanager.presentation.theme.NeonCyan
import com.brunogarcia.footballempireclubmanager.presentation.theme.AlertRed

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
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "CALENDÁRIO DE JOGOS",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontSize = 18.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkNavy,
                        titleContentColor = NeonCyan
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(MidnightBlue, DarkNavy)))
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(matches) { match ->
                        MatchItemCard(match, onClick = {
                            if (match.isPlayed) {
                                // Abre o relatório passando os IDs das equipas e se é jogo da Taça
                                navigator.push(MatchReportScreen(match.homeClubId, match.awayClubId, match.isCup))
                            }
                        })
                    }
                }
            }
        }
    }

    @Composable
    private fun MatchItemCard(match: MatchDisplayItem, onClick: () -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = com.brunogarcia.footballempireclubmanager.presentation.theme.GlassSurface
            ),
            border = BorderStroke(
                1.dp,
                if (match.isCup) NeonCyan.copy(alpha = 0.4f) else com.brunogarcia.footballempireclubmanager.presentation.theme.GlassBorder.copy(alpha = 0.15f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Etiqueta da Semana (S1, S2, etc.)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "J${match.week}", 
                        fontWeight = FontWeight.Black, 
                        color = NeonCyan,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Adversário e Localização
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (match.isHome) "VS ${match.opponentName.uppercase()}" else "@ ${match.opponentName.uppercase()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (match.isHome) "Em Casa" else "Fora de Casa",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (match.isCup) "• TAÇA" else "• LIGA",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = if (match.isCup) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Resultado ou Status
                if (match.isPlayed) {
                    val myGoals = if (match.isHome) match.homeGoals else match.awayGoals
                    val oppGoals = if (match.isHome) match.awayGoals else match.homeGoals

                    val (resultColor, resultBg) = when {
                        myGoals!! > oppGoals!! -> Pair(NeonGreen, NeonGreen.copy(alpha = 0.15f))
                        myGoals < oppGoals -> Pair(AlertRed, AlertRed.copy(alpha = 0.15f))
                        else -> Pair(Color.Gray, Color.Gray.copy(alpha = 0.15f))
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(resultBg)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${match.homeGoals} - ${match.awayGoals}",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = resultColor
                        )
                    }
                } else {
                    Text(
                        text = "POR JOGAR", 
                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}