package com.gabrielfreire.runandlift.feature.trainer.catalog

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.AppIcons
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppEmptyState
import com.gabrielfreire.runandlift.core.designsystem.component.AppListScreenScaffold
import com.gabrielfreire.runandlift.core.designsystem.component.AppLoadingState
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextField
import com.gabrielfreire.runandlift.core.designsystem.component.NO_LOADING_DELAY
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * O catálogo: busca, filtros e a lista de exercícios.
 *
 * **Lista preguiçosa, e não coluna rolável.** É a tela do produto que passou do que uma coluna
 * comum aguenta: são centenas de exercícios, e compô-los todos de uma vez era o que fazia a tela
 * "encher de repente" ao abrir e piscar a cada toque num chip de filtro — cada mudança de filtro
 * recompunha as centenas de linhas para mostrar as vinte que cabem. Ver `AppScreenLazyColumn`.
 *
 * **Enquanto a primeira carga acontece, só há o indicador.** Busca e filtros aparecem junto da
 * lista, e não antes dela: mostrar campos de refino sobre um vazio que ainda vai virar catálogo é
 * oferecer ferramentas para uma coisa que ainda não existe — e era o que produzia a sensação de a
 * tela se montar em duas etapas.
 *
 * **Depois disso, trocar a busca ou um chip repõe o indicador no lugar da lista** — e só dela: a
 * busca e os filtros ficam, porque quem acabou de fechar a lista demais precisa poder desfazer sem
 * esperar. Os chips filtram em memória e terminam antes do quadro seguinte, então o ViewModel segura
 * o estado por uma janela mínima; sem ela a lista apenas encolhia, e encolher sem aviso se lê como
 * defeito e não como resposta. Ver `CatalogViewModel.recompute`.
 *
 * **Tocar no card escolhe o exercício; tocar em "saiba como fazer" abre a ficha.** A ação principal
 * é escolher — a tela foi aberta de dentro da montagem de um dia, e mandar o treinador abrir a ficha
 * de cada um para depois voltar e tocar em outro lugar transformaria seis exercícios em dezoito
 * toques.
 *
 * **Três vazios diferentes, e essa é a razão de a tela ter estados nomeados.** Catálogo ausente é
 * "não há nada em disco, sincronize"; busca sem resultado é "não achei isso, tente outra coisa"; e
 * carregando é nenhuma das duas. Desenhar os três iguais mandaria o treinador apagar a busca para
 * resolver um problema de sincronização.
 */
@Composable
internal fun CatalogScreen(
    state: CatalogUiState,
    actions: CatalogActions,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppListScreenScaffold(
        title = stringResource(R.string.trainer_catalog_title),
        modifier = modifier,
        onBack = onBack,
        backContentDescription = stringResource(R.string.trainer_action_back),
    ) {
        when {
            // Sem espera aqui também, e pela mesma razão do indicador de refazer: quem garante que
            // ele não pisca é o `debounce` da busca, que sozinho já segura a primeira carga por
            // mais tempo do que se leva para perceber que algo apareceu. Com o meio segundo padrão
            // do componente, a leitura do Room terminava antes e a tela ia de branco para a lista
            // sem nada no meio — que é o que se lê como "apareceu do nada".
            state.loading -> item {
                AppLoadingState(
                    contentDescription = stringResource(R.string.trainer_catalog_loading),
                    delayMillis = NO_LOADING_DELAY,
                )
            }

            state.isCatalogMissing -> item {
                AppEmptyState(
                    title = stringResource(R.string.trainer_catalog_missing_title),
                    description = stringResource(R.string.trainer_catalog_missing),
                    icon = AppIcons.Workouts,
                    action = stringResource(R.string.trainer_catalog_retry),
                    onAction = actions.onRetry,
                )
            }

            else -> catalogContent(state = state, actions = actions)
        }
    }
}

/**
 * Busca, filtro e resultado — os três itens fixos do topo mais uma linha por exercício.
 *
 * Não é `@Composable`: é uma extensão de `LazyListScope`, que é o que permite a lista continuar
 * preguiçosa depois de passar por aqui. Um composable que emitisse tudo junto anularia o ganho.
 *
 * As chaves dos itens são os ids dos exercícios, e isso não é detalhe: sem elas, trocar um filtro
 * faria o Compose reaproveitar as linhas por **posição** — o segundo card viraria outro exercício
 * mantendo o estado do anterior, que é a versão sutil do mesmo piscar.
 */
private fun LazyListScope.catalogContent(state: CatalogUiState, actions: CatalogActions) {
    item(key = SEARCH_KEY) {
        AppTextField(
            value = state.query,
            onValueChange = actions.onQueryChange,
            label = stringResource(R.string.trainer_catalog_search),
            supportingText = stringResource(R.string.trainer_catalog_search_support),
        )
    }

    item(key = FILTERS_KEY) {
        CatalogFilters(state = state, actions = actions)
    }

    // Sem espera antes de aparecer, ao contrário do padrão do componente: aqui quem garante que o
    // indicador não pisca é o ViewModel, que segura o estado por uma janela mínima. Ver
    // `NO_LOADING_DELAY`.
    if (state.recomputing) {
        item(key = RECOMPUTING_KEY) {
            AppLoadingState(
                contentDescription = stringResource(R.string.trainer_catalog_filtering),
                delayMillis = NO_LOADING_DELAY,
            )
        }
        return
    }

    if (state.isEmptySearch) {
        item(key = EMPTY_KEY) {
            AppEmptyState(
                title = stringResource(R.string.trainer_catalog_no_match_title),
                description = stringResource(R.string.trainer_catalog_no_match),
                icon = AppIcons.Search,
            )
        }
        return
    }

    // A contagem existe porque os filtros são muitos e é fácil fechá-los demais sem perceber: ver
    // "12 exercícios" logo acima da lista é o que faz alguém desmarcar um chip antes de concluir
    // que o catálogo é pobre.
    item(key = COUNT_KEY) {
        Text(
            text = pluralStringResource(
                R.plurals.trainer_catalog_count,
                state.exercises.size,
                state.exercises.size,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    items(items = state.exercises, key = { it.id }) { exercise ->
        ExerciseCard(
            exercise = exercise,
            onSelect = { actions.onSelect(exercise) },
            onOpenDetail = { actions.onOpenDetail(exercise) },
        )
    }
}

/**
 * Chaves dos itens fixos do topo.
 *
 * Constantes e não literais porque uma chave repetida por engano derruba a lista em tempo de
 * execução, e o erro aponta para dentro do Compose e não para a linha que a duplicou.
 */
private const val SEARCH_KEY = "search"
private const val FILTERS_KEY = "filters"
private const val COUNT_KEY = "count"
private const val EMPTY_KEY = "empty"
private const val RECOMPUTING_KEY = "recomputing"

@Preview(name = "Catálogo · claro", showBackground = true, heightDp = 1000)
@Preview(
    name = "Catálogo · escuro",
    showBackground = true,
    heightDp = 1000,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun CatalogScreenPreview() {
    RunAndLiftTheme {
        Column {
            CatalogScreen(state = previewCatalogState(), actions = previewCatalogActions(), onBack = {})
        }
    }
}

/** A busca que não achou nada — o vazio que se distingue do catálogo ausente. */
@Preview(name = "Catálogo sem resultado · claro", showBackground = true, heightDp = 800)
@Composable
private fun CatalogEmptySearchPreview() {
    RunAndLiftTheme {
        Column {
            CatalogScreen(
                state = previewCatalogState().copy(query = "zzz", results = emptyList()),
                actions = previewCatalogActions(),
                onBack = {},
            )
        }
    }
}

/**
 * A lista sendo refeita depois de um chip.
 *
 * É o preview que mais ensina desta tela, porque é o estado que dura trezentos milissegundos no
 * aparelho: o que se confere é que a busca e os filtros **continuam ali** — quem fechou a lista
 * demais precisa poder desfazer sem esperar — e que o indicador ocupa a área da lista sem empurrar
 * o resto para fora.
 */
@Preview(name = "Catálogo refazendo a lista · claro", showBackground = true, heightDp = 600)
@Composable
private fun CatalogRecomputingPreview() {
    RunAndLiftTheme {
        Column {
            CatalogScreen(
                state = previewCatalogState().copy(
                    recomputing = true,
                    filter = CatalogFilter(muscleGroups = setOf("Peitoral")),
                ),
                actions = previewCatalogActions(),
                onBack = {},
            )
        }
    }
}
