package com.gabrielfreire.runandlift.feature.trainer.catalog

import com.gabrielfreire.runandlift.data.model.Exercise

/**
 * Estado do catálogo de exercícios.
 *
 * @param query o que está no campo de busca. Vai para o SQLite, que devolve o catálogo inteiro
 *   quando está vazio — `LIKE '%%'` casa com tudo, e é o que faz a tela abrir cheia sem uma segunda
 *   consulta.
 * @param catalog o catálogo **inteiro** em disco, sem busca e sem chips. Existe separado de
 *   [results] por causa dos chips: os de músculo e equipamento são construídos a partir do
 *   vocabulário do catálogo, e quando saíam de [results] eles encolhiam a cada letra digitada — a
 *   pessoa via as opções de filtro sumirem enquanto procurava, que é o oposto do que um filtro
 *   promete. Também é ele que responde se há catálogo em disco.
 * @param results o que o banco devolveu para [query], **antes** dos chips. Guardado separado do
 *   resultado final para trocar um filtro não custar uma ida ao banco.
 * @param filtersExpanded se as fileiras de chip estão abertas. **Fechadas por padrão**: são quatro
 *   assuntos de filtro, e abertos eles ocupavam meia tela antes da primeira linha da lista — que é
 *   o que a pessoa veio ver. Fechado, o filtro é uma linha só que diz quantos estão marcados.
 * @param recomputing a lista está sendo refeita porque a busca ou um chip mudou. Diferente de
 *   [loading], que é a **primeira** carga: aqui já existe lista na tela, e a busca e os filtros
 *   continuam à mostra para a pessoa poder desfazer o que acabou de fazer.
 *
 *   Existe porque marcar um chip filtra em memória e termina antes do quadro seguinte: sem um
 *   estado próprio, a lista simplesmente encolhia, e encolher sem aviso se lê como defeito e não
 *   como resposta. O ViewModel **segura** este estado por uma janela mínima de propósito — é a
 *   diferença entre o app parecer que engasgou e o app dizer que está trabalhando.
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
    val catalog: List<Exercise> = emptyList(),
    val results: List<Exercise> = emptyList(),
    val filter: CatalogFilter = CatalogFilter(),
    val filtersExpanded: Boolean = false,
    val recomputing: Boolean = false,
) {

    /**
     * O que a lista mostra: o que veio do banco, passado pelos chips.
     *
     * **Campo calculado na construção, e não `get()`.** Como propriedade calculada, ele refazia a
     * filtragem uma vez por leitor — a contagem, o teste de vazio e o laço da lista, três varreduras
     * de centenas de itens a cada recomposição. Aqui a conta acontece uma vez por estado novo, que é
     * exatamente quantas vezes ela muda de resultado.
     */
    val exercises: List<Exercise> = filter.apply(results)

    /**
     * Os grupos musculares que aparecem como chip.
     *
     * Vêm **do catálogo inteiro**, e não de uma lista fixa no código: o vocabulário é definido pelo
     * importador (`tools/catalog/`), e uma segunda cópia dele aqui divergiria no dia em que um
     * músculo novo entrasse. São dezessete, e a lista não muda enquanto a pessoa digita.
     */
    val muscleOptions: List<String> = catalog.flatMap { it.muscleGroups }.distinct().sorted()

    /** Idem para equipamento — treze valores, pela mesma razão. */
    val equipmentOptions: List<String> = catalog.mapNotNull { it.equipment }.distinct().sorted()

    /**
     * Nada a mostrar porque não há catálogo em disco.
     *
     * É uma tela **diferente** de "a busca não encontrou nada", e a diferença é o que a pessoa pode
     * fazer: aqui não há o que refinar, o que falta é sincronizar. Confundir as duas manda o
     * treinador apagar a busca para tentar de novo, e continuar sem nada.
     */
    val isCatalogMissing: Boolean get() = catalog.isEmpty()

    /** Nada a mostrar, mas o catálogo existe — busca ou filtro fecharam demais. */
    val isEmptySearch: Boolean get() = exercises.isEmpty() && !isCatalogMissing

    /**
     * Quantos chips estão marcados, somando os quatro assuntos.
     *
     * É o que a linha fechada de filtro mostra. Sem esse número, fechar os filtros esconderia o que
     * está encolhendo a lista — e quem não lembra de ter marcado nada concluiria que o catálogo é
     * pobre em vez de desmarcar um chip.
     */
    val activeFilterCount: Int get() = filter.count
}
