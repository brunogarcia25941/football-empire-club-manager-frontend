package com.brunogarcia.footballempireclubmanager.presentation.screens.mainmenu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.brunogarcia.footballempireclubmanager.presentation.screens.dashboard.DashboardScreen
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título do Jogo (Usando a tradução!)
                Text(
                    text = stringResource(Res.string.app_name),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(64.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "A inicializar a base de dados...")
                } else {
                    // Botão NOVO JOGO
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                // 1. Lê o ficheiro da pasta composeResources/files/
                                val jsonBytes = Res.readBytes("files/database_init.json")
                                val jsonString = jsonBytes.decodeToString()

                                // 2. Manda para o cérebro processar e, no fim, vai para o Dashboard!
                                screenModel.startNewGame(jsonString) {
                                    // Limpa o Menu Principal e mete o Dashboard como ecrã inicial
                                    navigator.replaceAll(DashboardScreen())
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.6f),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.btn_new_game),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botão CARREGAR JOGO (Desativado para já)
                    OutlinedButton(
                        onClick = { /* TODO no futuro */ },
                        modifier = Modifier.fillMaxWidth(0.6f),
                        enabled = false
                    ) {
                        Text(text = "Carregar Jogo")
                    }
                }
            }
        }
    }
}