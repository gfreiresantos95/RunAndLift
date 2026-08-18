package com.gabrielfreire.runandlift.feature.auth.location

import com.gabrielfreire.runandlift.core.designsystem.component.AppPickerState
import com.gabrielfreire.runandlift.feature.auth.fake.FakeLocationRepository
import com.gabrielfreire.runandlift.feature.auth.fake.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/**
 * A lista de localidades e a sua busca, no fluxo de cadastro.
 *
 * O que se testa aqui é justamente o que um preview não mostra: que "não achei" e "não carregou"
 * são estados diferentes, e que o texto exibido volta a ser a sigla que o banco espera.
 */
class LocationPickerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `estados aparecem como nome e sigla`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        testScheduler.advanceUntilIdle()

        assertEquals(
            listOf("Minas Gerais - MG", "Rio de Janeiro - RJ", "São Paulo - SP"),
            options(viewModel),
        )
    }

    @Test
    fun `busca sem resultado e uma lista vazia, e nao uma falha`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onQueryChange("zzz")

        // Vazio diz "não encontrei isso"; falha diz "não consegui olhar". Confundi-las faria a tela
        // oferecer "tentar de novo" para uma busca que funcionou.
        assertEquals(emptyList<String>(), options(viewModel))
    }

    @Test
    fun `IBGE fora do ar vira falha, e nao lista vazia`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel(locations = FakeLocationRepository(failing = true))
        testScheduler.advanceUntilIdle()

        // Numa lista vazia a pessoa conclui que o app não conhece o estado dela; na falha, ela vê
        // um botão de tentar de novo.
        assertEquals(AppPickerState.Failed, viewModel.uiState.value)
    }

    @Test
    fun `busca digitada antes de carregar continua valendo`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()

        // Digitar enquanto a lista ainda vem é o caso real de quem já sabe o que procura. O filtro
        // não pode se perder quando a resposta chega.
        viewModel.onQueryChange("minas")
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("Minas Gerais - MG"), options(viewModel))
    }

    @Test
    fun `cidades vem so com o nome`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel(uf = "SP")
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("Campinas", "Santo André", "São Paulo"), options(viewModel))
    }

    @Test
    fun `o texto escolhido volta a ser sigla e nome`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        testScheduler.advanceUntilIdle()

        // A tela mostra "São Paulo - SP" e o banco quer "SP". Refazer essa separação por string no
        // destino seria conhecer o formato do rótulo num segundo lugar.
        assertEquals(FakeLocationRepository.SAO_PAULO, viewModel.stateOf("São Paulo - SP"))
    }

    @Test
    fun `a lista de cidades nao traduz estado nenhum`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel(uf = "SP")
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.stateOf("Campinas"))
    }

    @Test
    fun `nova tentativa depois da falha carrega de verdade`() = runTest(mainDispatcherRule.dispatcher) {
        val locations = FakeLocationRepository(failing = true)
        val viewModel = viewModel(locations = locations)
        testScheduler.advanceUntilIdle()

        // A rede volta com a tela de falha na frente, que é o caso comum: a pessoa vê o aviso,
        // espera um instante e toca em tentar de novo.
        locations.failing = false
        viewModel.onRetry()
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("Minas Gerais - MG", "Rio de Janeiro - RJ", "São Paulo - SP"), options(viewModel))
    }

    @Test
    fun `nova tentativa que tambem falha volta a ser falha, e nao lista vazia`() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = viewModel(locations = FakeLocationRepository(failing = true))
            testScheduler.advanceUntilIdle()

            viewModel.onRetry()
            testScheduler.advanceUntilIdle()

            // "Nada encontrado" e "não consegui carregar" são telas diferentes de propósito: uma
            // pede outra busca, a outra oferece tentar de novo.
            assertEquals(AppPickerState.Failed, viewModel.uiState.value)
        }

    private fun options(viewModel: LocationPickerViewModel): List<String>? =
        (viewModel.uiState.value as? AppPickerState.Options)?.items

    private fun viewModel(locations: FakeLocationRepository = FakeLocationRepository(), uf: String? = null) =
        LocationPickerViewModel(locationRepository = locations, uf = uf)
}
