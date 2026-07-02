package com.brunogarcia.footballempireclubmanager.presentation.screens.mainmenu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.brunogarcia.footballempireclubmanager.presentation.screens.MainGameScreen
import com.brunogarcia.footballempireclubmanager.presentation.components.GlassCard
import com.brunogarcia.footballempireclubmanager.presentation.theme.MidnightBlue
import com.brunogarcia.footballempireclubmanager.presentation.theme.DarkNavy
import com.brunogarcia.footballempireclubmanager.presentation.theme.NeonGreen
import kotlinproject.composeapp.generated.resources.*
import kotlinx.coroutines.launch

import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

class MainMenuScreen : Screen {

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<MainMenuScreenModel>()

        val isLoading by screenModel.isLoading.collectAsState()
        val coroutineScope = rememberCoroutineScope()

        Scaffold { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(MidnightBlue, DarkNavy)))
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Logótipo / Título Principal
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "FOOTBALL EMPIRE",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "C L U B   M A N A G E R",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(56.dp))

                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "A inicializar a base de dados...", color = MaterialTheme.colorScheme.onBackground)
                    } else {
                        // Painel translúcido em Glassmorphism
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(0.5f)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Botão NOVO JOGO
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                             val jsonBytes = Res.readBytes("files/database_init.json")
                                             val jsonString = jsonBytes.decodeToString()

                                             val jsonParser = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                                             val initialData = jsonParser.decodeFromString<com.brunogarcia.footballempireclubmanager.domain.model.InitialDataWrapper>(jsonString)

                                             navigator.push(SelectTeamScreen(initialData))
                                         }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MidnightBlue
                                    ),
                                    shape = MaterialTheme.shapes.medium,
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    Text(
                                        text = stringResource(Res.string.btn_new_game).uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Botão CARREGAR JOGO
                                OutlinedButton(
                                    onClick = {
                                        screenModel.loadSavedGame {
                                            navigator.replaceAll(MainGameScreen())
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = screenModel.hasSavedGame(),
                                    shape = MaterialTheme.shapes.medium,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (screenModel.hasSavedGame()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
                                    ),
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    Text(
                                        text = "CARREGAR JOGO",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}