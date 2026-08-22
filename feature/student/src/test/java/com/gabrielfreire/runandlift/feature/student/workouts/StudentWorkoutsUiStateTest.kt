package com.gabrielfreire.runandlift.feature.student.workouts

import com.gabrielfreire.runandlift.feature.student.fake.FakeAssignmentRepository.Companion.assignment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * As perguntas que a aba e o dia fazem ao estado antes de se desenharem.
 *
 * Duas decisões moram aqui, e nenhuma se confere abrindo um `@Preview`. A primeira é que **falha
 * nunca conta como vazio** — a fixture do preview desenha um estado escolhido à mão, e é o `isEmpty`
 * que decide qual dos dois a tela de verdade mostra. A segunda é que **posição fora da lista devolve
 * nulo em vez de estourar**: o índice vem por argumento de navegação e continua valendo depois de o
 * treinador reatribuir um programa mais curto.
 */
class StudentWorkoutsUiStateTest {

    @Test
    fun `sem treino e sem falha a aba esta vazia`() {
        assertTrue(StudentWorkoutsUiState(loading = false).isEmpty)
    }

    @Test
    fun `falha de leitura nunca conta como aba vazia`() {
        // Vale mesmo sem treino nenhum em memória: com a leitura quebrada, ninguém sabe se há treino
        // — e "seu treinador ainda não montou" é uma afirmação, não um talvez.
        assertFalse(StudentWorkoutsUiState(loading = false, failed = true).isEmpty)
        assertFalse(StudentWorkoutsUiState(loading = false, failed = true, assignment = assignment()).isEmpty)
    }

    @Test
    fun `com treino a aba nao esta vazia`() {
        assertFalse(StudentWorkoutsUiState(loading = false, assignment = assignment()).isEmpty)
    }

    @Test
    fun `os dias saem da copia congelada, na ordem do treinador`() {
        val state = StudentWorkoutsUiState(loading = false, assignment = assignment())

        assertEquals(listOf("A", "B", "C"), state.days.map { it.label })
    }

    @Test
    fun `sem treino nao ha dia nenhum`() {
        assertEquals(emptyList<String>(), StudentWorkoutsUiState(loading = false).days)
    }

    @Test
    fun `a posicao devolve o dia daquela posicao`() {
        val state = StudentWorkoutsUiState(loading = false, assignment = assignment())

        assertEquals("B", state.day(1)?.label)
    }

    @Test
    fun `posicao fora da lista devolve nulo em vez de estourar`() {
        // O treinador reatribuiu um programa mais curto com a tela aberta, ou o processo foi
        // recriado com a rota antiga. As duas acontecem, e nenhuma pode fechar o app.
        val state = StudentWorkoutsUiState(loading = false, assignment = assignment())

        assertNull(state.day(3))
        assertNull(state.day(-1))
    }

    @Test
    fun `sem treino qualquer posicao e nula`() {
        assertNull(StudentWorkoutsUiState(loading = false).day(0))
    }
}
