package com.gabrielfreire.runandlift.data.remote.location

import com.gabrielfreire.runandlift.data.model.BrazilState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL
import java.text.Collator
import java.util.Locale

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
 * O JSON é lido pela API de árvore do kotlinx (`parseToJsonElement`), sem `@Serializable`: assim o
 * módulo não precisa do plugin de compilação, e os três campos que interessam são lidos de uma
 * resposta que tem dezenas.
 *
 * @param baseUrl injetável para o teste não sair à rede.
 */
internal class IbgeLocationRemoteDataSource(private val baseUrl: String = IBGE_LOCALIDADES) :
    LocationRemoteDataSource {

    override suspend fun states(): List<BrazilState> = fetch("$baseUrl/estados")
        .map { it.jsonObject }
        .map { BrazilState(uf = it.text(FIELD_UF), name = it.text(FIELD_NAME)) }
        .sortedWith(compareBy(collator()) { it.name })

    /**
     * Municípios de uma UF.
     *
     * A sigla vai na URL, então ela é conferida antes: duas letras e nada mais. Sem isso, um valor
     * vindo de fora entraria no caminho da requisição — e a resposta a uma URL montada com lixo é,
     * na melhor das hipóteses, um 404 que vira "sem cidades".
     */
    override suspend fun cities(uf: String): List<String> {
        require(UF_PATTERN.matches(uf)) { "UF inválida: $uf" }

        return fetch("$baseUrl/estados/${uf.uppercase(Locale.ROOT)}/municipios")
            .map { it.jsonObject.text(FIELD_NAME) }
            .sortedWith(collator())
    }

    private suspend fun fetch(url: String): JsonArray = Json.parseToJsonElement(read(url)).jsonArray

    /**
     * A ordenação é nossa, e não a do parâmetro `orderBy` da API.
     *
     * O IBGE ordena por código de caractere, e nessa régua "Águas Claras" cai depois de "Zabelê" —
     * numa lista de 853 nomes com campo de busca, um punhado de acentuadas exiladas no fim parece
     * defeito. O [Collator] em pt-BR põe cada uma onde a pessoa vai procurá-la.
     *
     * Uma instância por chamada, e não uma constante: [Collator] não é seguro para uso concorrente,
     * e as duas listas podem estar sendo carregadas ao mesmo tempo.
     */
    private fun collator(): Collator = Collator.getInstance(Locale.forLanguageTag("pt-BR"))

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

    /** Campo de texto obrigatório: ausente é resposta que não é a que esperávamos, e isso é erro. */
    private fun JsonObject.text(field: String): String = requireNotNull(this[field]) {
        "Campo $field ausente na resposta do IBGE"
    }.jsonPrimitive.content

    private companion object {
        const val IBGE_LOCALIDADES = "https://servicodados.ibge.gov.br/api/v1/localidades"

        const val FIELD_UF = "sigla"
        const val FIELD_NAME = "nome"

        const val TIMEOUT_MS = 15_000
        const val HTTP_OK = 200
        const val HTTP_REDIRECT = 300

        val UF_PATTERN = Regex("^[A-Za-z]{2}$")
    }
}
