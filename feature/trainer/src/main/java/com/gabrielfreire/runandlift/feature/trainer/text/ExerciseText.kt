package com.gabrielfreire.runandlift.feature.trainer.text

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.data.model.ExerciseCategory
import com.gabrielfreire.runandlift.data.model.ExerciseForce
import com.gabrielfreire.runandlift.data.model.ExerciseMechanic
import com.gabrielfreire.runandlift.data.model.TrainingLevel
import com.gabrielfreire.runandlift.feature.trainer.R

/*
 * Os enums do catálogo virando texto de tela.
 *
 * Moram aqui, e não junto do enum, porque `:data` não tem `strings.xml` nem Compose — é a mesma
 * exceção documentada de `AuthFailure.message()` e de `ActiveRole`: a tradução fica no pacote mais
 * próximo do enum que pode conhecer o `R`.
 *
 * O que **não** está aqui é músculo e equipamento: aqueles são texto livre em português, gravados já
 * traduzidos pelo importador do catálogo, justamente porque a busca do SQLite roda sobre eles.
 */

/** "Iniciante", "Intermediário", "Avançado" — o mesmo texto que o aluno vê no perfil dele. */
@Composable
internal fun TrainingLevel.label(): String = stringResource(
    when (this) {
        TrainingLevel.BEGINNER -> R.string.trainer_exercise_level_beginner
        TrainingLevel.INTERMEDIATE -> R.string.trainer_exercise_level_intermediate
        TrainingLevel.ADVANCED -> R.string.trainer_exercise_level_advanced
    },
)

/** "Composto" ou "Isolado". É o que decide a ordem dos exercícios dentro de um dia. */
@Composable
internal fun ExerciseMechanic.label(): String = stringResource(
    when (this) {
        ExerciseMechanic.COMPOUND -> R.string.trainer_exercise_mechanic_compound
        ExerciseMechanic.ISOLATION -> R.string.trainer_exercise_mechanic_isolation
    },
)

/** "Empurrar", "Puxar", "Isometria" — o que nomeia a divisão de treino. */
@Composable
internal fun ExerciseForce.label(): String = stringResource(
    when (this) {
        ExerciseForce.PUSH -> R.string.trainer_exercise_force_push
        ExerciseForce.PULL -> R.string.trainer_exercise_force_pull
        ExerciseForce.STATIC -> R.string.trainer_exercise_force_static
    },
)

/** A família do exercício, como aparece no filtro do catálogo. */
@Composable
internal fun ExerciseCategory.label(): String = stringResource(categoryRes)

/**
 * O recurso da categoria, sem `@Composable`.
 *
 * Existe separado de [label] porque o filtro precisa montar a lista de opções **fora** de uma
 * composição para poder ser testado — e um `when` repetido nos dois lugares é a forma mais fácil de
 * um deles ficar para trás quando a oitava categoria aparecer.
 */
internal val ExerciseCategory.categoryRes: Int
    @StringRes get() = when (this) {
        ExerciseCategory.STRENGTH -> R.string.trainer_exercise_category_strength
        ExerciseCategory.STRETCHING -> R.string.trainer_exercise_category_stretching
        ExerciseCategory.PLYOMETRICS -> R.string.trainer_exercise_category_plyometrics
        ExerciseCategory.POWERLIFTING -> R.string.trainer_exercise_category_powerlifting
        ExerciseCategory.OLYMPIC_WEIGHTLIFTING -> R.string.trainer_exercise_category_olympic
        ExerciseCategory.STRONGMAN -> R.string.trainer_exercise_category_strongman
        ExerciseCategory.CARDIO -> R.string.trainer_exercise_category_cardio
    }
