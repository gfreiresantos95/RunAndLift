package com.gabrielfreire.runandlift.feature.trainer.catalog

import com.gabrielfreire.runandlift.data.model.Exercise
import com.gabrielfreire.runandlift.data.model.ExerciseCategory
import com.gabrielfreire.runandlift.data.model.TrainingLevel

/**
 * Os filtros do catálogo, e o que eles fazem com uma lista.
 *
 * **Moram fora do composable de propósito.** A busca por texto acontece no SQLite — é
 * `ExerciseRepository.search`, que já existe e já roda sem tocar a rede —, mas os chips filtram em
 * memória, e essa é a parte que tem regra: "nenhum chip marcado significa todos" é o oposto do que
 * um `filter` ingênuo faz, e é exatamente o tipo de coisa que só se descobre errada quando a tela
 * abre vazia.
 *
 * A lista já vem do banco com no máximo algumas centenas de itens; filtrá-la em memória custa nada e
 * permite que um teste comum de JVM afirme cada regra sem instanciar Room nem tela.
 *
 * @param categories vazio quer dizer **todas**, e não nenhuma. É a diferença entre uma tela que abre
 *   mostrando o catálogo e uma que abre em branco esperando o primeiro toque.
 * @param muscleGroups os grupos marcados. Um exercício entra se **qualquer** um deles for músculo
 *   primário dele — filtro de músculo é "quero peito ou ombro", nunca "quero os dois ao mesmo
 *   tempo", que devolveria quase nada.
 */
internal data class CatalogFilter(
    val categories: Set<ExerciseCategory> = emptySet(),
    val muscleGroups: Set<String> = emptySet(),
    val equipment: Set<String> = emptySet(),
    val levels: Set<TrainingLevel> = emptySet(),
) {

    /** Se algum filtro está marcado — o que decide se a tela oferece "limpar filtros". */
    val isActive: Boolean
        get() = categories.isNotEmpty() || muscleGroups.isNotEmpty() ||
            equipment.isNotEmpty() || levels.isNotEmpty()

    /** Aplica os quatro filtros. Conjunto vazio não filtra nada. */
    fun apply(exercises: List<Exercise>): List<Exercise> = exercises.filter { exercise ->
        matchesCategory(exercise) && matchesMuscle(exercise) &&
            matchesEquipment(exercise) && matchesLevel(exercise)
    }

    private fun matchesCategory(exercise: Exercise): Boolean = categories.isEmpty() || exercise.category in categories

    private fun matchesMuscle(exercise: Exercise): Boolean =
        muscleGroups.isEmpty() || exercise.muscleGroups.any { it in muscleGroups }

    /**
     * Exercício sem equipamento declarado **não** casa com filtro de equipamento nenhum.
     *
     * São 77 no catálogo, e quase todos são peso do corpo mal classificados na origem. Deixá-los
     * passar por todo filtro poria flexão de braço na lista de quem procurou "barra", que é pior do
     * que não encontrá-los — quem quer peso do corpo tem o chip próprio.
     */
    private fun matchesEquipment(exercise: Exercise): Boolean = equipment.isEmpty() || exercise.equipment in equipment

    private fun matchesLevel(exercise: Exercise): Boolean = levels.isEmpty() || exercise.level in levels

    /**
     * Marca ou desmarca um valor num conjunto.
     *
     * Está aqui, e não na tela, porque é a mesma operação para os quatro filtros — e quatro cópias
     * de "se está dentro tire, senão ponha" é onde uma delas vira "ponha sempre".
     */
    companion object {
        fun <T> Set<T>.toggled(value: T): Set<T> = if (value in this) this - value else this + value
    }
}
