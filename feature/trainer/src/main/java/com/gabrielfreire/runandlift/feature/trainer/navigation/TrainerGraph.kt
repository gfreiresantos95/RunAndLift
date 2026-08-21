package com.gabrielfreire.runandlift.feature.trainer.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.gabrielfreire.runandlift.feature.trainer.account.AccountDestination
import com.gabrielfreire.runandlift.feature.trainer.assign.AssignDestination
import com.gabrielfreire.runandlift.feature.trainer.catalog.CatalogDestination
import com.gabrielfreire.runandlift.feature.trainer.catalog.ExerciseDetailDestination
import com.gabrielfreire.runandlift.feature.trainer.catalog.popWithPickedExercise
import com.gabrielfreire.runandlift.feature.trainer.home.TrainerHomeDestination
import com.gabrielfreire.runandlift.feature.trainer.invite.InviteDestination
import com.gabrielfreire.runandlift.feature.trainer.location.LocationPickerDestination
import com.gabrielfreire.runandlift.feature.trainer.menu.TrainerMenuDestination
import com.gabrielfreire.runandlift.feature.trainer.onboarding.OnboardingDestination
import com.gabrielfreire.runandlift.feature.trainer.profile.TrainerProfileDestination
import com.gabrielfreire.runandlift.feature.trainer.programeditor.DayEditorDestination
import com.gabrielfreire.runandlift.feature.trainer.programeditor.PrescriptionDestination
import com.gabrielfreire.runandlift.feature.trainer.programeditor.ProgramEditorDestination
import com.gabrielfreire.runandlift.feature.trainer.programs.ProgramsDestination
import com.gabrielfreire.runandlift.feature.trainer.students.StudentsDestination

/**
 * Grafo do treinador: as três abas, mais o passo a passo, o perfil profissional e os dados
 * cadastrais.
 *
 * Irmão do grafo do aluno e sem nenhuma rota em comum com ele — o que antes era disciplina de
 * nomenclatura agora é fronteira de módulo: este arquivo não consegue nomear uma tela de aluno nem
 * por engano, porque não a enxerga.
 *
 * O passo a passo **substitui a home na pilha** ao terminar: voltar para um fluxo já concluído não
 * faz sentido, e o botão voltar do aparelho o levaria de volta ao primeiro passo.
 *
 * Este arquivo é só o mapa. A ligação de cada destino com o seu ViewModel mora no pacote da tela.
 *
 * @param onSignedOut para onde ir quando a sessão terminar. Quem sabe é `:app`.
 * @param onSwitchRole `null` quando a conta não tem o papel de aluno.
 */
fun NavGraphBuilder.trainerGraph(
    navController: NavHostController,
    dependencies: TrainerDependencies,
    onSignedOut: () -> Unit,
    onSwitchRole: (() -> Unit)?,
) {
    navigation(startDestination = TrainerRoutes.HOME, route = TrainerRoutes.GRAPH) {
        trainerTabs(
            navController = navController,
            dependencies = dependencies,
            onSignedOut = onSignedOut,
            onSwitchRole = onSwitchRole,
        )
        trainerFlows(navController = navController, dependencies = dependencies)
        programBuilding(navController = navController, dependencies = dependencies)
    }
}

/**
 * A montagem de treino: programa, dia, prescrição e catálogo.
 *
 * Separada dos outros fluxos porque **as três primeiras compartilham um ViewModel** — o do editor de
 * programa, que fica vivo na pilha enquanto as outras são empilhadas por cima. É o que permite montar
 * um treino inteiro sem tocar a rede, e é a única parte do grafo em que a ordem de empilhamento é
 * uma decisão técnica e não só de navegação. Ver `sharedProgramEditorViewModel`.
 *
 * O catálogo fica **fora** desse compartilhamento, de propósito: ele não conhece programa nem dia, e
 * devolve o escolhido pela entrada anterior da pilha — a mesma mecânica das listas de estado e
 * cidade. É o que o deixa servir também à navegação livre, sem montagem nenhuma em curso.
 */
private fun NavGraphBuilder.programBuilding(navController: NavHostController, dependencies: TrainerDependencies) {
    composable(route = TrainerRoutes.PROGRAM_EDITOR_PATTERN, arguments = programArguments()) { entry ->
        ProgramEditorDestination(
            navController = navController,
            entry = entry,
            dependencies = dependencies,
            onBack = { navController.popBackStack() },
        )
    }
    composable(route = TrainerRoutes.DAY_EDITOR_PATTERN, arguments = dayArguments()) { entry ->
        DayEditorDestination(
            navController = navController,
            entry = entry,
            dependencies = dependencies,
            onBack = { navController.popBackStack() },
        )
    }
    composable(route = TrainerRoutes.PRESCRIPTION_PATTERN, arguments = prescriptionArguments()) { entry ->
        PrescriptionDestination(
            navController = navController,
            entry = entry,
            dependencies = dependencies,
            onBack = { navController.popBackStack() },
        )
    }
    composable(route = TrainerRoutes.ASSIGN_PATTERN, arguments = programArguments()) { entry ->
        AssignDestination(
            dependencies = dependencies,
            programId = entry.arguments?.getString(TrainerRoutes.PROGRAM_ID_ARG).orEmpty(),
            onBack = { navController.popBackStack() },
        )
    }
    composable(TrainerRoutes.CATALOG) {
        CatalogDestination(
            dependencies = dependencies,
            onSelect = { exerciseId -> navController.popWithPickedExercise(exerciseId) },
            onOpenDetail = { exerciseId ->
                navController.navigate(TrainerRoutes.exerciseDetail(exerciseId))
            },
            onBack = { navController.popBackStack() },
        )
    }
    composable(route = TrainerRoutes.EXERCISE_DETAIL_PATTERN, arguments = exerciseArguments()) { entry ->
        ExerciseDetailDestination(
            dependencies = dependencies,
            exerciseId = entry.arguments?.getString(TrainerRoutes.EXERCISE_ID_ARG).orEmpty(),
            onBack = { navController.popBackStack() },
        )
    }
}

private fun programArguments() = listOf(
    navArgument(TrainerRoutes.PROGRAM_ID_ARG) { type = NavType.StringType },
)

/**
 * Os índices vão como **inteiro**, e não como texto.
 *
 * O Navigation converte e recusa o que não for número, então uma rota malformada vira erro de
 * navegação em vez de um `toInt()` estourando dentro do composable — onde derrubaria a tela.
 */
private fun dayArguments() = programArguments() + navArgument(TrainerRoutes.DAY_INDEX_ARG) {
    type = NavType.IntType
}

private fun prescriptionArguments() = dayArguments() + navArgument(TrainerRoutes.EXERCISE_INDEX_ARG) {
    type = NavType.IntType
}

private fun exerciseArguments() = listOf(
    navArgument(TrainerRoutes.EXERCISE_ID_ARG) { type = NavType.StringType },
)

/**
 * As quatro abas, que são irmãs e ficam sempre no mesmo nível da pilha.
 *
 * Separadas dos fluxos por tamanho, e o corte não é arbitrário: **aba não é fluxo**. O que está aqui
 * a barra inferior alcança de qualquer lugar; o que está em [trainerFlows] se abre, se resolve e se
 * fecha.
 */
private fun NavGraphBuilder.trainerTabs(
    navController: NavHostController,
    dependencies: TrainerDependencies,
    onSignedOut: () -> Unit,
    onSwitchRole: (() -> Unit)?,
) {
    composable(TrainerRoutes.HOME) {
        TrainerHomeDestination(
            navController = navController,
            dependencies = dependencies,
            onOpenProfile = { navController.navigate(TrainerRoutes.PROFILE) },
        )
    }
    composable(TrainerRoutes.STUDENTS) {
        StudentsDestination(
            navController = navController,
            dependencies = dependencies,
            onOpenInvite = { navController.navigate(TrainerRoutes.INVITE) },
        )
    }
    composable(TrainerRoutes.WORKOUTS) {
        ProgramsDestination(
            navController = navController,
            dependencies = dependencies,
            onCreate = { navController.navigate(TrainerRoutes.programEditor()) },
            onOpen = { programId -> navController.navigate(TrainerRoutes.programEditor(programId)) },
        )
    }
    composable(TrainerRoutes.MENU) {
        TrainerMenuDestination(
            navController = navController,
            dependencies = dependencies,
            onSignedOut = onSignedOut,
            onSwitchRole = onSwitchRole,
            onOpen = { route -> navController.navigate(route) },
        )
    }
}

/** As telas com começo e fim: convite, dados cadastrais, localidade, passo a passo e perfil. */
private fun NavGraphBuilder.trainerFlows(navController: NavHostController, dependencies: TrainerDependencies) {
    composable(TrainerRoutes.INVITE) {
        InviteDestination(dependencies = dependencies, onBack = { navController.popBackStack() })
    }
    composable(TrainerRoutes.ACCOUNT) { entry ->
        AccountDestination(
            navController = navController,
            entry = entry,
            dependencies = dependencies,
            // Salvar volta levando o recibo; a seta volta sem ele, porque nada foi gravado.
            onSaved = { navController.popWithSavedResult() },
            onBack = { navController.popBackStack() },
        )
    }

    // As duas listas de localidade, abertas por "Meus dados". Ficam no mesmo grafo porque a
    // escolha volta pela entrada anterior da pilha — quem as abre é uma tela daqui.
    composable(TrainerRoutes.STATE_PICKER) {
        LocationPickerDestination(navController = navController, dependencies = dependencies, uf = null)
    }
    composable(route = TrainerRoutes.CITY_PICKER_PATTERN, arguments = ufArgument()) { entry ->
        LocationPickerDestination(
            navController = navController,
            dependencies = dependencies,
            uf = entry.arguments?.getString(TrainerRoutes.UF_ARG),
        )
    }
    composable(TrainerRoutes.ONBOARDING) {
        OnboardingDestination(
            dependencies = dependencies,
            onFinished = {
                navController.navigate(TrainerRoutes.HOME) {
                    popUpTo(TrainerRoutes.ONBOARDING) { inclusive = true }
                }
            },
        )
    }
    composable(TrainerRoutes.PROFILE) {
        TrainerProfileDestination(
            dependencies = dependencies,
            onSaved = { navController.popWithSavedResult() },
            onBack = { navController.popBackStack() },
        )
    }
}
