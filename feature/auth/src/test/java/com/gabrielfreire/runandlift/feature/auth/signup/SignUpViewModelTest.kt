package com.gabrielfreire.runandlift.feature.auth.signup

import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.PrivacyConsent
import com.gabrielfreire.runandlift.feature.auth.fake.FakeAuthRepository
import com.gabrielfreire.runandlift.feature.auth.fake.FakeUserRepository
import com.gabrielfreire.runandlift.feature.auth.fake.MainDispatcherRule
import com.gabrielfreire.runandlift.feature.auth.validation.AuthFormValidation
import com.gabrielfreire.runandlift.feature.auth.validation.BirthDateError
import com.gabrielfreire.runandlift.feature.auth.validation.CrefError
import com.gabrielfreire.runandlift.feature.auth.validation.EmailError
import com.gabrielfreire.runandlift.feature.auth.validation.NameError
import com.gabrielfreire.runandlift.feature.auth.validation.PasswordError
import com.gabrielfreire.runandlift.feature.auth.validation.PhoneError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Comportamento do cadastro.
 *
 * A régua do perfil é o assunto principal: a mesma tela pede coisas diferentes de aluno e de
 * treinador, e é aqui que se afirma que ela não cobra do aluno um campo que nem exibe para ele.
 */
class SignUpViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /** Preenche o cadastro inteiro com dados válidos — o que cada teste faz é desviar de um deles. */
    private fun SignUpViewModel.fillValidForm() {
        onNameChange("Ana Ribeiro")
        onEmailChange("valido@exemplo.com")
        onPasswordChange("senha123")
        onBirthDateChange("21051990")
        onTermsChange(true)
    }

    /**
     * O mesmo formulário mais o que só o treinador precisa: celular obrigatório e registro.
     *
     * O CREF entra como **conteúdo**, sem separador, porque é assim que o campo mascarado entrega:
     * a pontuação é apresentação e entra de volta só na gravação.
     */
    private fun SignUpViewModel.fillValidTrainerForm() {
        fillValidForm()
        onPhoneChange("11912345678")
        onCrefChange("012345GSP")
    }

    @Test
    fun `exige senha com o tamanho minimo`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = SignUpViewModel(repository, FakeUserRepository())

        viewModel.fillValidForm()
        viewModel.onPasswordChange("123")
        viewModel.onSubmit()

        assertEquals(PasswordError.TOO_SHORT, viewModel.uiState.value.passwordError)
        assertEquals(0, repository.calls)
    }

    @Test
    fun `nao cria conta sem aceite dos termos`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = SignUpViewModel(repository, FakeUserRepository())

        viewModel.fillValidForm()
        viewModel.onTermsChange(false)
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertTrue("aceite é condição para a conta existir", viewModel.formState.value.termsMissing)
        assertEquals(0, repository.calls)
    }

    @Test
    fun `um envio revela todos os erros de uma vez`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = SignUpViewModel(FakeAuthRepository(), FakeUserRepository())

        viewModel.onSubmit()

        // Formulário que revela um erro por envio faz a pessoa tentar N vezes para descobrir N
        // coisas — a validação extra não pode ser curto-circuitada pela de credencial.
        assertEquals(EmailError.REQUIRED, viewModel.uiState.value.emailError)
        assertEquals(PasswordError.REQUIRED, viewModel.uiState.value.passwordError)
        assertEquals(NameError.REQUIRED, viewModel.formState.value.nameError)
        assertEquals(BirthDateError.REQUIRED, viewModel.formState.value.birthDateError)
        assertTrue(viewModel.formState.value.termsMissing)
    }

    @Test
    fun `recusa quem nao tem a idade minima`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = SignUpViewModel(repository, FakeUserRepository())
        val tooYoung = LocalDate.now().minusYears(AuthFormValidation.MIN_AGE_YEARS - 1L)

        viewModel.fillValidForm()
        viewModel.onBirthDateChange(tooYoung.format(DateTimeFormatter.ofPattern("ddMMyyyy")))
        viewModel.onSubmit()

        assertEquals(BirthDateError.TOO_YOUNG, viewModel.formState.value.birthDateError)
        assertEquals(0, repository.calls)
    }

    @Test
    fun `grava o que o formulario coletou, inclusive o consentimento`() = runTest(mainDispatcherRule.dispatcher) {
        val users = FakeUserRepository()
        val viewModel = SignUpViewModel(FakeAuthRepository(), users, ActiveRole.STUDENT)

        viewModel.fillValidForm()
        viewModel.onPhoneChange("11987654321")
        viewModel.onMarketingChange(true)
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        val details = users.lastDetails
        assertEquals("Ana Ribeiro", details?.displayName)
        assertEquals(LocalDate.of(1990, 5, 21), details?.birthDate)
        assertEquals("11987654321", details?.phone)
        assertEquals(PrivacyConsent.CURRENT_TERMS_VERSION, details?.consent?.termsVersion)
        assertEquals(true, details?.consent?.marketingOptIn)
    }

    @Test
    fun `opt-in de marketing e separado do aceite dos termos`() = runTest(mainDispatcherRule.dispatcher) {
        val users = FakeUserRepository()
        val viewModel = SignUpViewModel(FakeAuthRepository(), users, ActiveRole.STUDENT)

        viewModel.fillValidForm()
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        // Consentimento em bloco não é consentimento: aceitar os termos não pode ligar o marketing.
        assertEquals(false, users.lastDetails?.consent?.marketingOptIn)
    }

    @Test
    fun `mascara nao chega ao estado, so digito`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = SignUpViewModel(FakeAuthRepository(), FakeUserRepository())

        viewModel.onBirthDateChange("21/05/1990")
        viewModel.onPhoneChange("(11) 98765-4321")

        assertEquals("21051990", viewModel.formState.value.birthDate)
        assertEquals("11987654321", viewModel.formState.value.phone)
    }

    @Test
    fun `grava o papel escolhido antes do login`() = runTest(mainDispatcherRule.dispatcher) {
        val users = FakeUserRepository()
        val viewModel = SignUpViewModel(FakeAuthRepository(), users, ActiveRole.TRAINER)

        viewModel.fillValidTrainerForm()
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(ActiveRole.TRAINER), users.rolesAdded)
        assertEquals(
            "com o papel já gravado, perguntar de novo seria perguntar duas vezes a mesma coisa",
            ActiveRole.TRAINER,
            viewModel.uiState.value.resolvedRole,
        )
    }

    @Test
    fun `treinador nao cria conta sem registro no CREF`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = SignUpViewModel(repository, FakeUserRepository(), ActiveRole.TRAINER)

        // Tudo o que o aluno precisa, e nada do que o treinador precisa.
        viewModel.fillValidForm()
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertEquals(CrefError.REQUIRED, viewModel.formState.value.crefError)
        assertEquals("o treinador é o canal de contato", PhoneError.REQUIRED, viewModel.formState.value.phoneError)
        assertEquals(0, repository.calls)
    }

    @Test
    fun `aluno nao pede registro nem celular`() = runTest(mainDispatcherRule.dispatcher) {
        val users = FakeUserRepository()
        val viewModel = SignUpViewModel(FakeAuthRepository(), users, ActiveRole.STUDENT)

        viewModel.fillValidForm()
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        // A mesma tela, outra régua: exigir do aluno o que só o treinador precisa seria cobrar
        // por um campo que o formulário dele nem exibe.
        assertNull(viewModel.formState.value.crefError)
        assertNull(viewModel.formState.value.phoneError)
        assertTrue(viewModel.uiState.value.authenticated)
        assertNull("registro profissional não é dado de aluno", users.lastDetails?.cref)
    }

    @Test
    fun `registro do treinador e gravado com separador, mas guardado sem`() = runTest(mainDispatcherRule.dispatcher) {
        val users = FakeUserRepository()
        val viewModel = SignUpViewModel(FakeAuthRepository(), users, ActiveRole.TRAINER)

        viewModel.fillValidTrainerForm()
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        // O estado guarda o conteúdo, como em qualquer campo mascarado; os separadores entram
        // uma vez, no caminho da gravação. Duas grafias no banco seriam dois registros.
        assertEquals("012345GSP", viewModel.formState.value.cref)
        assertEquals("012345-G/SP", users.lastDetails?.cref)
    }

    @Test
    fun `sem escolha previa deixa o papel para a tela seguinte`() = runTest(mainDispatcherRule.dispatcher) {
        val users = FakeUserRepository()
        val viewModel = SignUpViewModel(FakeAuthRepository(), users, intendedRole = null)

        viewModel.fillValidForm()
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.authenticated)
        assertEquals(emptyList<ActiveRole>(), users.rolesAdded)
        assertNull(viewModel.uiState.value.resolvedRole)
        // Sem papel ainda, mas o consentimento não pode esperar pela tela seguinte: coletado e
        // não registrado é o mesmo que não ter coletado.
        assertNotNull(users.lastDetails?.consent)
    }

    @Test
    fun `falha ao gravar o papel nao derruba a conta nem repete a pergunta`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = SignUpViewModel(
            FakeAuthRepository(),
            FakeUserRepository(failWriting = true),
            ActiveRole.STUDENT,
        )

        viewModel.fillValidForm()
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        // A conta já existe: devolver falha faria a pessoa tentar de novo e ouvir "e-mail em
        // uso".
        assertTrue(viewModel.uiState.value.authenticated)
        assertNull(viewModel.uiState.value.failure)
        // E o papel já foi respondido nas boas-vindas: mandá-la escolher de novo trocaria uma
        // falha de escrita por uma pergunta repetida.
        assertEquals(ActiveRole.STUDENT, viewModel.uiState.value.resolvedRole)
    }

    @Test
    fun `cadastro por formulario nunca cai na conclusao de cadastro`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = SignUpViewModel(FakeAuthRepository(), FakeUserRepository(), ActiveRole.TRAINER)

        viewModel.fillValidTrainerForm()
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        // O formulário coletou tudo antes de a conta existir: perguntar de novo o que acabou de ser
        // respondido é o mesmo defeito da escolha de papel duplicada.
        assertEquals(false, viewModel.uiState.value.profileIncomplete)
    }
}
