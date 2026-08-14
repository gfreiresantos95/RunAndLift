package com.gabrielfreire.runandlift.feature.auth.profileform

import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.BrazilState
import com.gabrielfreire.runandlift.data.model.PrivacyConsent
import com.gabrielfreire.runandlift.data.model.UserProfile
import com.gabrielfreire.runandlift.data.model.UserRoles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/**
 * A tradução entre o formulário e o que a camada de dados grava, nos dois sentidos.
 *
 * Vale testar as três funções juntas porque a pergunta que alguém faz ao mexer aqui é sempre a
 * mesma: **onde as duas conversões divergem?** A resposta é um campo só — o nome — e é isso que os
 * dois primeiros testes fixam.
 */
class ProfileFormDetailsTest {

    private val filled = ProfileFormState(
        name = "Ana Ribeiro",
        birthDate = "21051990",
        phone = "11987654321",
        cref = "012345GSP",
        acceptedTerms = true,
    )

    @Test
    fun `cadastro manda o nome do formulario`() {
        val details = filled.toSignUpDetails(isTrainer = false)

        assertEquals("Ana Ribeiro", details.displayName)
    }

    @Test
    fun `conclusao manda o nome do provedor, e nao o do formulario`() {
        val details = filled.toCompletionDetails(isTrainer = false, providerName = "Ana do Google")

        // A tela de conclusão não pergunta nome: o do formulário não foi digitado por ninguém ali.
        // Esta é a única escrita do fluxo do Google, e antes ela não mandava nome nenhum — o que
        // deixava a conta criada pelo Google sem nome em users/{uid} para sempre.
        assertEquals("Ana do Google", details.displayName)
    }

    @Test
    fun `conclusao sem nome de provedor nao grava nome`() {
        assertNull(filled.toCompletionDetails(isTrainer = false, providerName = null).displayName)
    }

    @Test
    fun `nome de provedor em branco conta como ausente`() {
        // Gravar "" esconderia a ausência atrás de um valor, e o app não teria como distinguir
        // "sem nome" de "nome vazio de propósito".
        assertNull(filled.toCompletionDetails(isTrainer = false, providerName = "   ").displayName)
    }

    @Test
    fun `nome vazio nao vira apelido do e-mail`() {
        val details = filled.copy(name = "   ").toSignUpDetails(isTrainer = false)

        // Este teste afirmava o contrário: que `ana@exemplo.com` virava o nome "ana". O caminho
        // que justificava a queda não passa por aqui — o cadastro por formulário exige nome e
        // sobrenome antes de enviar —, e o que ela produzia era um nome que ninguém escolheu,
        // gravado como se a pessoa o tivesse informado e exibido assim na lista do treinador.
        assertNull(details.displayName)
    }

    @Test
    fun `registro sai em forma canonica, e so para treinador`() {
        assertEquals("012345-G/SP", filled.toSignUpDetails(isTrainer = true).cref)
        assertNull("registro profissional não é dado de aluno", filled.toSignUpDetails(isTrainer = false).cref)
        assertEquals("012345-G/SP", filled.toCompletionDetails(isTrainer = true, providerName = null).cref)
    }

    @Test
    fun `registro incompleto nao vira gravacao`() {
        val details = filled.copy(cref = "012345").toSignUpDetails(isTrainer = true)

        // Meia gravação de um registro é pior que nenhuma: ela parece um dado conferível.
        assertNull(details.cref)
    }

    @Test
    fun `nascimento vira data, e celular vazio vira nulo`() {
        val details = filled.copy(phone = "").toSignUpDetails(isTrainer = false)

        assertEquals(LocalDate.of(1990, 5, 21), details.birthDate)
        // Campo nulo não é escrito no Firestore — é o que evita uma gravação parcial apagar o que
        // já estava lá.
        assertNull(details.phone)
    }

    @Test
    fun `consentimento so existe se a caixa foi marcada`() {
        val accepted = filled.toSignUpDetails(isTrainer = false).consent
        val refused = filled.copy(acceptedTerms = false).toSignUpDetails(isTrainer = false).consent

        assertEquals(PrivacyConsent.CURRENT_TERMS_VERSION, accepted?.termsVersion)
        assertNull("registrar um aceite que não aconteceu é pior que não registrar nada", refused)
    }

    @Test
    fun `o que ja esta gravado volta na forma que o campo mascarado entende`() {
        val profile = UserProfile(
            uid = "u1",
            displayName = "Bruno Lima",
            roles = UserRoles(trainer = true),
            activeRole = ActiveRole.TRAINER,
            birthDate = LocalDate.of(1988, 3, 14),
            phone = "11912345678",
            state = "SP",
            city = "Campinas",
        )

        val prefilled = ProfileFormState().prefilledFrom(
            profile = profile,
            registration = "012345-G/SP",
            state = BrazilState(uf = "SP", name = "São Paulo"),
        )

        assertEquals("14031988", prefilled.birthDate)
        assertEquals("11912345678", prefilled.phone)
        assertEquals("012345GSP", prefilled.cref)
        // O estado volta inteiro — sigla para gravar, nome para desenhar "São Paulo - SP".
        assertEquals("São Paulo - SP", prefilled.selectedState?.label)
        assertEquals("Campinas", prefilled.city)
    }

    @Test
    fun `cidade nao volta sozinha quando o estado nao pode ser resolvido`() {
        val profile = UserProfile(
            uid = "u1",
            displayName = "Bruno Lima",
            roles = UserRoles(trainer = true),
            activeRole = ActiveRole.TRAINER,
            state = "SP",
            city = "Campinas",
        )

        val prefilled = ProfileFormState().prefilledFrom(profile, registration = null, state = null)

        // Cidade sem estado não identifica lugar nenhum, e há mais de uma Bom Jesus no Brasil: o
        // campo ficaria preenchido com um nome que a lista seguinte não teria como confirmar.
        assertEquals("", prefilled.city)
    }

    @Test
    fun `perfil ausente devolve campos vazios, nunca nulos`() {
        val prefilled = ProfileFormState().prefilledFrom(profile = null, registration = null, state = null)

        assertEquals("", prefilled.birthDate)
        assertEquals("", prefilled.phone)
        assertEquals("", prefilled.cref)
        assertEquals("", prefilled.city)
        assertNull(prefilled.selectedState)
    }
}
