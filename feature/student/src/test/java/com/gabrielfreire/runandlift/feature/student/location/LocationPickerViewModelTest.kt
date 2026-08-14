package com.gabrielfreire.runandlift.feature.student.location

import com.gabrielfreire.runandlift.core.designsystem.component.AppPickerState
import com.gabrielfreire.runandlift.feature.student.fake.FakeLocationRepository
import com.gabrielfreire.runandlift.feature.student.fake.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/**
 * A lista de localidades e a sua busca.
 *
 * O que se testa aqui é justamente o que um preview não mostra: que a busca ignora acento, que
 * "não achei" e "não carregou" são estados diferentes, e que o texto exibido volta a ser a sigla
 * que o banco espera.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LocationPickerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `estados aparecem como nome e sigla`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals(
            listOf("Minas Gerais - MG", "Rio de Janeiro - RJ", "São Paulo - SP"),
            options(viewModel),
        )
    }

    @Test
    fun `busca ignora acento e caixa`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("sao paulo")

        // Quem procura a própria cidade digita sem acento: o teclado com acento custa dois toques
        // por letra. Exigi-lo devolveria lista vazia para um estado que existe.
        assertEquals(listOf("São Paulo - SP"), options(viewModel))
    }

    @Test
    fun `busca sem resultado e uma lista vazia, e nao uma falha`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("zzz")

        // Vazio diz "não encontrei isso"; falha diz "não consegui olhar". Confundi-las faria a tela
        // oferecer "tentar de novo" para uma busca que funcionou.
        assertEquals(emptyList<String>(), options(viewModel))
    }

    @Test
    fun `IBGE fora do ar vira falha, e nao lista vazia`() = runTest {
        val viewModel = viewModel(locations = FakeLocationRepository(failing = true))
        advanceUntilIdle()

        assertEquals(AppPickerState.Failed, viewModel.uiState.value)
    }

    @Test
    fun `nova tentativa recarrega`() = runTest {
        val viewModel = viewModel(locations = FakeLocationRepository(failing = true))
        advanceUntilIdle()

        viewModel.onRetry()
        advanceUntilIdle()

        // O fake continua falhando: o que se confere é que a tentativa acontece de novo e termina
        // num estado, e não que ela fica presa em carregamento para sempre.
        assertEquals(AppPickerState.Failed, viewModel.uiState.value)
    }

    @Test
    fun `cidades vem so com o nome`() = runTest {
        val viewModel = viewModel(uf = "SP")
        advanceUntilIdle()

        assertEquals(listOf("Campinas", "Santo André", "São Paulo"), options(viewModel))
    }

    @Test
    fun `o texto escolhido volta a ser sigla e nome`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        // A tela mostra "São Paulo - SP" e o banco quer "SP". Refazer essa separação por string no
        // destino seria conhecer o formato do rótulo num segundo lugar.
        assertEquals(FakeLocationRepository.SAO_PAULO, viewModel.stateOf("São Paulo - SP"))
    }

    @Test
    fun `a lista de cidades nao traduz estado nenhum`() = runTest {
        val viewModel = viewModel(uf = "SP")
        advanceUntilIdle()

        assertNull(viewModel.stateOf("Campinas"))
    }

    private fun options(viewModel: LocationPickerViewModel): List<String>? =
        (viewModel.uiState.value as? AppPickerState.Options)?.items

    private fun viewModel(locations: FakeLocationRepository = FakeLocationRepository(), uf: String? = null) =
        LocationPickerViewModel(locationRepository = locations, uf = uf)
}
