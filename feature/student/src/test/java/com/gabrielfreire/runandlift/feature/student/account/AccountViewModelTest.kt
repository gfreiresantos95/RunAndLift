package com.gabrielfreire.runandlift.feature.student.account

import com.gabrielfreire.runandlift.feature.student.fake.FakeAuthRepository
import com.gabrielfreire.runandlift.feature.student.fake.FakeUserRepository
import com.gabrielfreire.runandlift.feature.student.fake.MainDispatcherRule
import com.gabrielfreire.runandlift.feature.student.validation.NameError
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
 * Dados cadastrais do aluno.
 *
 * A regra que mais importa aqui é a que quase passou despercebida: **editar precisa de uma escrita
 * diferente da do cadastro**. `saveProfile` preenche o que falta e preserva o nome existente, o que
 * é certo no cadastro e faria o botão de salvar não fazer nada nesta tela.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `traz nome, contato e identidade da conta`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals("Ana Ribeiro", viewModel.uiState.value.name)
        assertEquals(FakeAuthRepository.ACCOUNT.email, viewModel.uiState.value.email)
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `salvar substitui o nome, e nao o preserva`() = runTest {
        val users = FakeUserRepository(displayName = "Ana Ribeiro")
        val viewModel = viewModel(users = users)
        advanceUntilIdle()

        viewModel.onNameChange("Ana Ribeiro Santos")
        viewModel.onSubmit()
        advanceUntilIdle()

        // Com `saveProfile` este teste falharia em silêncio: ele só escreve nome quando não há um,
        // e a tela pareceria salvar sem salvar.
        assertEquals("Ana Ribeiro Santos" to "11987654321", users.lastIdentity)
    }

    @Test
    fun `nome sem sobrenome nao passa`() = runTest {
        val users = FakeUserRepository()
        val viewModel = viewModel(users = users)
        advanceUntilIdle()

        viewModel.onNameChange("Ana")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(NameError.INCOMPLETE, viewModel.uiState.value.nameError)
        assertNull("formulário inválido não vai ao banco", users.lastIdentity)
    }

    @Test
    fun `celular vazio e resposta valida, e apaga o numero`() = runTest {
        val users = FakeUserRepository()
        val viewModel = viewModel(users = users)
        advanceUntilIdle()

        viewModel.onPhoneChange("")
        viewModel.onSubmit()
        advanceUntilIdle()

        // Numa tela de edição, esvaziar um campo opcional é uma decisão — diferente do cadastro,
        // onde vazio significa "ainda não informei".
        assertEquals(null, users.lastIdentity?.second)
    }

    @Test
    fun `celular pela metade nao passa`() = runTest {
        val users = FakeUserRepository()
        val viewModel = viewModel(users = users)
        advanceUntilIdle()

        viewModel.onPhoneChange("119876")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.phoneError != null)
        assertNull(users.lastIdentity)
    }

    private fun viewModel(
        auth: FakeAuthRepository = FakeAuthRepository(),
        users: FakeUserRepository = FakeUserRepository(),
    ) = AccountViewModel(authRepository = auth, userRepository = users)
}
