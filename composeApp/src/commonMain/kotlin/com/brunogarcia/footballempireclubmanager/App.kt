package com.brunogarcia.footballempireclubmanager

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.brunogarcia.footballempireclubmanager.di.appModule
import com.brunogarcia.footballempireclubmanager.presentation.screens.dashboard.DashboardScreen
import org.koin.compose.KoinApplication

@Composable
fun App() {
    MaterialTheme {
        // Inicializamos o Koin e damos-lhe a nossa lista de "receitas"
        KoinApplication(application = {
            modules(appModule)
        }) {
            // Inicializamos o Voyager para gerir os ecrãs
            // E dizemos que o primeiro ecrã é o Dashboard
            Navigator(DashboardScreen()) { navigator ->
                SlideTransition(navigator) // Animações de deslizar
            }
        }
    }
}