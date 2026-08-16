package com.gabrielfreire.runandlift.feature.trainer.profile

import com.gabrielfreire.runandlift.data.model.ShowcaseConsent
import com.gabrielfreire.runandlift.data.model.TrainerExperience
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeAuthRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeTrainerRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeUserRepository
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

/**
 * A edição do perfil profissional.
 *
 * Três coisas separam esta tela do passo a passo, e são elas que os testes cobram: o formulário
 * **vem preenchido**, a lista vazia **é gravada** porque a pergunta está à vista, e o aceite da
 * vitrine só viaja quando **muda** — reenviá-lo carimbaria uma data de consentimento nova a cada
 * toque em salvar.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TrainerProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `abre com o que ja esta gravado`() = runTest {
        val viewModel = viewModel(trainers = FakeTrainerRepository(stored = FakeTrainerRepository.complete()))

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.loading)
        assertEquals("Carlos Pereira", viewModel.uiState.value.name)
        assertEquals(FakeTrainerRepository.CREF, viewModel.uiState.value.cref)
        assertEquals(TrainerExperience.TWO_TO_FIVE_YEARS, viewModel.formState.value.experience)
        assertTrue(viewModel.formState.value.showcase)
    }

    @Test
    fun `abre dizendo o que falta, como o aviso da home dizia`() = runTest {
        val viewModel = viewModel()

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.missing.specialties)
        assertTrue(viewModel.uiState.value.missing.showcase)
    }

    @Test
    fun `salvar grava as listas mesmo vazias`() = runTest {
        val trainers = FakeTrainerRepository()
        val viewModel = viewModel(trainers = trainers)
        advanceUntilIdle()

        viewModel.onSubmit()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.saved)
        assertEquals(emptySet<Any>(), trainers.lastDetails?.specialties)
        assertEquals(emptySet<Any>(), trainers.lastDetails?.availableDays)
    }

    @Test
    fun `salvar sem mexer na vitrine nao reenvia o aceite`() = runTest {
        val trainers = FakeTrainerRepository(stored = FakeTrainerRepository.complete())
        val viewModel = viewModel(trainers = trainers)
        advanceUntilIdle()

        viewModel.onSubmit()
        advanceUntilIdle()

        assertNull("reenviar o aceite apagaria quando ele de fato aconteceu", trainers.lastDetails?.showcase)
    }

    @Test
    fun `desmarcar a vitrine tira o perfil do ar`() = runTest {
        val trainers = FakeTrainerRepository(stored = FakeTrainerRepository.complete())
        val viewModel = viewModel(trainers = trainers)
        advanceUntilIdle()

        viewModel.formActions.onShowcaseChange(false)
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(ShowcaseConsent(accepted = false), trainers.lastDetails?.showcase)
    }

    @Test
    fun `aceitar a vitrine pela primeira vez publica o perfil`() = runTest {
        val trainers = FakeTrainerRepository()
        val viewModel = viewModel(trainers = trainers)
        advanceUntilIdle()

        viewModel.formActions.onShowcaseChange(true)
        viewModel.formActions.onBioChange("Atendo em estúdio.")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(ShowcaseConsent(accepted = true), trainers.lastDetails?.showcase)
        assertEquals("Atendo em estúdio.", trainers.lastDetails?.bio)
    }

    @Test
    fun `capacidade invalida nao grava e mostra o erro`() = runTest {
        val trainers = FakeTrainerRepository()
        val viewModel = viewModel(trainers = trainers)
        advanceUntilIdle()

        viewModel.formActions.onShowcaseChange(true)
        viewModel.formActions.onMaxStudentsChange("0")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(0, trainers.saveCount)
        assertFalse(viewModel.uiState.value.saved)
        assertTrue(viewModel.formState.value.maxStudentsError != null)
    }

    @Test
    fun `gravacao que falha nao fecha a tela`() = runTest {
        val viewModel = viewModel(trainers = FakeTrainerRepository(failWriting = true))
        advanceUntilIdle()

        viewModel.onSubmit()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.failed)
        assertFalse("quem veio corrigir precisa saber que a correção não pegou", viewModel.uiState.value.saved)
    }

    @Test
    fun `leitura que falha abre a tela em branco, e nao presa carregando`() = runTest {
        val viewModel = viewModel(trainers = FakeTrainerRepository(failReading = true))

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.loading)
        assertFalse(viewModel.uiState.value.missing.any)
        assertNull(viewModel.formState.value.experience)
    }

    private fun viewModel(
        auth: FakeAuthRepository = FakeAuthRepository(),
        users: FakeUserRepository = FakeUserRepository(),
        trainers: FakeTrainerRepository = FakeTrainerRepository(),
    ) = TrainerProfileViewModel(authRepository = auth, userRepository = users, trainerRepository = trainers)
}
