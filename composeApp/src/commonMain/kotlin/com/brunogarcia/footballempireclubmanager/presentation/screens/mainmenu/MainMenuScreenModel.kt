package com.brunogarcia.footballempireclubmanager.presentation.screens.mainmenu

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.brunogarcia.footballempireclubmanager.domain.model.InitialDataWrapper
import com.brunogarcia.footballempireclubmanager.domain.repository.GameRepository
import com.brunogarcia.footballempireclubmanager.domain.usecase.GenerateFixturesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class MainMenuScreenModel(
    private val repository: GameRepository,
    private val generateFixturesUseCase: GenerateFixturesUseCase
) : ScreenModel {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Função para ler o JSON e inicializar o jogo
    fun startNewGame(jsonString: String, onFinished: () -> Unit) {
        screenModelScope.launch {
            _isLoading.value = true

            try {
                // O JSON exige que ignoremos chaves desconhecidas por segurança
                val jsonParser = Json { ignoreUnknownKeys = true }

                // Converte a String gigante de JSON para as nossas Data Classes
                val initialData = jsonParser.decodeFromString<InitialDataWrapper>(jsonString)

                // Para o teste inicial, vamos ser o treinador das "Águias de Lisboa"
                val userClubId = initialData.clubs.first().id

                // Atira tudo para a memória
                repository.initializeGame(initialData.clubs, initialData.players, userClubId)

                // Gerar e guardar o calendário
                val fixtures = generateFixturesUseCase.execute(initialData.clubs)
                repository.saveFixtures(fixtures)

                // Avisa o Ecrã que já pode mudar de página
                onFinished()

            } catch (e: Exception) {
                println("Erro ao carregar a base de dados: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}