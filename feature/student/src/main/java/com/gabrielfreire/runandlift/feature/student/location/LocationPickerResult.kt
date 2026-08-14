package com.gabrielfreire.runandlift.feature.student.location

/**
 * As chaves pelas quais a tela de seleção devolve a escolha para quem a abriu.
 *
 * É o "abrir para obter um resultado" da navegação do Compose: a tela de escolha escreve no
 * `SavedStateHandle` da entrada **anterior** da pilha e se desempilha; a tela de dados cadastrais,
 * ao voltar à composição, lê e limpa.
 *
 * **É uma segunda cópia do que o `:feature:auth` tem**, e é deliberado, pela mesma razão de
 * [com.gabrielfreire.runandlift.feature.student.validation.AccountFormValidation]: os dois módulos
 * não se enxergam. As chaves são as mesmas strings por coincidência de nome, não por contrato — cada
 * grafo entrega o resultado dentro de si, e nenhuma das duas telas jamais lê o que a outra escreveu.
 *
 * O que **não** está duplicado é a parte cara: a busca sem acento vive em `:data`
 * ([com.gabrielfreire.runandlift.data.location.LocationSearch]) e o desenho da tela em `:core`.
 *
 * **Gatilho para extrair:** o terceiro módulo que precisar escolher uma localidade.
 */
internal object LocationPickerResult {

    /** Sigla do estado escolhido — o que vai ao banco. */
    const val STATE_UF = "location.state.uf"

    /** Nome por extenso do mesmo estado, para o campo poder exibir `São Paulo - SP`. */
    const val STATE_NAME = "location.state.name"

    /** Nome do município escolhido. */
    const val CITY = "location.city"
}
