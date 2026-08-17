package com.gabrielfreire.runandlift.feature.trainer.location

import com.gabrielfreire.runandlift.core.designsystem.component.AppPickerState
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeLocationRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * A lista de estados e a de municípios.
 *
 * O que se afirma aqui é o que o preview não mostra: a busca ignora acento e caixa, uma falha vira
 * um estado **próprio** — e não uma lista vazia, que diria "não existe" em vez de "não carregou" —,
 * e o texto exibido não é o que se grava.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LocationPickerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `lista os estados por extenso com a sigla`() = runTest {
        val viewModel = LocationPickerViewModel(FakeLocationRepository())

        advanceUntilIdle()

        assertEquals(
            listOf("Minas Gerais - MG", "Rio de Janeiro - RJ", "São Paulo - SP"),
            (viewModel.uiState.value as AppPickerState.Options).items,
        )
    }

    @Test
    fun `a busca ignora acento e caixa`() = runTest {
        val viewModel = LocationPickerViewModel(FakeLocationRepository(), uf = "SP")
        advanceUntilIdle()

        viewModel.onQueryChange("sao")
        advanceUntilIdle()

        assertEquals(listOf("São Paulo"), (viewModel.uiState.value as AppPickerState.Options).items)
    }

    @Test
    fun `busca sem resultado devolve lista vazia, e nao falha`() = runTest {
        val viewModel = LocationPickerViewModel(FakeLocationRepository(), uf = "SP")
        advanceUntilIdle()

        viewModel.onQueryChange("Manaus")
        advanceUntilIdle()

        assertTrue((viewModel.uiState.value as AppPickerState.Options).items.isEmpty())
    }

    @Test
    fun `IBGE fora do ar vira falha, e nao lista vazia`() = runTest {
        val viewModel = LocationPickerViewModel(FakeLocationRepository(failing = true))

        advanceUntilIdle()

        assertEquals(AppPickerState.Failed, viewModel.uiState.value)
    }

    @Test
    fun `tentar de novo recomeca do carregamento`() = runTest {
        val viewModel = LocationPickerViewModel(FakeLocationRepository(failing = true))
        advanceUntilIdle()

        viewModel.onRetry()

        assertEquals(AppPickerState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `traduz o texto escolhido de volta para o estado`() = runTest {
        val viewModel = LocationPickerViewModel(FakeLocationRepository())
        advanceUntilIdle()

        assertEquals(FakeLocationRepository.SAO_PAULO, viewModel.stateOf("São Paulo - SP"))
    }

    @Test
    fun `na lista de cidades nao ha estado a traduzir`() = runTest {
        val viewModel = LocationPickerViewModel(FakeLocationRepository(), uf = "SP")
        advanceUntilIdle()

        assertNull(viewModel.stateOf("Campinas"))
    }
}
