package com.gabrielfreire.runandlift.feature.trainer.account

import com.gabrielfreire.runandlift.feature.trainer.fake.FakeAuthRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeLocationRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeUserRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.MainDispatcherRule
import com.gabrielfreire.runandlift.feature.trainer.fake.SavedIdentity
import com.gabrielfreire.runandlift.feature.trainer.validation.PhoneError
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
 * Os dados cadastrais do treinador.
 *
 * A regra que separa esta tela da equivalente do aluno tem teste próprio: **o celular é
 * obrigatório**. É a mesma assimetria do cadastro — quem presta o serviço precisa ter como ser
 * alcançado fora do app —, e é o tipo de coisa que some numa refatoração que "unifica" as duas
 * telas.
 *
 * O resto do que se afirma aqui é o que nenhum preview mostra: o estado volta do banco como sigla e
 * é exibido por extenso, trocar de estado limpa a cidade, e só a sigla atravessa na gravação.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `abre com o que esta gravado, com o estado por extenso`() = runTest {
        val viewModel = viewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertEquals("Carlos Pereira", state.name)
        assertEquals("11987654321", state.phone)
        assertEquals("carlos@exemplo.com", state.email)
        assertEquals("03/11/1988", state.birthDate)
        assertEquals("São Paulo - SP", state.selectedState?.label)
        assertEquals("Campinas", state.city)
    }

    @Test
    fun `celular vazio nao passa — e no aluno passaria`() = runTest {
        val users = FakeUserRepository()
        val viewModel = viewModel(users = users)
        advanceUntilIdle()

        viewModel.onPhoneChange("")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(PhoneError.REQUIRED, viewModel.uiState.value.phoneError)
        assertNull(users.lastIdentity)
    }

    @Test
    fun `trocar de estado limpa a cidade`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onStatePicked(uf = "RJ", name = "Rio de Janeiro")

        assertEquals("", viewModel.uiState.value.city)
        assertEquals("Rio de Janeiro - RJ", viewModel.uiState.value.selectedState?.label)
    }

    @Test
    fun `reescolher o mesmo estado preserva a cidade`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onStatePicked(uf = "SP", name = "São Paulo")

        assertEquals("Campinas", viewModel.uiState.value.city)
    }

    @Test
    fun `salvar manda so a sigla do estado`() = runTest {
        val users = FakeUserRepository()
        val viewModel = viewModel(users = users)
        advanceUntilIdle()

        viewModel.onNameChange("  Carlos Eduardo Pereira  ")
        viewModel.onSubmit()
        advanceUntilIdle()

        val expected = SavedIdentity(
            displayName = "Carlos Eduardo Pereira",
            phone = "11987654321",
            state = "SP",
            city = "Campinas",
        )

        assertEquals(expected, users.lastIdentity)
        assertTrue(viewModel.uiState.value.saved)
    }

    @Test
    fun `sem lista de estados a localidade volta vazia, para ser escolhida de novo`() = runTest {
        // A implementação real nunca falha em `state()`; sem lista, devolve nulo. Exibir "- SP"
        // seria pior do que pedir de novo.
        val viewModel = viewModel(locations = FakeLocationRepository(failing = true))

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.selectedState)
        assertEquals("", viewModel.uiState.value.city)
    }

    @Test
    fun `nome incompleto nao passa`() = runTest {
        val users = FakeUserRepository()
        val viewModel = viewModel(users = users)
        advanceUntilIdle()

        viewModel.onNameChange("Carlos")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertNull(users.lastIdentity)
        assertFalse(viewModel.uiState.value.saved)
    }

    @Test
    fun `conta sem documento abre a tela vazia, e nao presa carregando`() = runTest {
        val viewModel = viewModel(users = FakeUserRepository(missingProfile = true))

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.loading)
        assertEquals("", viewModel.uiState.value.name)
    }

    private fun viewModel(
        auth: FakeAuthRepository = FakeAuthRepository(),
        users: FakeUserRepository = FakeUserRepository(),
        locations: FakeLocationRepository = FakeLocationRepository(),
    ) = AccountViewModel(authRepository = auth, userRepository = users, locationRepository = locations)
}
