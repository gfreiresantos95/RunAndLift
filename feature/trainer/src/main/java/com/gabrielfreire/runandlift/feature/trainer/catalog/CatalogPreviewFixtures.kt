package com.gabrielfreire.runandlift.feature.trainer.catalog

import com.gabrielfreire.runandlift.data.model.Exercise
import com.gabrielfreire.runandlift.data.model.ExerciseCategory
import com.gabrielfreire.runandlift.data.model.ExerciseForce
import com.gabrielfreire.runandlift.data.model.ExerciseMechanic
import com.gabrielfreire.runandlift.data.model.TrainingLevel

/**
 * Exercícios de exemplo dos previews do catálogo.
 *
 * São exercícios reais da base importada, com o texto como ele chega — inclusive o nome que segue em
 * inglês, como "Crossover na Polia" ao lado de "Face Pull". É o que se quer conferir: a lista tem de
 * aguentar nomes longos, curtos e mistos sem quebrar o alinhamento.
 */

internal fun previewExercises(): List<Exercise> = listOf(
    Exercise(
        id = "Barbell_Bench_Press_Medium_Grip",
        name = "Supino Reto com Barra - Pegada Média",
        muscleGroups = listOf("Peitoral"),
        equipment = "Barra",
        instructions = listOf(
            "Deite-se no banco e segure a barra com pegada média, um pouco além da largura dos ombros.",
            "Retire a barra do suporte e estabilize acima do peito, com os braços estendidos.",
            "Desça controlando até a barra tocar de leve a parte média do peito.",
            "Empurre de volta à posição inicial, expirando na subida.",
        ),
        secondaryMuscleGroups = listOf("Ombros", "Tríceps"),
        level = TrainingLevel.INTERMEDIATE,
        mechanic = ExerciseMechanic.COMPOUND,
        force = ExerciseForce.PUSH,
        category = ExerciseCategory.STRENGTH,
    ),
    Exercise(
        id = "Cable_Crossover",
        name = "Crossover na Polia",
        muscleGroups = listOf("Peitoral"),
        equipment = "Polia",
        instructions = listOf(
            "Ajuste as polias acima da cabeça e segure as manoplas com os braços abertos.",
            "Dê um passo à frente e incline levemente o tronco.",
            "Junte as mãos à frente do corpo em arco, contraindo o peitoral.",
            "Volte devagar até sentir o alongamento.",
        ),
        secondaryMuscleGroups = listOf("Ombros"),
        level = TrainingLevel.BEGINNER,
        mechanic = ExerciseMechanic.ISOLATION,
        force = ExerciseForce.PUSH,
    ),
    Exercise(
        id = "Face_Pull",
        name = "Face Pull",
        muscleGroups = listOf("Ombros"),
        equipment = "Polia",
        instructions = listOf(
            "Ajuste a polia na altura do rosto e segure a corda com as duas mãos.",
            "Puxe em direção à testa, abrindo os cotovelos na linha dos ombros.",
            "Volte controlando, sem deixar o peso bater.",
        ),
        secondaryMuscleGroups = listOf("Meio das costas", "Trapézio"),
        level = TrainingLevel.INTERMEDIATE,
        mechanic = ExerciseMechanic.ISOLATION,
        force = ExerciseForce.PULL,
    ),
    // Sem equipamento e sem mecânica declarados: são 77 e 87 na base, e é o caso em que a linha de
    // apoio tem de se virar com menos do que costuma ter.
    Exercise(
        id = "Plank",
        name = "Prancha",
        muscleGroups = listOf("Abdômen"),
        equipment = null,
        instructions = listOf("Apoie antebraços e pontas dos pés.", "Mantenha o quadril na linha do tronco."),
        level = TrainingLevel.BEGINNER,
        force = ExerciseForce.STATIC,
    ),
)

internal fun previewCatalogState(): CatalogUiState = CatalogUiState(loading = false, results = previewExercises())

internal fun previewCatalogActions(): CatalogActions = CatalogActions(
    onQueryChange = {},
    onToggleCategory = {},
    onToggleMuscle = {},
    onToggleEquipment = {},
    onToggleLevel = {},
    onClearFilters = {},
    onSelect = {},
    onOpenDetail = {},
    onRetry = {},
)
