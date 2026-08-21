package com.gabrielfreire.runandlift.feature.trainer.assign

import com.gabrielfreire.runandlift.data.model.Assignment
import com.gabrielfreire.runandlift.data.model.LinkStatus
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeAssignmentRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeAuthRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeLinkRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeLinkRepository.Companion.link
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeProgramRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeProgramRepository.Companion.program
import com.gabrielfreire.runandlift.feature.trainer.fake.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * O que o preview da tela de atribuir não mostra: quem entra na lista, o que a escrita congela e o
 * que sobra na tela quando ela não vai.
 *
 * A asserção que mais importa é a da cópia: **atribuir congela os dias**, e é isso que permite ao
 * aluno ler o próprio treino sem poder ler a coleção de programas. Se a prescrição gravada
 * apontasse para o molde em vez de carregá-lo, nada aqui falharia — e o aluno abriria a academia
 * com uma tela vazia.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AssignViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `so aluno com vinculo ativo entra na lista`() = runTest {
        // Pausado e encerrado não recebem prescrição, e não é decisão de tela: a regra do Firestore
        // exige vínculo ativo para criar a atribuição. Mostrá-los seria oferecer um botão recusado.
        val links = FakeLinkRepository(
            links = listOf(
                link(LinkStatus.ACTIVE),
                link(LinkStatus.PAUSED, name = "Bruno"),
                link(LinkStatus.ENDED, name = "Caio"),
            ),
        )
        val viewModel = viewModel(links = links)

        advanceUntilIdle()

        assertEquals(listOf("Ana Souza"), viewModel.uiState.value.students.map { it.studentName })
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `os alunos vem em ordem alfabetica`() = runTest {
        val links = FakeLinkRepository(
            links = listOf(link(LinkStatus.ACTIVE, name = "Zeca"), link(LinkStatus.ACTIVE, name = "Ana Souza")),
        )
        val viewModel = viewModel(links = links)

        advanceUntilIdle()

        assertEquals(listOf("Ana Souza", "Zeca"), viewModel.uiState.value.students.map { it.studentName })
    }

    @Test
    fun `comeca carregando antes de a leitura terminar`() = runTest {
        val viewModel = viewModel()

        // Sem advanceUntilIdle de propósito: é o primeiro frame, com a tela já desenhada.
        assertTrue(viewModel.uiState.value.loading)
    }

    @Test
    fun `leitura que falha vira falha, e nao lista vazia`() = runTest {
        val viewModel = viewModel(links = FakeLinkRepository(failReading = true))

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.failed)
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `sem sessao nao ha carteira, e a tela nao fica presa carregando`() = runTest {
        val viewModel = AssignViewModel(
            authRepository = FakeAuthRepository(signedIn = null),
            linkRepository = FakeLinkRepository(links = listOf(link(LinkStatus.ACTIVE))),
            programRepository = FakeProgramRepository(programs = listOf(program())),
            assignmentRepository = FakeAssignmentRepository(),
            programId = "p1",
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.failed)
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `atribuir congela uma copia dos dias do programa`() = runTest {
        val assignments = FakeAssignmentRepository()
        val viewModel = viewModel(assignments = assignments)
        advanceUntilIdle()

        viewModel.onAssign(link(LinkStatus.ACTIVE))
        advanceUntilIdle()

        val gravada = assignments.assigned!!

        assertEquals("Treino ABC", gravada.programName)
        assertEquals("p1", gravada.programId)
        assertEquals(program().days, gravada.days)
        assertEquals("Ana Souza", gravada.studentName)
    }

    @Test
    fun `atribuir marca o aluno como ja atendido`() = runTest {
        val aluno = link(LinkStatus.ACTIVE)
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onAssign(aluno)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isAssigned(aluno))
        assertNull(viewModel.uiState.value.assigning)
        assertFalse(viewModel.uiState.value.assignFailed)
    }

    @Test
    fun `quem ja tem o programa aparece marcado ao abrir`() = runTest {
        val aluno = link(LinkStatus.ACTIVE)
        val assignments = FakeAssignmentRepository(assignments = listOf(assignmentOf(aluno.studentId)))
        val viewModel = viewModel(assignments = assignments)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isAssigned(aluno))
    }

    @Test
    fun `atribuir sem rede acende o aviso e a linha volta ao que era`() = runTest {
        val aluno = link(LinkStatus.ACTIVE)
        val viewModel = viewModel(assignments = FakeAssignmentRepository(failWriting = true))
        advanceUntilIdle()

        viewModel.onAssign(aluno)
        advanceUntilIdle()

        // Sem rede não há como prescrever: a fila durável (E0-04) não existe, e fingir que gravou
        // seria pior do que recusar.
        assertTrue(viewModel.uiState.value.assignFailed)
        assertFalse(viewModel.uiState.value.isAssigned(aluno))
        assertNull(viewModel.uiState.value.assigning)
    }

    @Test
    fun `encerrar tira o aluno de quem esta com o programa`() = runTest {
        val aluno = link(LinkStatus.ACTIVE)
        val assignments = FakeAssignmentRepository(assignments = listOf(assignmentOf(aluno.studentId)))
        val viewModel = viewModel(assignments = assignments)
        advanceUntilIdle()

        viewModel.onRemove(aluno)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isAssigned(aluno))
        assertEquals(1, assignments.writeCount)
    }

    @Test
    fun `encerrar que falha deixa o aluno onde estava`() = runTest {
        val aluno = link(LinkStatus.ACTIVE)
        val assignments = FakeAssignmentRepository(
            assignments = listOf(assignmentOf(aluno.studentId)),
            failWriting = true,
        )
        val viewModel = viewModel(assignments = assignments)
        advanceUntilIdle()

        viewModel.onRemove(aluno)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.assignFailed)
        assertTrue(viewModel.uiState.value.isAssigned(aluno))
    }

    @Test
    fun `uma escrita por vez`() = runTest {
        val assignments = FakeAssignmentRepository()
        val viewModel = viewModel(assignments = assignments)
        advanceUntilIdle()

        viewModel.onAssign(link(LinkStatus.ACTIVE))
        // Sem esperar a primeira terminar: dois toques não podem virar duas escritas.
        viewModel.onAssign(link(LinkStatus.ACTIVE, name = "Bruno"))
        advanceUntilIdle()

        assertEquals(1, assignments.writeCount)
    }

    @Test
    fun `sem programa lido nao ha o que atribuir`() = runTest {
        // A leitura falhou e a tela está no estado de erro: tocar numa linha que nem devia estar
        // desenhada não pode gravar uma prescrição de programa nenhum.
        val assignments = FakeAssignmentRepository()
        val viewModel = viewModel(programs = FakeProgramRepository(failReading = true), assignments = assignments)
        advanceUntilIdle()

        viewModel.onAssign(link(LinkStatus.ACTIVE))
        viewModel.onRemove(link(LinkStatus.ACTIVE))
        advanceUntilIdle()

        assertEquals(0, assignments.writeCount)
    }

    @Test
    fun `tentar de novo reconstroi a tela depois de a rede voltar`() = runTest {
        val links = FakeLinkRepository(links = listOf(link(LinkStatus.ACTIVE)), failReading = true)
        val viewModel = viewModel(links = links)
        advanceUntilIdle()

        links.failReading = false
        viewModel.refresh()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.failed)
        assertEquals(1, viewModel.uiState.value.students.size)
    }

    private fun assignmentOf(studentId: String) = Assignment.from(
        program = program(),
        studentId = studentId,
        studentName = "Ana Souza",
    )

    private fun viewModel(
        links: FakeLinkRepository = FakeLinkRepository(links = listOf(link(LinkStatus.ACTIVE))),
        programs: FakeProgramRepository = FakeProgramRepository(programs = listOf(program())),
        assignments: FakeAssignmentRepository = FakeAssignmentRepository(),
    ) = AssignViewModel(
        authRepository = FakeAuthRepository(),
        linkRepository = links,
        programRepository = programs,
        assignmentRepository = assignments,
        programId = "p1",
    )
}
