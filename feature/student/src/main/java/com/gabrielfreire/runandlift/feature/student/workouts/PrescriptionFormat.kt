package com.gabrielfreire.runandlift.feature.student.workouts

/**
 * Os números de uma prescrição virando os pedaços de texto que a linha mostra.
 *
 * **É a segunda cópia desta regra** — a primeira é o `PrescriptionRow` do `:feature:trainer`, e os
 * dois módulos não se veem por decisão de arquitetura. O gatilho de extração do projeto é a terceira
 * cópia; quando ela vier, o lugar não é o `:core` (que não conhece `:data` nem prescrição) e sim um
 * `PrescribedExercise` que saiba se formatar, o que hoje esbarra em `:data` não ter `strings.xml`.
 *
 * Aqui a regra sai do composable, ao contrário do lado do treinador, porque **é isto que um teste
 * comum de JVM alcança**: a escolha entre faixa e número fixo e o arredondamento da carga são as
 * duas decisões desta tela, e nenhuma delas se confere abrindo um `@Preview`.
 */
internal object PrescriptionFormat {

    /**
     * Carga sem casa decimal quando ela é inteira.
     *
     * "60 kg" e não "60,0 kg": a segunda forma sugere uma precisão que a anilha da academia não tem,
     * e ocupa espaço numa linha que já é densa. A meia casa sobrevive porque existe de verdade —
     * 62,5 kg é a soma de duas anilhas de 1,25 — e vem com **vírgula**, que é como se escreve carga
     * em português.
     */
    fun load(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else value.toString().replace('.', ',')

    /**
     * Junta os pedaços com o separador da linha de números.
     *
     * Os nulos caem fora em vez de virarem "sem carga": ocupar espaço para dizer que não há o que
     * dizer é o que transforma uma linha legível de relance numa frase.
     */
    fun summary(parts: List<String?>): String = parts.filterNotNull().joinToString(SEPARATOR)

    private const val SEPARATOR = " · "
}
