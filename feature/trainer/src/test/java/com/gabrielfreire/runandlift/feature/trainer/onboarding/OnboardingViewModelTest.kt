package com.gabrielfreire.runandlift.feature.trainer.onboarding

import com.gabrielfreire.runandlift.data.model.ServiceMode
import com.gabrielfreire.runandlift.data.model.ShowcaseConsent
import com.gabrielfreire.runandlift.data.model.TrainerExperience
import com.gabrielfreire.runandlift.data.model.TrainerSpecialty
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeAuthRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeTrainerRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.MainDispatcherRule
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
 * O passo a passo do primeiro acesso como treinador.
 *
 * O que os testes cobram são as quatro regras que o preview não mostra: **pular não grava**, o
 * aceite da vitrine **abre** os dois últimos passos, sem aceite a apresentação não chega ao banco
 * nem preenchida, e a conclusão **sempre** é carimbada — que é a única marca de que este fluxo
 * aconteceu, já que o documento existe desde o cadastro.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `comeca no primeiro passo, com cinco no total`() = runTest {
        val viewModel = viewModel()

        assertEquals(OnboardingStep.EXPERIENCE, viewModel.uiState.value.step)
        assertEquals(1, viewModel.uiState.value.position)
        // Cinco, e não sete: apresentação e capacidade ainda não existem como pergunta.
        assertEquals(OnboardingStep.ALWAYS_SHOWN.size, viewModel.uiState.value.total)
    }

    @Test
    fun `pular percorre a sequencia sem gravar resposta, mas carimba a conclusao`() = runTest {
        val trainers = FakeTrainerRepository()
        val viewModel = viewModel(trainers = trainers)

        repeat(OnboardingStep.ALWAYS_SHOWN.size) { viewModel.onStepDone(answered = false) }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.finished)
        assertEquals(1, trainers.saveCount)

        val details = trainers.lastDetails
        assertNull(details?.experience)
        assertNull("lista pulada não pode virar conjunto vazio gravado", details?.specialties)
        assertNull(details?.serviceModes)
        assertNull(details?.availableDays)
        assertNull("recusar a vitrine não grava nada — não há o que desligar", details?.showcase)

        // O carimbo é o que impede o fluxo de reabrir na próxima abertura, e é a única coisa que
        // esta gravação escreve quando tudo foi pulado.
        assertTrue(details?.onboardingDone == true)
    }

    @Test
    fun `aceite da vitrine acrescenta os dois passos publicados`() = runTest {
        val viewModel = viewModel()

        repeat(times = 4) { viewModel.onStepDone(answered = true) }
        assertEquals(OnboardingStep.SHOWCASE_CONSENT, viewModel.uiState.value.step)

        viewModel.formActions.onShowcaseChange(true)

        assertEquals(
            OnboardingStep.ALWAYS_SHOWN.size + OnboardingStep.BEHIND_CONSENT.size,
            viewModel.uiState.value.total,
        )
        assertFalse("com o aceite, o passo do consentimento deixa de ser o último", viewModel.uiState.value.isLast)
    }

    @Test
    fun `recusar a vitrine encerra o fluxo no quinto passo`() = runTest {
        val trainers = FakeTrainerRepository()
        val viewModel = viewModel(trainers = trainers)

        repeat(times = 4) { viewModel.onStepDone(answered = true) }
        viewModel.formActions.onShowcaseChange(true)
        viewModel.formActions.onShowcaseChange(false)

        assertEquals(OnboardingStep.ALWAYS_SHOWN.size, viewModel.uiState.value.total)
        assertTrue(viewModel.uiState.value.isLast)

        viewModel.onStepDone(answered = true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.finished)
    }

    @Test
    fun `o que foi respondido chega ao banco`() = runTest {
        val trainers = FakeTrainerRepository()
        val viewModel = viewModel(trainers = trainers)

        viewModel.formActions.onExperienceSelect(TrainerExperience.FIVE_TO_TEN_YEARS)
        viewModel.formActions.onSpecialtyToggle(TrainerSpecialty.RUNNING)
        viewModel.formActions.onServiceModeToggle(ServiceMode.ONLINE)
        viewModel.formActions.onDayToggle(DayOfWeek.TUESDAY)

        repeat(OnboardingStep.ALWAYS_SHOWN.size) { viewModel.onStepDone(answered = true) }
        advanceUntilIdle()

        val details = trainers.lastDetails
        assertEquals(TrainerExperience.FIVE_TO_TEN_YEARS, details?.experience)
        assertEquals(setOf(TrainerSpecialty.RUNNING), details?.specialties)
        assertEquals(setOf(ServiceMode.ONLINE), details?.serviceModes)
        assertEquals(setOf(DayOfWeek.TUESDAY), details?.availableDays)
    }

    @Test
    fun `sem aceite, apresentacao e capacidade nao chegam ao banco`() = runTest {
        val trainers = FakeTrainerRepository()
        val viewModel = viewModel(trainers = trainers)

        // Aceita, escreve os dois campos publicados, e volta atrás — o caso que a retirada do
        // consentimento precisa cobrir de verdade.
        viewModel.formActions.onShowcaseChange(true)
        viewModel.formActions.onBioChange("Atendo corredores")
        viewModel.formActions.onMaxStudentsChange("20")
        viewModel.formActions.onShowcaseChange(false)

        repeat(OnboardingStep.ALWAYS_SHOWN.size) { viewModel.onStepDone(answered = true) }
        advanceUntilIdle()

        assertNull(trainers.lastDetails?.bio)
        assertNull(trainers.lastDetails?.maxStudents)
        assertNull(trainers.lastDetails?.showcase)
    }

    @Test
    fun `aceite vai junto da gravacao final`() = runTest {
        val trainers = FakeTrainerRepository()
        val viewModel = viewModel(trainers = trainers)

        viewModel.formActions.onShowcaseChange(true)
        viewModel.formActions.onBioChange("Atendo corredores")
        viewModel.formActions.onMaxStudentsChange("20")

        repeat(OnboardingStep.ALWAYS_SHOWN.size + OnboardingStep.BEHIND_CONSENT.size) {
            viewModel.onStepDone(answered = true)
        }
        advanceUntilIdle()

        assertEquals(ShowcaseConsent(accepted = true), trainers.lastDetails?.showcase)
        assertEquals("Atendo corredores", trainers.lastDetails?.bio)
        assertEquals(20, trainers.lastDetails?.maxStudents)
    }

    @Test
    fun `capacidade invalida segura o passo, e pular passa por cima dela`() = runTest {
        val trainers = FakeTrainerRepository()
        val viewModel = viewModel(trainers = trainers)

        viewModel.formActions.onShowcaseChange(true)

        repeat(OnboardingStep.ALWAYS_SHOWN.size + 1) { viewModel.onStepDone(answered = true) }
        assertEquals(OnboardingStep.CAPACITY, viewModel.uiState.value.step)

        // Digitado no passo em que ele aparece: a régua vale para o formulário inteiro, então um
        // valor inválido escrito antes seguraria já o primeiro passo.
        viewModel.formActions.onMaxStudentsChange("0")
        viewModel.onStepDone(answered = true)
        advanceUntilIdle()

        assertFalse("com a capacidade inválida, o fluxo não termina", viewModel.uiState.value.finished)
        assertEquals(0, trainers.saveCount)

        // Pular não valida: quem desistiu do campo não fica preso nele.
        viewModel.onStepDone(answered = false)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.finished)
        assertEquals(1, trainers.saveCount)
    }

    @Test
    fun `voltar preserva o que ja foi respondido`() = runTest {
        val viewModel = viewModel()

        viewModel.formActions.onExperienceSelect(TrainerExperience.OVER_TEN_YEARS)
        viewModel.onStepDone(answered = true)
        viewModel.onBack()

        assertEquals(OnboardingStep.EXPERIENCE, viewModel.uiState.value.step)
        assertEquals(1, viewModel.uiState.value.position)
        assertEquals(TrainerExperience.OVER_TEN_YEARS, viewModel.formState.value.experience)
    }

    @Test
    fun `nao ha para onde voltar no primeiro passo`() = runTest {
        val viewModel = viewModel()

        assertFalse(viewModel.uiState.value.canGoBack)

        viewModel.onBack()

        assertEquals(1, viewModel.uiState.value.position)
    }

    @Test
    fun `gravacao que falha avisa e nao prende`() = runTest {
        val viewModel = viewModel(trainers = FakeTrainerRepository(failWriting = true))

        repeat(OnboardingStep.ALWAYS_SHOWN.size) { viewModel.onStepDone(answered = false) }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.failed)
        assertFalse(
            "falhar não pode empurrar ninguém para a home como se tivesse gravado",
            viewModel.uiState.value.finished,
        )
        assertFalse(viewModel.uiState.value.saving)
    }

    @Test
    fun `sem sessao o fluxo termina sem gravar`() = runTest {
        val trainers = FakeTrainerRepository()
        val viewModel = viewModel(auth = FakeAuthRepository(signedIn = null), trainers = trainers)

        repeat(OnboardingStep.ALWAYS_SHOWN.size) { viewModel.onStepDone(answered = false) }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.finished)
        assertEquals(0, trainers.saveCount)
    }

    private fun viewModel(
        auth: FakeAuthRepository = FakeAuthRepository(),
        trainers: FakeTrainerRepository = FakeTrainerRepository(),
    ) = OnboardingViewModel(authRepository = auth, trainerRepository = trainers)
}
