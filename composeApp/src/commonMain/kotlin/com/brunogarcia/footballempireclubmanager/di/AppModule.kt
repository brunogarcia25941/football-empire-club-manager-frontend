package com.brunogarcia.footballempireclubmanager.di

import com.brunogarcia.footballempireclubmanager.data.repository.GameRepositoryImpl
import com.brunogarcia.footballempireclubmanager.domain.repository.GameRepository
import com.brunogarcia.footballempireclubmanager.domain.usecase.*
import com.brunogarcia.footballempireclubmanager.presentation.screens.dashboard.DashboardScreenModel
import com.brunogarcia.footballempireclubmanager.presentation.screens.leaguetable.LeagueTableScreenModel
import com.brunogarcia.footballempireclubmanager.presentation.screens.mainmenu.MainMenuScreenModel
import com.brunogarcia.footballempireclubmanager.presentation.screens.squad.SquadScreenModel
import com.brunogarcia.footballempireclubmanager.presentation.screens.tactics.TacticsScreenModel
import org.koin.dsl.module

val appModule = module {
    // 1. O nosso Repositório (Existe apenas uma cópia para o jogo todo -> single)
    single<GameRepository> { GameRepositoryImpl() }

    // 2. Os nossos Casos de Uso (factory cria um novo sempre que for preciso)
    factory { SimulateMatchweekUseCase() }
    factory { ProcessWeeklyUpdatesUseCase() }
    factory { GenerateFixturesUseCase() }
    factory { CalculateLeagueTableUseCase() }

    // O AdvanceTime precisa do Repositório e dos outros dois UseCases.
    // O "get()" diz ao Koin: "Procura aqui na lista em cima e injeta automaticamente!"
    factory { AdvanceTimeUseCase(get(), get(), get()) }

    // 3. Os Ecrãs
    factory { MainMenuScreenModel(get(), get()) }
    factory { DashboardScreenModel(get(), get()) }
    factory { SquadScreenModel(get()) }
    factory { TacticsScreenModel(get()) }
    factory { LeagueTableScreenModel(get(), get()) }
}