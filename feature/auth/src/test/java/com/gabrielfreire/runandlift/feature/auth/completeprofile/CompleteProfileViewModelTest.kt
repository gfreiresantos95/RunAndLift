package com.gabrielfreire.runandlift.feature.auth.completeprofile

import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.PrivacyConsent
import com.gabrielfreire.runandlift.data.model.UserProfile
import com.gabrielfreire.runandlift.data.model.UserRoles
import com.gabrielfreire.runandlift.feature.auth.fake.FakeAuthRepository
import com.gabrielfreire.runandlift.feature.auth.fake.FakeUserRepository
import com.gabrielfreire.runandlift.feature.auth.fake.MainDispatcherRule
import com.gabrielfreire.runandlift.feature.auth.validation.BirthDateError
import com.gabrielfreire.runandlift.feature.auth.validation.CrefError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * Conclusão do cadastro de quem entrou pelo Google.
 *
 * A promessa da tela é **pedir só o que falta**, e é ela que os testes cobram: o que já existe
 * volta preenchido, o consentimento some para quem já consentiu, e o nome nunca é cobrado porque
 * não há campo onde consertá-lo.
 */
class CompleteProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val auth = FakeAuthRepository(signedIn = FakeAuthRepository.ACCOUNT)

    private fun profile(birthDate: LocalDate? = null, phone: String? = null, consent: String? = null) = UserProfile(
        uid = "u1",
        displayName = "Bruno Lima",
        roles = UserRoles(trainer = true),
        activeRole = ActiveRole.TRAINER,
        birthDate = birthDate,
        phone = phone,
        acceptedTermsVersion = consent,
    )

    /**
     * A regressão que motivou o conserto: **conta criada pelo Google ficava sem nome no banco**.
     *
     * O nome vinha na folha do Google, parava no SDK e nunca era gravado — esta tela é a única
     * escrita do fluxo, e ela mandava tudo menos o nome. Só ficou visível quando a home passou a
     * cumprimentar pelo nome e cumprimentou com "Olá!".
     */
    @Test
    fun `nome vindo do Google e gravado em users`() = runTest(mainDispatcherRule.dispatcher) {
        val google = FakeAuthRepository(
            signedIn = FakeAuthRepository.ACCOUNT.copy(displayName = "Ana Ribeiro"),
        )
        val users = FakeUserRepository()
        val viewModel = CompleteProfileViewModel(google, users, ActiveRole.STUDENT)
        testScheduler.advanceUntilIdle()

        viewModel.onBirthDateChange("21051990")
        viewModel.onTermsChange(true)
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertEquals("Ana Ribeiro", users.lastDetails?.displayName)
    }

    @Test
    fun `nome ja gravado tem precedencia sobre o do provedor`() = runTest(mainDispatcherRule.dispatcher) {
        val google = FakeAuthRepository(
            signedIn = FakeAuthRepository.ACCOUNT.copy(displayName = "Ana da Conta Google"),
        )
        val users = FakeUserRepository(storedProfile = profile(consent = PrivacyConsent.CURRENT_TERMS_VERSION))
        val viewModel = CompleteProfileViewModel(google, users, ActiveRole.STUDENT)
        testScheduler.advanceUntilIdle()

        // Quem editou o próprio nome não pode vê-lo voltar ao da conta Google só por passar por
        // esta tela de novo.
        assertEquals("Bruno Lima", viewModel.uiState.value.name)
    }

    @Test
    fun `conta sem nome no provedor nao inventa nome`() = runTest(mainDispatcherRule.dispatcher) {
        val users = FakeUserRepository()
        val viewModel = CompleteProfileViewModel(auth, users, ActiveRole.STUDENT)
        testScheduler.advanceUntilIdle()

        viewModel.onBirthDateChange("21051990")
        viewModel.onTermsChange(true)
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        // Sem nome em lugar nenhum, o campo fica vazio no banco em vez de receber um derivado do
        // e-mail: um nome que a pessoa nunca escolheu apareceria na home como se fosse dela.
        assertNull(users.lastDetails?.displayName)
    }

    @Test
    fun `o que ja esta gravado volta preenchido no campo`() = runTest(mainDispatcherRule.dispatcher) {
        val users = FakeUserRepository(
            storedProfile = profile(birthDate = LocalDate.of(1988, 3, 14), phone = "11912345678"),
            storedCref = "012345-G/SP",
        )
        val viewModel = CompleteProfileViewModel(auth, users, ActiveRole.TRAINER)
        testScheduler.advanceUntilIdle()

        // Recomeçar do zero um dado que já existe é pedir de novo o que já foi dado. E volta na
        // forma que o campo mascarado entende — conteúdo, sem separador.
        assertEquals("14031988", viewModel.formState.value.birthDate)
        assertEquals("11912345678", viewModel.formState.value.phone)
        assertEquals("012345GSP", viewModel.formState.value.cref)
    }

    @Test
    fun `quem ja consentiu nao e perguntado de novo`() = runTest(mainDispatcherRule.dispatcher) {
        val users = FakeUserRepository(storedProfile = profile(consent = PrivacyConsent.CURRENT_TERMS_VERSION))
        val viewModel = CompleteProfileViewModel(auth, users, ActiveRole.TRAINER)
        testScheduler.advanceUntilIdle()

        assertFalse("repetir o pedido a quem já consentiu não o torna mais válido", viewModel.uiState.value.askConsent)
    }

    @Test
    fun `sem consentimento registrado, o bloco aparece`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = CompleteProfileViewModel(auth, FakeUserRepository(), ActiveRole.STUDENT)
        testScheduler.advanceUntilIdle()

        // Consentimento coletado por um provedor de identidade não é consentimento dado a nós.
        assertTrue(viewModel.uiState.value.askConsent)
    }

    @Test
    fun `o nome do provedor e exibido como confirmacao`() = runTest(mainDispatcherRule.dispatcher) {
        val users = FakeUserRepository(storedProfile = profile())
        val viewModel = CompleteProfileViewModel(auth, users, ActiveRole.TRAINER)
        testScheduler.advanceUntilIdle()

        assertEquals("Bruno Lima", viewModel.uiState.value.name)
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `nao cobra o nome, porque nao ha campo onde conserta-lo`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = CompleteProfileViewModel(auth, FakeUserRepository(), ActiveRole.STUDENT)
        testScheduler.advanceUntilIdle()

        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        // O nome veio do provedor e a tela não o exibe: validá-lo produziria um erro sem conserto.
        assertNull(viewModel.formState.value.nameError)
        assertEquals(BirthDateError.REQUIRED, viewModel.formState.value.birthDateError)
    }

    @Test
    fun `treinador ainda precisa do registro aqui`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = CompleteProfileViewModel(auth, FakeUserRepository(), ActiveRole.TRAINER)
        testScheduler.advanceUntilIdle()

        viewModel.onBirthDateChange("14031988")
        viewModel.onPhoneChange("11912345678")
        viewModel.onTermsChange(true)
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertEquals(CrefError.REQUIRED, viewModel.formState.value.crefError)
        assertNull("nada foi gravado com o formulário inválido", viewModel.uiState.value.completedRole)
    }

    @Test
    fun `conclusao grava o papel junto com o resto, numa escrita so`() = runTest(mainDispatcherRule.dispatcher) {
        val users = FakeUserRepository()
        val viewModel = CompleteProfileViewModel(auth, users, ActiveRole.TRAINER)
        testScheduler.advanceUntilIdle()

        viewModel.onBirthDateChange("14031988")
        viewModel.onPhoneChange("11912345678")
        viewModel.onCrefChange("012345GSP")
        viewModel.onTermsChange(true)
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        // É o que dispensa a tela de escolha depois do Google.
        assertEquals(listOf(ActiveRole.TRAINER), users.rolesAdded)
        assertEquals(ActiveRole.TRAINER, viewModel.uiState.value.completedRole)
        assertEquals("012345-G/SP", users.lastDetails?.cref)
        assertEquals(PrivacyConsent.CURRENT_TERMS_VERSION, users.lastDetails?.consent?.termsVersion)
    }

    @Test
    fun `reenvia o nome ja gravado, sem trocar por outro`() = runTest(mainDispatcherRule.dispatcher) {
        val users = FakeUserRepository(storedProfile = profile())
        val viewModel = CompleteProfileViewModel(auth, users, ActiveRole.STUDENT)
        testScheduler.advanceUntilIdle()

        viewModel.onBirthDateChange("21051990")
        viewModel.onTermsChange(true)
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        // Este teste afirmava o contrário — que a tela não mandava nome nenhum, "porque o
        // repositório preserva o que existe". A premissa estava certa sobre o repositório e errada
        // sobre a origem: nada gravava o nome do provedor, então não havia o que preservar. Agora
        // o nome vai junto, e o que ele carrega é o **mesmo** que já estava lá.
        assertEquals("Bruno Lima", users.lastDetails?.displayName)
    }

    @Test
    fun `falha de escrita nao deixa passar`() = runTest(mainDispatcherRule.dispatcher) {
        val users = FakeUserRepository(failWriting = true)
        val viewModel = CompleteProfileViewModel(auth, users, ActiveRole.STUDENT)
        testScheduler.advanceUntilIdle()

        viewModel.onBirthDateChange("21051990")
        viewModel.onTermsChange(true)
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        // Ao contrário do cadastro por formulário, aqui a conta já existe e nada se perde em tentar
        // de novo. Deixar seguir produziria exatamente a conta pela metade que a tela impede.
        assertTrue(viewModel.uiState.value.failed)
        assertNull(viewModel.uiState.value.completedRole)
    }

    @Test
    fun `envio duplicado nao grava duas vezes`() = runTest(mainDispatcherRule.dispatcher) {
        val users = FakeUserRepository()
        val viewModel = CompleteProfileViewModel(auth, users, ActiveRole.STUDENT)
        testScheduler.advanceUntilIdle()

        viewModel.onBirthDateChange("21051990")
        viewModel.onTermsChange(true)
        viewModel.onSubmit()
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(ActiveRole.STUDENT), users.rolesAdded)
    }

    @Test
    fun `leitura que falha ainda deixa a tela utilizavel`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = CompleteProfileViewModel(auth, FakeUserRepository(failReading = true), ActiveRole.STUDENT)
        testScheduler.advanceUntilIdle()

        // Sem conseguir ler, a tela abre vazia em vez de travar em carregamento — pedir de novo é
        // pior que pedir a mais, mas ficar preso na abertura é pior que os dois.
        assertFalse(viewModel.uiState.value.loading)
        assertEquals("", viewModel.formState.value.birthDate)
    }
}
