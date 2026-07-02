package com.brunogarcia.footballempireclubmanager.presentation.screens.leaguetable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.brunogarcia.footballempireclubmanager.presentation.components.GlassCard
import com.brunogarcia.footballempireclubmanager.presentation.theme.MidnightBlue
import com.brunogarcia.footballempireclubmanager.presentation.theme.DarkNavy
import com.brunogarcia.footballempireclubmanager.presentation.theme.NeonGreen
import com.brunogarcia.footballempireclubmanager.presentation.theme.NeonCyan

class LeagueTableScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = getScreenModel<LeagueTableScreenModel>()
        val table by screenModel.table.collectAsState()
        val selectedDivision by screenModel.selectedDivision.collectAsState()
        val userClubId = screenModel.getUserClubId()

        LaunchedEffect(Unit) { screenModel.initSelectedDivision() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(MidnightBlue, DarkNavy)))
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "CLASSIFICAÇÃO", 
                    style = MaterialTheme.typography.headlineSmall, 
                    fontWeight = FontWeight.Black,
                    color = NeonCyan,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Seletor de Divisão
                TabRow(
                    selectedTabIndex = selectedDivision - 1,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    containerColor = Color.Transparent,
                    contentColor = NeonCyan
                ) {
                    Tab(
                        selected = selectedDivision == 1,
                        onClick = { screenModel.selectDivision(1) },
                        text = { Text("1ª DIVISÃO", fontWeight = FontWeight.Bold) },
                        selectedContentColor = NeonCyan,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Tab(
                        selected = selectedDivision == 2,
                        onClick = { screenModel.selectDivision(2) },
                        text = { Text("2ª DIVISÃO", fontWeight = FontWeight.Bold) },
                        selectedContentColor = NeonCyan,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tabela em Vidro Fosco
                GlassCard(
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Cabeçalho da Tabela
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("#", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Black, color = NeonCyan, fontSize = 13.sp)
                            Text("CLUBE", modifier = Modifier.weight(1f), fontWeight = FontWeight.Black, color = NeonCyan, fontSize = 13.sp)
                            Text("J", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Black, color = NeonCyan, fontSize = 13.sp)
                            Text("DG", modifier = Modifier.width(40.dp), fontWeight = FontWeight.Black, color = NeonCyan, fontSize = 13.sp)
                            Text("PTS", modifier = Modifier.width(40.dp), fontWeight = FontWeight.Black, color = NeonCyan, fontSize = 13.sp)
                        }

                        HorizontalDivider(
                            color = com.brunogarcia.footballempireclubmanager.presentation.theme.GlassBorder.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            itemsIndexed(table) { index, entry ->
                                val isUserClub = entry.clubId == userClubId
                                val rowTextColor = if (isUserClub) NeonCyan else Color.White
                                val rowFontWeight = if (isUserClub) FontWeight.Black else FontWeight.Medium

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isUserClub) NeonCyan.copy(alpha = 0.12f) else Color.Transparent)
                                        .padding(vertical = 12.dp, horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = "${index + 1}", 
                                        modifier = Modifier.width(30.dp), 
                                        fontSize = 14.sp, 
                                        color = rowTextColor,
                                        fontWeight = rowFontWeight
                                    )
                                    Text(
                                        text = entry.clubName, 
                                        modifier = Modifier.weight(1f), 
                                        fontSize = 14.sp, 
                                        color = rowTextColor,
                                        fontWeight = rowFontWeight
                                    )
                                    Text(
                                        text = "${entry.played}", 
                                        modifier = Modifier.width(30.dp), 
                                        fontSize = 14.sp, 
                                        color = rowTextColor,
                                        fontWeight = rowFontWeight
                                    )
                                    Text(
                                        text = "${entry.goalDifference}", 
                                        modifier = Modifier.width(40.dp), 
                                        fontSize = 14.sp, 
                                        color = rowTextColor,
                                        fontWeight = rowFontWeight
                                    )
                                    Text(
                                        text = "${entry.points}", 
                                        modifier = Modifier.width(40.dp), 
                                        fontSize = 14.sp, 
                                        color = if (isUserClub) NeonCyan else NeonGreen,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                HorizontalDivider(
                                    color = com.brunogarcia.footballempireclubmanager.presentation.theme.GlassBorder.copy(alpha = 0.1f),
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}