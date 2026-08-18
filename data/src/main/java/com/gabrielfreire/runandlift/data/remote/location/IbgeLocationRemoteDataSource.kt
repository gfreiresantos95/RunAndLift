package com.gabrielfreire.runandlift.data.remote.location

import com.gabrielfreire.runandlift.data.model.BrazilState
import java.net.HttpURLConnection
import java.net.URL

/**
 * [LocationRemoteDataSource] sobre a API de Localidades do IBGE.
 *
 * É a fonte oficial da divisão político-administrativa, é pública e não pede chave. O custo dela é
 * o tamanho da resposta: cada município vem com a hierarquia territorial inteira aninhada, então a
 * lista de Minas Gerais chega com cerca de **380 KB** para entregar 853 nomes. Não há parâmetro de
 * seleção de campos na API — o que dá para fazer é o que este arquivo faz: buscar uma UF por vez,
 * ficar só com o nome, e deixar o cache do repositório impedir a segunda ida.
 *
 * `HttpURLConnection` e não um cliente HTTP: são duas chamadas GET sem autenticação, sem cabeçalho
 * especial e sem corpo. Um Retrofit ou um Ktor aqui seria uma dependência inteira — e uma decisão
 * de arquitetura para o projeto todo — a serviço de dois `GET`.
 *
 * Aqui ficou só a ida à rede. O que a resposta vira, e em que ordem, mora em [IbgePayload], que um
 * teste comum alcança com uma string.
 *
 * @param baseUrl injetável para o teste não sair à rede.
 */
internal class IbgeLocationRemoteDataSource(private val baseUrl: String = IBGE_LOCALIDADES) :
    LocationRemoteDataSource {

    override suspend fun states(): List<BrazilState> = IbgePayload.states(read("$baseUrl/estados"))

    /**
     * Municípios de uma UF.
     *
     * A sigla vai na URL, então ela é conferida antes por [IbgePayload.requireUf] — um valor vindo
     * de fora não entra no caminho da requisição sem passar por lá.
     */
    override suspend fun cities(uf: String): List<String> =
        IbgePayload.cities(read("$baseUrl/estados/${IbgePayload.requireUf(uf)}/municipios"))

    /**
     * Corpo da resposta, ou exceção.
     *
     * `suspend` sem trocar de dispatcher de propósito: quem chama já está no de I/O, e trocar aqui
     * de novo esconderia essa responsabilidade no lugar errado.
     */
    @Suppress("RedundantSuspendModifier")
    private suspend fun read(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            setRequestProperty("Accept", "application/json")
        }

        return try {
            val code = connection.responseCode
            check(code in HTTP_OK until HTTP_REDIRECT) { "IBGE respondeu $code em $url" }

            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val IBGE_LOCALIDADES = "https://servicodados.ibge.gov.br/api/v1/localidades"

        const val TIMEOUT_MS = 15_000
        const val HTTP_OK = 200
        const val HTTP_REDIRECT = 300
    }
}
