package com.gabrielfreire.runandlift.feature.trainer.programeditor

import com.gabrielfreire.runandlift.data.model.PrescribedExercise
import com.gabrielfreire.runandlift.data.model.TrainingGoal
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeExerciseRepository.Companion.exercise
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeProgramRepository.Companion.program
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * O dono do rascunho aplicando ao estado as regras que moram em `ProgramEdits.kt`.
 *
 * As regras em si já têm o seu teste — o que se verifica aqui é o outro lado: que **cada mutação
 * chega ao `StateFlow`**. É a falha que ninguém vê acontecer, porque um `update` esquecido não
 * quebra nada: a tela simplesmente não muda ao tocar, e isso passa por "não funcionou o toque".
 *
 * O caso do objetivo é o único que decide alguma coisa aqui dentro, e por isso é o mais detalhado:
 * tocar no chip já marcado o desmarca, que é a única saída de quem escolheu errado no primeiro toque.
 */
class ProgramDraftControllerTest {

    private val controller = ProgramDraftController()

    @Test
    fun `o rascunho nasce vazio`() {
        assertEquals("", controller.draft.value.name)
        assertEquals("", controller.draft.value.id)
        assertNull(controller.draft.value.goal)
    }

    @Test
    fun `escrever em current substitui tudo`() {
        // É o que o ViewModel faz ao terminar de ler do Firestore e ao receber o programa gravado.
        controller.current = program()

        assertEquals("Treino ABC", controller.draft.value.name)
        assertEquals("p1", controller.current.id)
    }

    @Test
    fun `nome e observacoes vao para o rascunho`() {
        controller.onNameChange("Treino ABC")
        controller.onNotesChange("Aquecer 10 min")

        assertEquals("Treino ABC", controller.draft.value.name)
        assertEquals("Aquecer 10 min", controller.draft.value.notes)
    }

    @Test
    fun `tocar no objetivo ja escolhido o desmarca`() {
        controller.onGoalChange(TrainingGoal.HYPERTROPHY)
        assertEquals(TrainingGoal.HYPERTROPHY, controller.draft.value.goal)

        controller.onGoalChange(TrainingGoal.HYPERTROPHY)
        assertNull(
            "sem essa saída, um chip marcado por engano não teria como ser desfeito",
            controller.draft.value.goal,
        )
    }

    @Test
    fun `tocar em outro objetivo troca em vez de desmarcar`() {
        controller.onGoalChange(TrainingGoal.HYPERTROPHY)
        controller.onGoalChange(TrainingGoal.WEIGHT_LOSS)

        assertEquals(TrainingGoal.WEIGHT_LOSS, controller.draft.value.goal)
    }

    @Test
    fun `dia entra, muda de rotulo e sai`() {
        controller.onAddDay()
        controller.onDayInfoChange(dayIndex = 0, label = "Push", focus = "Peito e tríceps")

        assertEquals("Push", controller.draft.value.days.single().label)
        assertEquals("Peito e tríceps", controller.draft.value.days.single().focus)

        controller.onRemoveDay(0)

        assertEquals(emptyList<String>(), controller.draft.value.days.map { it.label })
    }

    @Test
    fun `exercicio entra no dia, troca de ordem e sai`() {
        controller.onAddDay()
        controller.onAddExercise(dayIndex = 0, exercise = exercise("supino"))
        controller.onAddExercise(dayIndex = 0, exercise = exercise("agachamento"))

        assertEquals(listOf("supino", "agachamento"), exerciseIds())

        controller.onMoveExercise(dayIndex = 0, from = 1, to = 0)

        assertEquals(listOf("agachamento", "supino"), exerciseIds())

        controller.onRemoveExercise(dayIndex = 0, exerciseIndex = 0)

        assertEquals(listOf("supino"), exerciseIds())
    }

    @Test
    fun `a prescricao ajustada volta para o rascunho`() {
        controller.onAddDay()
        controller.onAddExercise(dayIndex = 0, exercise = exercise("supino"))

        val ajustada = controller.draft.value.days.first().exercises.first().copy(sets = 5, loadKg = 60.0)

        controller.onPrescriptionChange(dayIndex = 0, exerciseIndex = 0, prescription = ajustada)

        val gravada = controller.draft.value.days.first().exercises.first()

        assertEquals(5, gravada.sets)
        assertEquals(60.0, gravada.loadKg!!, 0.0)
    }

    @Test
    fun `exercicio recem-escolhido chega com a prescricao padrao`() {
        controller.onAddDay()
        controller.onAddExercise(dayIndex = 0, exercise = exercise("supino"))

        val prescrita = controller.draft.value.days.first().exercises.first()

        assertEquals(PrescribedExercise.DEFAULT_SETS, prescrita.sets)
        assertEquals("Supino", prescrita.exerciseName)
    }

    private fun exerciseIds() = controller.draft.value.days.first().exercises.map { it.exerciseId }
}
