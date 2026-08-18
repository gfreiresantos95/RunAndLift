package com.gabrielfreire.runandlift.data.remote.location

import com.gabrielfreire.runandlift.data.model.BrazilState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.Collator
import java.util.Locale

/**
 * A resposta do IBGE virando lista, e a sigla virando caminho de URL.
 *
 * Mora fora de [IbgeLocationRemoteDataSource] pela razão de sempre — lá está a chamada de rede,
 * aqui a decisão —, e aqui a decisão é uma que ninguém confere lendo o código: **a ordem**. O IBGE
 * ordena por código de caractere, régua na qual "Águas Claras" cai depois de "Zabelê"; numa lista de
 * 853 nomes, um punhado de acentuadas exiladas no fim parece defeito do app, não da fonte.
 *
 * O JSON é lido pela API de árvore do kotlinx (`parseToJsonElement`), sem `@Serializable`: assim o
 * módulo não precisa do plugin de compilação, e os dois campos que interessam são lidos de uma
 * resposta que tem dezenas — cada município vem com a hierarquia territorial inteira aninhada.
 */
internal object IbgePayload {

    const val FIELD_UF = "sigla"
    const val FIELD_NAME = "nome"

    private val UF_PATTERN = Regex("^[A-Za-z]{2}$")

    /**
     * A sigla conferida e em maiúscula, pronta para entrar na URL.
     *
     * Confere antes de concatenar porque o valor vem de fora: sem isso, lixo entraria no caminho da
     * requisição, e a resposta a uma URL montada com lixo é, na melhor das hipóteses, um 404 que
     * vira "sem cidades" — uma tela vazia no lugar de um erro.
     *
     * `Locale.ROOT` porque isto é chave, não texto: no locale turco, "pi" viraria "Pİ".
     */
    fun requireUf(uf: String): String {
        require(UF_PATTERN.matches(uf)) { "UF inválida: $uf" }

        return uf.uppercase(Locale.ROOT)
    }

    /** As unidades da federação, ordenadas pelo nome como quem procura espera encontrá-las. */
    fun states(json: String): List<BrazilState> = Json.parseToJsonElement(json).jsonArray
        .map { it.jsonObject }
        .map { BrazilState(uf = it.text(FIELD_UF), name = it.text(FIELD_NAME)) }
        .sortedWith(compareBy(collator()) { it.name })

    /** Só os nomes dos municípios, na mesma ordem de gente. */
    fun cities(json: String): List<String> = Json.parseToJsonElement(json).jsonArray
        .map { it.jsonObject.text(FIELD_NAME) }
        .sortedWith(collator())

    /**
     * A régua de ordenação em pt-BR, que põe cada acentuada onde a pessoa vai procurá-la.
     *
     * Uma instância por chamada, e não uma constante: [Collator] não é seguro para uso concorrente,
     * e as duas listas podem estar sendo carregadas ao mesmo tempo.
     */
    private fun collator(): Collator = Collator.getInstance(Locale.forLanguageTag("pt-BR"))

    /** Campo de texto obrigatório: ausente é resposta que não é a que esperávamos, e isso é erro. */
    private fun JsonObject.text(field: String): String = requireNotNull(this[field]) {
        "Campo $field ausente na resposta do IBGE"
    }.jsonPrimitive.content
}
