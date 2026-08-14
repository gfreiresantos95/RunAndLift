package com.gabrielfreire.runandlift.data.location

import com.gabrielfreire.runandlift.data.model.BrazilState
import com.gabrielfreire.runandlift.data.remote.location.LocationRemoteDataSource
import com.gabrielfreire.runandlift.data.util.AppDispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * [LocationRepository] com cache em memória por cima do IBGE.
 *
 * O cache não é otimização, é o que torna a decisão de consultar a API em tempo de execução
 * suportável. A divisão político-administrativa não muda durante o uso do app, e a resposta de
 * municípios de um estado grande passa de 380 KB: sem cache, voltar do seletor de cidade para
 * trocar o estado e voltar de novo custaria o download inteiro cada vez.
 *
 * **Em memória, e não em disco.** Um cache persistido resolveria também a abertura seguinte, e é o
 * candidato natural se a lentidão incomodar — mas seria uma tabela do Room, com esquema exportado e
 * migração, para um dado que a sessão inteira usa duas vezes. O gatilho para promovê-lo é a
 * primeira reclamação de espera, não a possibilidade dela.
 *
 * O [Mutex] existe para o caso que a tela produz sozinha: abrir o seletor gira uma carga, e um toque
 * apressado em voltar-e-entrar gira outra antes de a primeira terminar. Sem ele, as duas baixariam
 * a mesma lista.
 */
internal class CachedLocationRepository(
    private val remoteDataSource: LocationRemoteDataSource,
    private val dispatchers: AppDispatchers,
) : LocationRepository {

    private val mutex = Mutex()

    private var cachedStates: List<BrazilState>? = null
    private val cachedCities = mutableMapOf<String, List<String>>()

    override suspend fun states(): List<BrazilState> = withContext(dispatchers.io) {
        mutex.withLock {
            cachedStates ?: remoteDataSource.states().also { cachedStates = it }
        }
    }

    override suspend fun cities(uf: String): List<String> = withContext(dispatchers.io) {
        // `Locale.ROOT` porque isto é chave, não texto: no locale turco, `uppercase()` transforma
        // "i" em "İ", e a mesma UF entraria duas vezes no cache.
        val key = uf.uppercase(Locale.ROOT)

        mutex.withLock {
            cachedCities[key] ?: remoteDataSource.cities(key).also { cachedCities[key] = it }
        }
    }

    /**
     * Nunca propaga falha: quem chama está desenhando um perfil já gravado, e a alternativa a "não
     * sei o nome deste estado" é a tela não abrir. Mostrar só a sigla é a perda menor.
     */
    override suspend fun state(uf: String): BrazilState? {
        val target = uf.uppercase(Locale.ROOT)

        return runCatching { states() }.getOrNull()?.firstOrNull { it.uf == target }
    }
}
