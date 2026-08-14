package com.gabrielfreire.runandlift.feature.student.fake

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Troca a `Main` por um dispatcher de teste enquanto o teste roda.
 *
 * Cópia da regra do `:feature-auth`, e não um reuso: os dois módulos não compartilham source set de
 * teste, e criar um módulo `:test-fixtures` para trinta linhas custaria mais configuração de build
 * do que a duplicação custa de manutenção. Quando o terceiro módulo precisar dela, o cálculo muda.
 *
 * [StandardTestDispatcher] e não `UnconfinedTestDispatcher`: a corrotina só roda quando o teste
 * manda, com `advanceUntilIdle()`. É o que permite afirmar o estado de carregamento **antes** de a
 * leitura terminar — um dispatcher irrestrito completaria tudo antes da primeira asserção.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(val dispatcher: TestDispatcher = StandardTestDispatcher()) : TestWatcher() {

    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)

    override fun finished(description: Description) = Dispatchers.resetMain()
}
