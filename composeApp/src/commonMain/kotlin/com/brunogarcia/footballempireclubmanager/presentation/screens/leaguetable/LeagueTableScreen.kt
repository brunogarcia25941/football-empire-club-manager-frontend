package com.brunogarcia.footballempireclubmanager.presentation.screens.leaguetable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel

class LeagueTableScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = getScreenModel<LeagueTableScreenModel>()
        val table by screenModel.table.collectAsState()

        LaunchedEffect(Unit) { screenModel.refreshTable() }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Classificação", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Cabeçalho da Tabela
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text("#", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Bold)
                Text("Clube", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("J", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Bold)
                Text("DG", modifier = Modifier.width(40.dp), fontWeight = FontWeight.Bold)
                Text("Pts", modifier = Modifier.width(40.dp), fontWeight = FontWeight.Bold)
            }

            Divider()

            LazyColumn {
                itemsIndexed(table) { index, entry ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                        Text("${index + 1}", modifier = Modifier.width(30.dp), fontSize = 14.sp)
                        Text(entry.clubName, modifier = Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("${entry.played}", modifier = Modifier.width(30.dp), fontSize = 14.sp)
                        Text("${entry.goalDifference}", modifier = Modifier.width(40.dp), fontSize = 14.sp)
                        Text("${entry.points}", modifier = Modifier.width(40.dp), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Divider(thickness = 0.5.dp)
                }
            }
        }
    }
}