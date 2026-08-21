package com.gabrielfreire.runandlift.feature.trainer.catalog

import com.gabrielfreire.runandlift.data.model.Exercise

/**
 * Estado do catálogo de exercícios.
 *
 * @param query o que está no campo de busca. Vai para o SQLite, que devolve o catálogo inteiro
 *   quando está vazio — `LIKE '%%'` casa com tudo, e é o que faz a tela abrir cheia sem uma segunda
 *   consulta.
 * @param results o que o banco devolveu para [query], **antes** dos chips. Guardado separado do
 *   resultado final para trocar um filtro não custar uma ida ao banco.
 * @param syncing verdadeiro enquanto o catálogo está sendo baixado. Só acontece na primeira abertura
 *   depois de o número de versão subir no Remote Config; nas outras é instantâneo e ninguém vê.
 * @param syncFailed a sincronização não foi. **Não é erro de tela**: o que estiver em disco continua
 *   ali e continua sendo usado. Só vira mensagem quando o disco está vazio, que é o único caso em
 *   que a pessoa fica sem nada.
 */
internal data class CatalogUiState(
    val loading: Boolean = true,
    val syncing: Boolean = false,
    val syncFailed: Boolean = false,
    val query: String = "",
    val results: List<Exercise> = emptyList(),
    val filter: CatalogFilter = CatalogFilter(),
) {

    /** O que a lista mostra: o que veio do banco, passado pelos chips. */
    val exercises: List<Exercise> get() = filter.apply(results)

    /**
     * Nada a mostrar porque não há catálogo em disco.
     *
     * É uma tela **diferente** de "a busca não encontrou nada", e a diferença é o que a pessoa pode
     * fazer: aqui não há o que refinar, o que falta é sincronizar. Confundir as duas manda o
     * treinador apagar a busca para tentar de novo, e continuar sem nada.
     */
    val isCatalogMissing: Boolean get() = results.isEmpty() && !hasQuery && !filter.isActive

    /** Nada a mostrar, mas o catálogo existe — busca ou filtro fecharam demais. */
    val isEmptySearch: Boolean get() = exercises.isEmpty() && !isCatalogMissing

    private val hasQuery: Boolean get() = query.isNotBlank()

    /**
     * Os grupos musculares que aparecem como chip.
     *
     * Vêm **do próprio catálogo**, e não de uma lista fixa no código: o vocabulário é definido pelo
     * importador (`tools/catalog/`), e uma segunda cópia dele aqui divergiria no dia em que um
     * músculo novo entrasse. São dezessete, o que cabe numa fileira que quebra em duas linhas.
     */
    val muscleOptions: List<String>
        get() = results.flatMap { it.muscleGroups }.distinct().sorted()

    /** Idem para equipamento — treze valores, pela mesma razão. */
    val equipmentOptions: List<String>
        get() = results.mapNotNull { it.equipment }.distinct().sorted()
}
