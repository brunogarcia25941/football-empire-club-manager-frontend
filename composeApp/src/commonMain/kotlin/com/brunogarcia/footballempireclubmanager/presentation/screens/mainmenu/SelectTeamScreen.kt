package com.brunogarcia.footballempireclubmanager.presentation.screens.mainmenu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.brunogarcia.footballempireclubmanager.domain.model.Club
import com.brunogarcia.footballempireclubmanager.domain.model.InitialDataWrapper
import com.brunogarcia.footballempireclubmanager.presentation.screens.MainGameScreen
import com.brunogarcia.footballempireclubmanager.presentation.theme.*
import org.koin.core.parameter.parametersOf

class SelectTeamScreen(private val initialData: InitialDataWrapper) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        
        val screenModel = getScreenModel<SelectTeamScreenModel> { parametersOf(initialData) }
        val isLoading by screenModel.isLoading.collectAsState()
        
        var selectedClub by remember { mutableStateOf<Club?>(null) }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = "ESCOLHER CLUBE", 
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontSize = 18.sp
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = NeonCyan)
                        }
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
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NeonCyan)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(initialData.clubs) { club ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedClub = club },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = GlassSurface
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    GlassBorder.copy(alpha = 0.15f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(club.primaryColor.toComposeColor())
                                    )
                                    
                                    Spacer(modifier = Modifier.width(16.dp))
                                    
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = club.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Reputação: ${club.reputation}/100",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = formatBudget(club.budget),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = NeonGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }

            selectedClub?.let { club ->
                AlertDialog(
                    onDismissRequest = { selectedClub = null },
                    containerColor = DarkNavy,
                    confirmButton = {
                        Button(
                            onClick = {
                                selectedClub = null
                                screenModel.selectTeam(club.id) {
                                    navigator.replaceAll(MainGameScreen())
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = MidnightBlue
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("INICIAR CARREIRA", fontWeight = FontWeight.Black)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { selectedClub = null },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.textButtonColors(contentColor = AlertRed)
                        ) {
                            Text("CANCELAR", fontWeight = FontWeight.Bold)
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(club.primaryColor.toComposeColor()),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = club.name.take(2).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Text(text = club.name, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HorizontalDivider(color = GlassBorder.copy(alpha = 0.2f))
                            DetailRow("Orçamento do Clube:", formatBudget(club.budget))
                            DetailRow("Estádio:", "${club.stadiumCapacity} lugares")
                            DetailRow("Preço de Bilheteira:", "${club.ticketPrice.toInt()} €")
                            DetailRow("Reputação de Estrelas:", "${club.reputation}/100")
                            DetailRow("Academia de Juniores:", "Nível ${club.youthAcademyLevel}/10")
                            DetailRow("Centro de Treinos:", "Nível ${club.trainingFacilities}/10")
                        }
                    }
                )
            }
        }
    }

    @Composable
    private fun DetailRow(label: String, value: String) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }

    private fun formatBudget(budget: Double): String {
        return when {
            budget >= 1_000_000 -> "${(budget / 1_000_000).toInt()}M €"
            budget >= 1_000 -> "${(budget / 1_000).toInt()}k €"
            else -> "${budget.toInt()} €"
        }
    }

    private fun String.toComposeColor(): Color {
        return try {
            val hex = this.removePrefix("#")
            val argb = when (hex.length) {
                6 -> "FF$hex".toLong(16)
                8 -> hex.toLong(16)
                else -> 0xFFFFFFFF
            }
            Color(argb)
        } catch (e: Exception) {
            Color.Gray
        }
    }
}
