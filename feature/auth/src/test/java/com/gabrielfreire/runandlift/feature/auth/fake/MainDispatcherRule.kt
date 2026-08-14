package com.gabrielfreire.runandlift.feature.auth.fake

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
 * Todo ViewModel deste módulo usa `viewModelScope`, que despacha na `Main` — sem isto, qualquer
 * teste que dispare uma corrotina falha com "Module with the Main dispatcher had failed to
 * initialize". Como regra, e não como `@Before`/`@After` copiados em cada classe: seis arquivos
 * repetindo o mesmo par é seis lugares para esquecer o `resetMain` e vazar o dispatcher para o
 * teste seguinte.
 *
 * [StandardTestDispatcher] e não `UnconfinedTestDispatcher`: o corpo da corrotina só roda quando o
 * teste manda, com `advanceUntilIdle()`. É o que permite afirmar o estado **durante** o envio — um
 * dispatcher irrestrito completaria tudo antes da primeira asserção e esconderia o `submitting`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(val dispatcher: TestDispatcher = StandardTestDispatcher()) : TestWatcher() {

    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)

    override fun finished(description: Description) = Dispatchers.resetMain()
}
