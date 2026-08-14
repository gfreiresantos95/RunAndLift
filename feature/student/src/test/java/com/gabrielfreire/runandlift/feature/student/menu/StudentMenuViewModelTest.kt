package com.gabrielfreire.runandlift.feature.student.menu

import com.gabrielfreire.runandlift.feature.student.fake.FakeAuthRepository
import com.gabrielfreire.runandlift.feature.student.fake.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * A ordem do logout, que é a regra inteira desta tela: **primeiro encerra a sessão, depois navega**.
 *
 * Invertê-la deixaria a tela de entrada visível com a sessão ainda ativa, e um retorno rápido
 * cairia de novo na home — o tipo de defeito que só aparece em aparelho lento e nunca no preview.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StudentMenuViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `sair encerra a sessao`() = runTest {
        val auth = FakeAuthRepository()
        val viewModel = StudentMenuViewModel(auth)

        viewModel.signOut(onSignedOut = {})
        advanceUntilIdle()

        assertEquals(1, auth.signOutCount)
    }

    @Test
    fun `so navega depois de a sessao terminar`() = runTest {
        val auth = FakeAuthRepository()
        val viewModel = StudentMenuViewModel(auth)
        var navigated = false

        viewModel.signOut(onSignedOut = { navigated = true })

        // Antes de a corrotina rodar, nada aconteceu — nem a saída, nem a navegação.
        assertFalse(navigated)
        assertEquals(0, auth.signOutCount)

        advanceUntilIdle()

        assertTrue(navigated)
        assertEquals(1, auth.signOutCount)
    }
}
