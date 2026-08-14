package com.gabrielfreire.runandlift.data.location

import java.text.Normalizer
import java.util.Locale

/**
 * Como se procura um estado ou uma cidade pelo nome.
 *
 * A regra inteira é: **acento não conta e maiúscula não conta**. Quem procura a própria cidade
 * digita "sao paulo" ou "vitoria", porque o teclado com acento custa dois toques por letra e porque
 * metade das pessoas não sabe de cor onde vai o circunflexo de "Amapá". Uma busca que exige o acento
 * devolve lista vazia para um nome que existe — e o efeito prático é a pessoa concluir que a cidade
 * dela não está lá.
 *
 * **Mora em `:data`, e não no módulo de tela**, por dois motivos. O primeiro é que são duas telas em
 * dois módulos que não se enxergam — o cadastro e o perfil —, e a busca precisa responder igual nos
 * dois: uma cópia da regra em cada um é uma cópia que vai divergir. O segundo é que isto é
 * conhecimento sobre nome de localidade, e não sobre layout: `:core` desenha a lista, e desenhar não
 * inclui decidir o que casa com o quê.
 *
 * O que ela deliberadamente **não** faz: distância de edição, sinônimo, sigla ou "você quis dizer".
 * A lista tem campo de busca e no máximo 853 itens; adivinhar a intenção de quem digitou errado é
 * resolver um problema que rolar dez linhas já resolve.
 */
object LocationSearch {

    /**
     * `true` quando [query] aparece em [candidate], ignorando acento e caixa.
     *
     * Busca vazia casa com tudo — é o estado inicial da tela, e uma lista vazia ali diria que não
     * existe estado nenhum.
     */
    fun matches(candidate: String, query: String): Boolean = fold(candidate).contains(fold(query))

    /**
     * Texto reduzido à forma comparável: sem acento, tudo minúsculo.
     *
     * `NFD` separa a letra do sinal — "á" vira "a" mais um acento agudo solto — e aí o sinal é uma
     * marca sem espaçamento (`Mn`), que a expressão remove. É o caminho que funciona para o
     * alfabeto inteiro, em vez de uma tabela de "á→a, ã→a, â→a" que esquece o ü de Jaguarão.
     *
     * `Locale.ROOT` porque isto é comparação, não texto para ler: no locale turco, `lowercase()`
     * transformaria o "I" de "Ipiranga" num caractere que o "i" digitado não encontraria.
     */
    private fun fold(text: String): String = Normalizer
        .normalize(text, Normalizer.Form.NFD)
        .replace(DIACRITICS, "")
        .lowercase(Locale.ROOT)
        .trim()

    /** Marcas sem espaçamento — o que sobra de um acento depois da decomposição. */
    private val DIACRITICS = Regex("\\p{Mn}+")
}
