package com.gabrielfreire.runandlift.feature.student.onboarding

import com.gabrielfreire.runandlift.data.model.HealthDataConsent
import com.gabrielfreire.runandlift.data.model.TrainingGoal
import com.gabrielfreire.runandlift.data.model.TrainingLevel
import com.gabrielfreire.runandlift.feature.student.fake.FakeAuthRepository
import com.gabrielfreire.runandlift.feature.student.fake.FakeStudentRepository
import com.gabrielfreire.runandlift.feature.student.fake.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek

/**
 * O passo a passo do primeiro acesso.
 *
 * O que os testes cobram são as três regras que o preview não mostra: **pular não grava**, o
 * consentimento **abre** os dois últimos passos, e sem aceite os campos de saúde não chegam ao
 * banco nem se estiverem preenchidos.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `comeca no primeiro passo, com quatro no total`() = runTest {
        val viewModel = viewModel()

        assertEquals(OnboardingStep.LEVEL, viewModel.uiState.value.step)
        assertEquals(1, viewModel.uiState.value.position)
        // Quatro, e não seis: peso e restrições ainda não existem como pergunta.
        assertEquals(OnboardingStep.ALWAYS_SHOWN.size, viewModel.uiState.value.total)
    }

    @Test
    fun `pular percorre a sequencia sem gravar resposta`() = runTest {
        val students = FakeStudentRepository()
        val viewModel = viewModel(students = students)

        repeat(OnboardingStep.ALWAYS_SHOWN.size) { viewModel.onStepDone(answered = false) }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.finished)
        assertEquals(1, students.saveCount)

        // Gravou o documento — é ele que marca "o onboarding aconteceu" — mas sem resposta nenhuma
        // dentro, porque nenhuma foi dada.
        val details = students.lastDetails
        assertNull(details?.level)
        assertNull(details?.goal)
        assertNull("dias pulados não podem virar 'nenhum dia' gravado", details?.availableDays)
        assertNull(details?.healthConsent)
    }

    @Test
    fun `consentimento acrescenta os dois passos de saude`() = runTest {
        val viewModel = viewModel()

        viewModel.onStepDone(answered = true)
        viewModel.onStepDone(answered = true)
        viewModel.onStepDone(answered = true)
        assertEquals(OnboardingStep.HEALTH_CONSENT, viewModel.uiState.value.step)

        viewModel.formActions.onHealthConsentChange(true)

        assertEquals(
            OnboardingStep.ALWAYS_SHOWN.size + OnboardingStep.BEHIND_CONSENT.size,
            viewModel.uiState.value.total,
        )
        assertFalse("com o aceite, o passo do consentimento deixa de ser o último", viewModel.uiState.value.isLast)
    }

    @Test
    fun `sem consentimento o fluxo termina no quarto passo`() = runTest {
        val students = FakeStudentRepository()
        val viewModel = viewModel(students = students)

        repeat(OnboardingStep.ALWAYS_SHOWN.size) { viewModel.onStepDone(answered = true) }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.finished)
        assertNull(students.lastDetails?.healthConsent)
    }

    @Test
    fun `respostas dadas chegam ao banco`() = runTest {
        val students = FakeStudentRepository()
        val viewModel = viewModel(students = students)

        viewModel.formActions.onLevelSelect(TrainingLevel.INTERMEDIATE)
        viewModel.onStepDone(answered = true)
        viewModel.formActions.onGoalSelect(TrainingGoal.HYPERTROPHY)
        viewModel.onStepDone(answered = true)
        viewModel.formActions.onDayToggle(DayOfWeek.MONDAY)
        viewModel.formActions.onDayToggle(DayOfWeek.WEDNESDAY)
        viewModel.onStepDone(answered = true)
        viewModel.formActions.onHealthConsentChange(true)
        viewModel.onStepDone(answered = true)
        viewModel.formActions.onWeightChange("72,5")
        viewModel.formActions.onHeightChange("175")
        viewModel.onStepDone(answered = true)
        viewModel.formActions.onRestrictionsChange("Ombro direito")
        viewModel.onStepDone(answered = true)
        advanceUntilIdle()

        val details = students.lastDetails
        assertEquals(TrainingLevel.INTERMEDIATE, details?.level)
        assertEquals(TrainingGoal.HYPERTROPHY, details?.goal)
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), details?.availableDays)
        assertEquals(72.5, details?.weightKg)
        assertEquals(175, details?.heightCm)
        assertEquals("Ombro direito", details?.restrictions)
        assertEquals(HealthDataConsent.CURRENT_VERSION, details?.healthConsent?.version)
    }

    @Test
    fun `retirar o consentimento apaga o que foi digitado`() = runTest {
        val viewModel = viewModel()

        viewModel.formActions.onHealthConsentChange(true)
        viewModel.formActions.onWeightChange("72,5")
        viewModel.formActions.onHeightChange("175")
        viewModel.formActions.onHealthConsentChange(false)

        // Dado sensível não fica em memória à espera de uma autorização que foi retirada.
        assertEquals("", viewModel.formState.value.weight)
        assertEquals("", viewModel.formState.value.height)
    }

    @Test
    fun `peso fora da faixa segura o passo`() = runTest {
        val viewModel = viewModel()

        viewModel.formActions.onHealthConsentChange(true)
        repeat(OnboardingStep.ALWAYS_SHOWN.size) { viewModel.onStepDone(answered = true) }
        assertEquals(OnboardingStep.MEASURES, viewModel.uiState.value.step)

        viewModel.formActions.onWeightChange("7")
        viewModel.onStepDone(answered = true)

        // Quem digitou 7 no lugar de 70 precisa ser avisado antes de o número virar o ponto de
        // partida do treino.
        assertEquals(OnboardingStep.MEASURES, viewModel.uiState.value.step)
        assertTrue(viewModel.formState.value.weightError != null)
    }

    @Test
    fun `voltar um passo preserva o que foi respondido`() = runTest {
        val viewModel = viewModel()

        viewModel.formActions.onLevelSelect(TrainingLevel.INTERMEDIATE)
        viewModel.onStepDone(answered = true)
        assertEquals(OnboardingStep.GOAL, viewModel.uiState.value.step)

        viewModel.onBack()

        // Quem volta quer corrigir, não recomeçar: encontrar o campo vazio faria a pessoa digitar
        // duas vezes o que ela só queria conferir.
        assertEquals(OnboardingStep.LEVEL, viewModel.uiState.value.step)
        assertEquals(TrainingLevel.INTERMEDIATE, viewModel.formState.value.level)
    }

    @Test
    fun `nao ha para onde voltar no primeiro passo`() = runTest {
        val viewModel = viewModel()

        assertFalse(viewModel.uiState.value.canGoBack)

        viewModel.onBack()

        assertEquals(OnboardingStep.LEVEL, viewModel.uiState.value.step)
        assertEquals(1, viewModel.uiState.value.position)
    }

    @Test
    fun `gravacao que falha nao prende, e avisa`() = runTest {
        val viewModel = viewModel(students = FakeStudentRepository(failWriting = true))

        repeat(OnboardingStep.ALWAYS_SHOWN.size) { viewModel.onStepDone(answered = true) }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.failed)
        assertFalse(viewModel.uiState.value.saving)
        // Não navega sozinho: o que não gravou reaparece no aviso da home, e a tela oferece tentar
        // de novo antes disso.
        assertFalse(viewModel.uiState.value.finished)
    }

    @Test
    fun `sem sessao encerra sem gravar`() = runTest {
        val students = FakeStudentRepository()
        val viewModel = viewModel(auth = FakeAuthRepository(signedIn = null), students = students)

        repeat(OnboardingStep.ALWAYS_SHOWN.size) { viewModel.onStepDone(answered = true) }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.finished)
        assertEquals(0, students.saveCount)
    }

    private fun viewModel(
        auth: FakeAuthRepository = FakeAuthRepository(),
        students: FakeStudentRepository = FakeStudentRepository(),
    ) = OnboardingViewModel(authRepository = auth, studentRepository = students)
}
