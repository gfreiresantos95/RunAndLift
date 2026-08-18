package com.gabrielfreire.runandlift.data.user

import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.PrivacyConsent
import com.gabrielfreire.runandlift.data.model.SignUpDetails
import com.gabrielfreire.runandlift.data.model.UserProfile
import com.gabrielfreire.runandlift.data.model.UserRoles
import com.google.firebase.firestore.FieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * As duas regras de `users/{uid}` que quebram sem dar erro.
 *
 * A primeira é o **acúmulo de papéis**: cadastrar-se como aluno não pode tirar de alguém a carteira
 * de treinador que ele já tinha. A segunda é a **trava do nome** — a tela de escolha de papel deriva
 * um nome do e-mail, e sem a trava ela sobrescreveria o nome real de quem preencheu o formulário.
 * Nenhuma das duas aparece na tela de quem acabou de se cadastrar; as duas aparecem semanas depois,
 * em alguém que usava o app dos dois lados.
 *
 * O terceiro grupo guarda a diferença entre os dois mapas de gravação, que é sutil e é o tipo de
 * coisa que se unifica "para simplificar": no cadastro nulo **omite**, na edição nulo **apaga**.
 */
class UserDocumentTest {

    // -- Papéis se somam ------------------------------------------------------------------------

    @Test
    fun `o papel novo entra sem derrubar o que ja havia`() {
        val roles = UserDocument.roles(UserRoles(trainer = true), ActiveRole.STUDENT)

        // Treinador que também é aluno de outro treinador é caso real, resolvido sem segunda conta.
        assertTrue(roles.trainer)
        assertTrue(roles.student)
    }

    @Test
    fun `conta nova ganha so o papel escolhido`() {
        val roles = UserDocument.roles(existing = null, role = ActiveRole.TRAINER)

        assertTrue(roles.trainer)
        assertFalse(roles.student)
    }

    @Test
    fun `entrar sem papel nenhum nao apaga os que existem`() {
        val roles = UserDocument.roles(UserRoles(trainer = true, student = true), role = null)

        // É o caminho do Google, onde o papel ainda não foi perguntado.
        assertTrue(roles.hasBoth)
    }

    // -- O nome só é escrito quando não há nenhum ----------------------------------------------

    @Test
    fun `nome ja gravado nunca e sobrescrito pelo cadastro`() {
        assertNull(UserDocument.nameToWrite(existing = "Ana Souza", provided = "ana"))
    }

    @Test
    fun `nome em branco conta como ausente, e o novo entra`() {
        assertEquals("Ana Souza", UserDocument.nameToWrite(existing = "   ", provided = "Ana Souza"))
        assertEquals("Ana Souza", UserDocument.nameToWrite(existing = null, provided = "Ana Souza"))
    }

    @Test
    fun `sem nome novo nao ha o que gravar`() {
        assertNull(UserDocument.nameToWrite(existing = null, provided = null))
    }

    // -- O mapa do cadastro: nulo omite ---------------------------------------------------------

    @Test
    fun `campo nao informado fica fora do mapa, porque merge apagaria o que ja esta la`() {
        val fields = UserDocument.fields(UserRoles(student = true), ActiveRole.STUDENT, SignUpDetails())

        assertFalse(fields.containsKey(UserDocument.FIELD_PHONE))
        assertFalse(fields.containsKey(UserDocument.FIELD_BIRTH_DATE))
        assertFalse(fields.containsKey(UserDocument.FIELD_DISPLAY_NAME))
    }

    @Test
    fun `os papeis vao como mapa aninhado, e nao como chave com ponto`() {
        val fields = UserDocument.fields(UserRoles(trainer = true), ActiveRole.TRAINER, SignUpDetails())

        // Em `set()` o ponto é parte do nome do campo, não caminho: com a chave achatada o
        // Firestore criaria um campo literalmente chamado "roles.trainer".
        assertFalse(fields.containsKey(UserDocument.FIELD_ROLE_TRAINER))
        assertEquals(
            mapOf(UserDocument.FIELD_TRAINER to true, UserDocument.FIELD_STUDENT to false),
            fields[UserDocument.FIELD_ROLES],
        )
    }

    @Test
    fun `a data de nascimento vai como texto ISO`() {
        val details = SignUpDetails(birthDate = LocalDate.of(1995, 3, 7))
        val fields = UserDocument.fields(UserRoles(), null, details)

        // Um Timestamp a deslocaria um dia inteiro conforme o fuso de quem lê.
        assertEquals("1995-03-07", fields[UserDocument.FIELD_BIRTH_DATE])
    }

    @Test
    fun `so a sigla do estado vai ao banco`() {
        val fields = UserDocument.fields(UserRoles(), null, SignUpDetails(state = "SP", city = "Campinas"))

        // "São Paulo" gravado junto seria uma segunda grafia esperando para divergir da primeira.
        assertEquals("SP", fields[UserDocument.FIELD_STATE])
        assertEquals("Campinas", fields[UserDocument.FIELD_CITY])
    }

    @Test
    fun `o aceite guarda versao, opcao de marketing e a hora do servidor`() {
        val consent = PrivacyConsent(PrivacyConsent.CURRENT_TERMS_VERSION, marketingOptIn = false)
        val fields = UserDocument.consentFields(consent)

        assertEquals(PrivacyConsent.CURRENT_TERMS_VERSION, fields[UserDocument.FIELD_TERMS_VERSION])
        assertEquals(false, fields[UserDocument.FIELD_MARKETING_OPT_IN])
        assertTrue(fields[UserDocument.FIELD_TERMS_ACCEPTED_AT] is FieldValue)
    }

    @Test
    fun `o CREF nao entra no documento da conta`() {
        val fields = UserDocument.fields(UserRoles(trainer = true), ActiveRole.TRAINER, SignUpDetails(cref = "012345"))

        // Ele vai para `trainerProfiles/{uid}`, que é o que o aluno vinculado consegue ler.
        assertFalse(fields.containsKey(UserDocument.FIELD_CREF))
    }

    // -- O mapa da edição: nulo apaga -----------------------------------------------------------

    @Test
    fun `na edicao, campo esvaziado e apagado e nao preservado`() {
        val fields = UserDocument.identityFields(displayName = "Ana Souza", phone = null, state = null, city = null)

        // Omitir preservaria o antigo, que é o comportamento do cadastro e exatamente o que esta
        // gravação existe para não fazer.
        assertTrue(fields[UserDocument.FIELD_PHONE] is FieldValue)
        assertTrue(fields[UserDocument.FIELD_STATE] is FieldValue)
        assertEquals("Ana Souza", fields[UserDocument.FIELD_DISPLAY_NAME])
    }

    @Test
    fun `na edicao, valor preenchido substitui`() {
        val fields = UserDocument.identityFields("Ana Souza", phone = "11999998888", state = "SP", city = "Campinas")

        assertEquals("11999998888", fields[UserDocument.FIELD_PHONE])
        assertEquals("Campinas", fields[UserDocument.FIELD_CITY])
    }

    // -- O perfil devolvido sem reler -----------------------------------------------------------

    @Test
    fun `o resultado devolve o nome antigo quando o cadastro nao pode gravar o novo`() {
        val existing = UserProfile(uid = "u1", displayName = "Ana Souza", roles = UserRoles(), activeRole = null)
        val merged = UserDocument.merged("u1", existing, ActiveRole.STUDENT, SignUpDetails(displayName = "ana"))

        // Espelha a gravação: o campo não foi escrito, então o resultado não pode dizer que foi.
        assertEquals("Ana Souza", merged.displayName)
    }

    @Test
    fun `o resultado acumula o papel, como o mapa gravado`() {
        val existing = UserProfile(uid = "u1", displayName = null, roles = UserRoles(trainer = true), activeRole = null)
        val merged = UserDocument.merged("u1", existing, ActiveRole.STUDENT, SignUpDetails())

        assertTrue(merged.roles.hasBoth)
        assertEquals(ActiveRole.STUDENT, merged.activeRole)
    }

    @Test
    fun `conta nova sai do cadastro com o que foi preenchido`() {
        val details = SignUpDetails(
            displayName = "Ana Souza",
            birthDate = LocalDate.of(1995, 3, 7),
            consent = PrivacyConsent("2026-08-08", marketingOptIn = true),
            state = "SP",
        )
        val merged = UserDocument.merged("u1", existing = null, role = ActiveRole.STUDENT, details = details)

        assertEquals("Ana Souza", merged.displayName)
        assertEquals("2026-08-08", merged.acceptedTermsVersion)
        assertEquals("SP", merged.state)
    }

    @Test
    fun `o que o cadastro nao mandou continua sendo o que ja estava la`() {
        val existing = UserProfile(
            uid = "u1",
            displayName = "Ana Souza",
            roles = UserRoles(student = true),
            activeRole = ActiveRole.STUDENT,
            phone = "11999998888",
            city = "Campinas",
        )
        val merged = UserDocument.merged("u1", existing, role = null, details = SignUpDetails(state = "SP"))

        assertEquals("11999998888", merged.phone)
        assertEquals("Campinas", merged.city)
        assertEquals(ActiveRole.STUDENT, merged.activeRole)
    }
}
