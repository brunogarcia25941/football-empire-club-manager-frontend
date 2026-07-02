package com.brunogarcia.footballempireclubmanager.presentation.screens.mainmenu

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.brunogarcia.footballempireclubmanager.domain.model.Club
import com.brunogarcia.footballempireclubmanager.domain.model.InitialDataWrapper
import com.brunogarcia.footballempireclubmanager.presentation.screens.MainGameScreen
import org.koin.core.parameter.parametersOf

/**
 * Ecrã de Seleção de Equipas. Apresenta uma lista dos clubes disponíveis
 * e abre um popup detalhado antes de confirmar a escolha.
 */
class SelectTeamScreen(private val initialData: InitialDataWrapper) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        
        // Inicializa o ScreenModel passando os dados iniciais do JSON por parâmetro
        val screenModel = getScreenModel<SelectTeamScreenModel> { parametersOf(initialData) }
        val isLoading by screenModel.isLoading.collectAsState()
        
        var selectedClub by remember { mutableStateOf<Club?>(null) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Escolher Clube", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { paddingValues ->
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(initialData.clubs) { club ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedClub = club },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Pequeno indicador circular com a cor primária do clube
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
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
                                        fontWeight = FontWeight.Bold
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
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Pop-up dialog com os detalhes da equipa selecionada
            selectedClub?.let { club ->
                AlertDialog(
                    onDismissRequest = { selectedClub = null },
                    confirmButton = {
                        Button(
                            onClick = {
                                selectedClub = null
                                screenModel.selectTeam(club.id) {
                                    // Limpa o fluxo e entra diretamente no Dashboard principal do jogo
                                    navigator.replaceAll(MainGameScreen())
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Iniciar Carreira", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { selectedClub = null },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancelar", color = MaterialTheme.colorScheme.error)
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
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Text(text = club.name, fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DetailRow("Orçamento:", formatBudget(club.budget))
                            DetailRow("Estádio:", "${club.stadiumCapacity} lugares")
                            DetailRow("Preço Bilhete:", "${club.ticketPrice.toInt()} €")
                            DetailRow("Reputação:", "${club.reputation}/100")
                            DetailRow("Academia Juniores:", "Nível ${club.youthAcademyLevel}/10")
                            DetailRow("Centro Treinos:", "Nível ${club.trainingFacilities}/10")
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
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }

    private fun formatBudget(budget: Double): String {
        return when {
            budget >= 1_000_000 -> "${(budget / 1_000_000).toInt()}M €"
            budget >= 1_000 -> "${(budget / 1_000).toInt()}k €"
            else -> "${budget.toInt()} €"
        }
    }

    // Função auxiliar para converter o formato Hexadecimal de cor da BD para compose Color
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
