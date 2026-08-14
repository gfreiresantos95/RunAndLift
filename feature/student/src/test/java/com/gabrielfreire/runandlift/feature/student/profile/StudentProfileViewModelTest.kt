package com.gabrielfreire.runandlift.feature.student.profile

import com.gabrielfreire.runandlift.data.model.HealthDataConsent
import com.gabrielfreire.runandlift.data.model.StudentProfile
import com.gabrielfreire.runandlift.data.model.TrainingLevel
import com.gabrielfreire.runandlift.feature.student.fake.FakeAuthRepository
import com.gabrielfreire.runandlift.feature.student.fake.FakeStudentRepository
import com.gabrielfreire.runandlift.feature.student.fake.FakeUserRepository
import com.gabrielfreire.runandlift.feature.student.fake.MainDispatcherRule
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
 * A edição de perfil — a segunda chance do que o onboarding deixou passar.
 *
 * Duas regras que só aqui aparecem: o consentimento **não é recarimbado** a cada gravação, e o
 * e-mail chega à tela como leitura, vindo da conta e não do formulário.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StudentProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `o que ja esta gravado volta preenchido`() = runTest {
        val viewModel = viewModel(students = FakeStudentRepository(FakeStudentRepository.complete()))
        advanceUntilIdle()

        assertEquals(TrainingLevel.INTERMEDIATE, viewModel.formState.value.level)
        assertEquals("72,5", viewModel.formState.value.weight)
        assertEquals("175", viewModel.formState.value.height)
        assertTrue(viewModel.formState.value.healthConsent)
    }

    @Test
    fun `consentimento ja dado nao e recarimbado`() = runTest {
        val students = FakeStudentRepository(FakeStudentRepository.complete())
        val viewModel = viewModel(students = students)
        advanceUntilIdle()

        viewModel.formActions.onRestrictionsChange("Joelho esquerdo")
        viewModel.onSubmit()
        advanceUntilIdle()

        // Reenviá-lo carimbaria uma data de aceite nova a cada edição, apagando quando ele de fato
        // aconteceu — que é justamente o que a LGPD manda saber provar.
        assertNull(students.lastDetails?.healthConsent)
    }

    @Test
    fun `aceitar agora envia o consentimento`() = runTest {
        val students = FakeStudentRepository(StudentProfile(uid = "u1"))
        val viewModel = viewModel(students = students)
        advanceUntilIdle()

        viewModel.formActions.onHealthConsentChange(true)
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(HealthDataConsent.CURRENT_VERSION, students.lastDetails?.healthConsent?.version)
    }

    @Test
    fun `dias vazios na edicao sao gravados como escolha`() = runTest {
        val students = FakeStudentRepository(FakeStudentRepository.complete())
        val viewModel = viewModel(students = students)
        advanceUntilIdle()

        viewModel.formActions.onDayToggle(java.time.DayOfWeek.MONDAY)
        viewModel.formActions.onDayToggle(java.time.DayOfWeek.WEDNESDAY)
        viewModel.onSubmit()
        advanceUntilIdle()

        // Diferente do onboarding: aqui a pergunta está na tela, e não há como "pular" um campo
        // que se está olhando — esvaziar é uma resposta.
        assertEquals(emptySet<java.time.DayOfWeek>(), students.lastDetails?.availableDays)
    }

    @Test
    fun `peso invalido nao grava`() = runTest {
        val students = FakeStudentRepository(FakeStudentRepository.complete())
        val viewModel = viewModel(students = students)
        advanceUntilIdle()

        viewModel.formActions.onWeightChange("7")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(0, students.saveCount)
        assertFalse(viewModel.uiState.value.saved)
    }

    @Test
    fun `gravacao que falha nao fecha a tela`() = runTest {
        val viewModel = viewModel(students = FakeStudentRepository(failWriting = true))
        advanceUntilIdle()

        viewModel.onSubmit()
        advanceUntilIdle()

        // Diferente do onboarding: quem veio corrigir um dado precisa saber se a correção pegou.
        assertTrue(viewModel.uiState.value.failed)
        assertFalse(viewModel.uiState.value.saved)
    }

    private fun viewModel(
        auth: FakeAuthRepository = FakeAuthRepository(),
        users: FakeUserRepository = FakeUserRepository(),
        students: FakeStudentRepository = FakeStudentRepository(),
    ) = StudentProfileViewModel(authRepository = auth, userRepository = users, studentRepository = students)
}
