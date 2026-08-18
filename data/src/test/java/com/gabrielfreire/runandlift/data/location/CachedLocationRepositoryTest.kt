package com.gabrielfreire.runandlift.data.location

import com.gabrielfreire.runandlift.data.model.BrazilState
import com.gabrielfreire.runandlift.data.remote.location.LocationRemoteDataSource
import com.gabrielfreire.runandlift.data.util.AppDispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

/**
 * O cache que torna pagável a decisão de consultar o IBGE em tempo de execução.
 *
 * Aqui não se testa o IBGE: testa-se a **política** por cima dele, que é onde estão as três coisas
 * que só aparecem no uso. A lista de municípios de Minas Gerais passa de 380 KB, e o seletor é uma
 * tela de onde se entra e se sai — sem o cache, cada volta custaria o download inteiro de novo.
 *
 * O último teste é o que mais importa e o que menos parece: `state(uf)` **nunca propaga falha**. Quem
 * a chama está desenhando um perfil já gravado, e a alternativa a "não sei o nome deste estado" não
 * é uma mensagem de erro — é a tela não abrir.
 */
class CachedLocationRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()

    private val dispatchers = AppDispatchers(io = testDispatcher, default = testDispatcher)

    @Test
    fun `a segunda consulta de estados nao vai a rede`() = runTest(testDispatcher) {
        val remote = FakeLocationRemoteDataSource()
        val repository = CachedLocationRepository(remote, dispatchers)

        repository.states()
        repository.states()

        assertEquals(1, remote.stateCalls)
    }

    @Test
    fun `estados vem inteiros da fonte, sem o cache mexer na ordem`() = runTest(testDispatcher) {
        val repository = CachedLocationRepository(FakeLocationRemoteDataSource(), dispatchers)

        assertEquals(listOf("MG", "SP"), repository.states().map { it.uf })
    }

    @Test
    fun `cada UF tem o proprio cache, e uma nao serve pela outra`() = runTest(testDispatcher) {
        val remote = FakeLocationRemoteDataSource()
        val repository = CachedLocationRepository(remote, dispatchers)

        repository.cities("SP")
        repository.cities("MG")
        repository.cities("SP")

        // Dois downloads e não três: o terceiro pedido é o primeiro estado outra vez.
        assertEquals(2, remote.cityCalls)
        assertEquals(listOf("Belo Horizonte", "Uberlândia"), repository.cities("MG"))
    }

    @Test
    fun `a sigla em minuscula acha o mesmo cache da maiuscula`() = runTest(testDispatcher) {
        val remote = FakeLocationRemoteDataSource()
        val repository = CachedLocationRepository(remote, dispatchers)

        repository.cities("SP")
        repository.cities("sp")

        assertEquals(1, remote.cityCalls)
    }

    @Test
    fun `a UF sobe para maiuscula pelo locale raiz, e nao pelo do aparelho`() = runTest(testDispatcher) {
        val original = Locale.getDefault()
        val remote = FakeLocationRemoteDataSource()
        val repository = CachedLocationRepository(remote, dispatchers)

        try {
            // No locale turco, `"i".uppercase()` vira `İ` — e `PI` entraria no cache como duas
            // chaves diferentes conforme o aparelho de quem digitou.
            Locale.setDefault(Locale.forLanguageTag("tr"))
            repository.cities("pi")
            repository.cities("PI")
        } finally {
            Locale.setDefault(original)
        }

        assertEquals(1, remote.cityCalls)
        assertEquals("PI", remote.lastUf)
    }

    @Test
    fun `state acha o estado pela sigla, em qualquer caixa`() = runTest(testDispatcher) {
        val repository = CachedLocationRepository(FakeLocationRemoteDataSource(), dispatchers)

        assertEquals("São Paulo", repository.state("sp")?.name)
    }

    @Test
    fun `state responde nulo para sigla que nao existe`() = runTest(testDispatcher) {
        val repository = CachedLocationRepository(FakeLocationRemoteDataSource(), dispatchers)

        assertNull(repository.state("XX"))
    }

    @Test
    fun `IBGE fora do ar nao impede um perfil de abrir`() = runTest(testDispatcher) {
        val repository = CachedLocationRepository(FakeLocationRemoteDataSource(failing = true), dispatchers)

        // A exceção morre aqui de propósito. Quem chama mostra só a sigla, que é a perda menor.
        assertNull(repository.state("SP"))
    }

    @Test
    fun `falha nao vira cache, e a proxima tentativa acontece`() = runTest(testDispatcher) {
        val remote = FakeLocationRemoteDataSource(failing = true)
        val repository = CachedLocationRepository(remote, dispatchers)

        repository.state("SP")
        remote.failing = false

        // Guardar o fracasso seria condenar a sessão inteira por causa de um segundo sem sinal.
        assertEquals("São Paulo", repository.state("SP")?.name)
    }

    /**
     * A fonte crua, que conta quantas vezes foi chamada.
     *
     * Contar a chamada é o teste inteiro: o cache não muda **o que** volta, só quantas vezes se vai
     * buscar — e uma asserção sobre o conteúdo passaria igual com o cache removido.
     */
    private class FakeLocationRemoteDataSource(var failing: Boolean = false) : LocationRemoteDataSource {

        var stateCalls: Int = 0
            private set

        var cityCalls: Int = 0
            private set

        /** A sigla que chegou aqui, para afirmar que a normalização acontece antes da rede. */
        var lastUf: String? = null
            private set

        override suspend fun states(): List<BrazilState> {
            if (failing) error("IBGE fora do ar")

            stateCalls++

            return listOf(BrazilState(uf = "MG", name = "Minas Gerais"), BrazilState(uf = "SP", name = "São Paulo"))
        }

        override suspend fun cities(uf: String): List<String> {
            if (failing) error("IBGE fora do ar")

            cityCalls++
            lastUf = uf

            return when (uf) {
                "MG" -> listOf("Belo Horizonte", "Uberlândia")
                "SP" -> listOf("Campinas", "São Paulo")
                else -> emptyList()
            }
        }
    }
}
