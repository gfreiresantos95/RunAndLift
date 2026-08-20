package com.gabrielfreire.runandlift.feature.student.home

/**
 * Separador de milhar do português. Ponto, e não vírgula — a vírgula aqui é a casa decimal.
 *
 * Está escrito à mão e não vem de `NumberFormat` pela mesma razão de os dias da semana virem de
 * `R.string`: `NumberFormat` segue o idioma **do aparelho**, e um app em português mostraria
 * "12,480 kg" para quem tem o celular em inglês — que se lê como doze quilos e meio, e não como
 * doze toneladas e meia. O número é conteúdo da tela, não formatação de sistema.
 */
private const val THOUSANDS = '.'

/** De quantos em quantos dígitos o separador entra, contando da direita. */
private const val GROUP = 3

/** Minutos numa hora. */
private const val HOUR = 60

/**
 * O número com separador de milhar — 12480 vira "12.480".
 *
 * Existe porque volume de treino passa dos cinco dígitos em qualquer semana séria, e um "12480"
 * corrido obriga quem lê a contar as casas com o olho. Abaixo de mil não muda nada.
 *
 * Negativo não acontece em volume, mas acontece em subtração mal feita: o sinal fica fora do
 * agrupamento em vez de virar um separador solto na frente do número.
 */
internal fun Int.asGroupedNumber(): String {
    val digits = toString().removePrefix(prefix = "-")

    val grouped = digits
        .reversed()
        .chunked(size = GROUP)
        .joinToString(separator = THOUSANDS.toString())
        .reversed()

    return if (this < 0) "-$grouped" else grouped
}

/**
 * Minutos em tempo de treino — 197 vira "3h17", 45 vira "45min".
 *
 * A hora só aparece quando existe. "0h45" é o formato de cronômetro, e o painel não está cronome-
 * trando nada: está dizendo quanto tempo alguém passou treinando na semana, que é uma frase.
 *
 * Os minutos ganham zero à esquerda depois da hora, e não antes: "3h7" se lê como três horas e sete
 * minutos por quem já sabe, e como erro de digitação por todo mundo.
 */
internal fun Int.asDuration(): String {
    val hours = this / HOUR
    val minutes = this % HOUR

    return if (hours == 0) "${minutes}min" else "${hours}h${minutes.toString().padStart(2, '0')}"
}
