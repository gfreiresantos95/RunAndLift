package com.gabrielfreire.runandlift.feature.student.account

import com.gabrielfreire.runandlift.feature.student.fake.FakeAuthRepository
import com.gabrielfreire.runandlift.feature.student.fake.FakeLocationRepository
import com.gabrielfreire.runandlift.feature.student.fake.FakeUserRepository
import com.gabrielfreire.runandlift.feature.student.fake.MainDispatcherRule
import com.gabrielfreire.runandlift.feature.student.fake.SavedIdentity
import com.gabrielfreire.runandlift.feature.student.validation.CityError
import com.gabrielfreire.runandlift.feature.student.validation.NameError
import com.gabrielfreire.runandlift.feature.student.validation.StateError
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
 *
 * As de localidade vêm logo atrás, e nenhuma delas aparece num preview: só a sigla é gravada, o nome
 * por extenso é remontado na carga, e trocar de estado apaga a cidade do estado anterior.
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
    fun `sigla gravada volta como nome e sigla`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        // O banco guarda "SP"; o campo tem de exibir a escolha na forma em que ela foi feita.
        assertEquals("São Paulo - SP", viewModel.uiState.value.selectedState?.label)
        assertEquals("Campinas", viewModel.uiState.value.city)
    }

    @Test
    fun `estado que nao pode ser consultado deixa os dois campos vazios`() = runTest {
        val viewModel = viewModel(locations = FakeLocationRepository(failing = true))
        advanceUntilIdle()

        // Sem a lista não dá para escrever "São Paulo - SP", e "- SP" seria pior que vazio. A
        // cidade vai junto: sozinha ela não identifica lugar nenhum.
        assertNull(viewModel.uiState.value.selectedState)
        assertEquals("", viewModel.uiState.value.city)
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
        assertEquals(
            SavedIdentity(
                displayName = "Ana Ribeiro Santos",
                phone = "11987654321",
                state = "SP",
                city = "Campinas",
            ),
            users.lastIdentity,
        )
    }

    @Test
    fun `so a sigla do estado vai ao banco`() = runTest {
        val users = FakeUserRepository()
        val viewModel = viewModel(users = users)
        advanceUntilIdle()

        viewModel.onStatePicked(uf = "RJ", name = "Rio de Janeiro")
        viewModel.onCityPicked("Niterói")
        viewModel.onSubmit()
        advanceUntilIdle()

        // "Rio de Janeiro" fica na tela e morre aqui: duas grafias do mesmo estado no banco seriam
        // dois estados na hora de agrupar alunos por região.
        assertEquals("RJ", users.lastIdentity?.state)
        assertEquals("Niterói", users.lastIdentity?.city)
    }

    @Test
    fun `trocar de estado apaga a cidade do estado anterior`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onStatePicked(uf = "RJ", name = "Rio de Janeiro")

        // Campinas no Rio de Janeiro é um par que não existe. Apagar é a única saída que não exige
        // a pessoa perceber sozinha que precisa escolher de novo.
        assertEquals("", viewModel.uiState.value.city)
    }

    @Test
    fun `escolher o mesmo estado de novo preserva a cidade`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onStatePicked(uf = "SP", name = "São Paulo")

        // Abrir a lista, mudar de ideia e reescolher o mesmo estado não é uma troca — apagar a
        // cidade aqui puniria quem só foi conferir.
        assertEquals("Campinas", viewModel.uiState.value.city)
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
    fun `conta antiga sem localidade nao salva sem escolher`() = runTest {
        val users = FakeUserRepository(storedState = null, storedCity = null)
        val viewModel = viewModel(users = users)
        advanceUntilIdle()

        viewModel.onSubmit()
        advanceUntilIdle()

        // É o preenchimento retroativo: quem criou conta antes de o campo existir responde aqui, na
        // primeira vez que voltar a esta tela.
        assertEquals(StateError.REQUIRED, viewModel.uiState.value.stateError)
        assertEquals(CityError.REQUIRED, viewModel.uiState.value.cityError)
        assertNull(users.lastIdentity)
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
        assertEquals(null, users.lastIdentity?.phone)
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
        locations: FakeLocationRepository = FakeLocationRepository(),
    ) = AccountViewModel(authRepository = auth, userRepository = users, locationRepository = locations)
}
