package com.gabrielfreire.runandlift.feature.auth.completeprofile

import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.PrivacyConsent
import com.gabrielfreire.runandlift.data.model.UserProfile
import com.gabrielfreire.runandlift.data.model.UserRoles
import com.gabrielfreire.runandlift.feature.auth.fake.FakeUserRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * O que falta numa conta para ela poder ser usada.
 *
 * Duas regras aqui são invisíveis na interface e caras se quebrarem: leitura que falha responde
 * "não falta nada", e a régua do treinador é mais exigente que a do aluno.
 */
class ProfileCompletionTest {

    private fun profile(
        birthDate: LocalDate? = LocalDate.of(1990, 5, 21),
        phone: String? = "11987654321",
        consent: String? = PrivacyConsent.CURRENT_TERMS_VERSION,
    ) = UserProfile(
        uid = "u1",
        displayName = "Ana Ribeiro",
        roles = UserRoles(student = true),
        activeRole = ActiveRole.STUDENT,
        birthDate = birthDate,
        phone = phone,
        acceptedTermsVersion = consent,
    )

    @Test
    fun `leitura que falha responde que nao falta nada`() = runTest {
        val users = FakeUserRepository(failReading = true)

        val missing = ProfileCompletion.missing(users, "u1", ActiveRole.TRAINER)

        // Sem rede e sem cache não dá para afirmar que a conta está incompleta. Prender quem só
        // quer treinar por causa de um palpite é pior do que deixar passar um cadastro pela
        // metade, que a próxima abertura online cobra.
        assertFalse("nunca bloquear alguém com base num palpite", missing.any)
    }

    @Test
    fun `conta sem documento nenhum precisa de tudo o que o papel exige`() = runTest {
        val users = FakeUserRepository()

        val missing = ProfileCompletion.missing(users, "u1", ActiveRole.TRAINER)

        assertTrue(missing.birthDate)
        assertTrue(missing.phone)
        assertTrue(missing.cref)
        assertTrue(missing.consent)
    }

    @Test
    fun `aluno nao precisa de celular nem de registro`() = runTest {
        val users = FakeUserRepository(storedProfile = profile(phone = null))

        val missing = ProfileCompletion.missing(users, "u1", ActiveRole.STUDENT)

        // Cobrar do aluno o que só o treinador precisa seria pagar uma leitura por um documento
        // que a conta dele nem tem, e pedir um campo que o formulário dele nunca exibiu.
        assertFalse(missing.phone)
        assertFalse(missing.cref)
        assertFalse(missing.any)
    }

    @Test
    fun `treinador sem registro esta incompleto`() = runTest {
        val users = FakeUserRepository(storedProfile = profile(), storedCref = null)

        val missing = ProfileCompletion.missing(users, "u1", ActiveRole.TRAINER)

        // Prescrever é atividade privativa de profissional registrado (Lei 9.696/1998): conta de
        // treinador sem registro não pode fazer o que a tela seguinte oferece.
        assertTrue(missing.cref)
        assertTrue(missing.any)
    }

    @Test
    fun `treinador completo nao falta nada`() = runTest {
        val users = FakeUserRepository(storedProfile = profile(), storedCref = "012345-G/SP")

        val missing = ProfileCompletion.missing(users, "u1", ActiveRole.TRAINER)

        assertFalse(missing.any)
    }

    @Test
    fun `aceite antigo conta como aceite`() = runTest {
        val users = FakeUserRepository(storedProfile = profile(consent = "2020-01-01"))

        val missing = ProfileCompletion.missing(users, "u1", ActiveRole.STUDENT)

        // Só a ausência conta. Termos novos são um pedido de re-consentimento, que é outro assunto
        // — tratá-los aqui viraria um bloqueio de acesso para a base inteira a cada revisão do
        // texto jurídico.
        assertFalse(missing.consent)
    }

    @Test
    fun `nascimento ausente basta para estar incompleto`() = runTest {
        val users = FakeUserRepository(storedProfile = profile(birthDate = null))

        val missing = ProfileCompletion.missing(users, "u1", ActiveRole.STUDENT)

        assertTrue(missing.birthDate)
        assertTrue(missing.any)
    }

    @Test
    fun `nada faltando e o padrao de MissingProfileData`() {
        assertEquals(false, MissingProfileData().any)
    }
}
