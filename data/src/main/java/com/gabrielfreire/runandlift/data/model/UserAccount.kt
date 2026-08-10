package com.gabrielfreire.runandlift.data.model

import java.time.LocalDate

/** Conta autenticada. Identidade crua, sem papel nem perfil — isso é [UserProfile]. */
data class UserAccount(val uid: String, val email: String?, val isEmailVerified: Boolean)

/**
 * Papéis que uma conta pode exercer.
 *
 * Os dois podem ser verdadeiros ao mesmo tempo: um treinador que também é aluno de outro treinador
 * é caso real e resolvido no modelo, sem segunda conta (backlog §3.2).
 */
data class UserRoles(val trainer: Boolean = false, val student: Boolean = false) {
    val hasAny: Boolean get() = trainer || student
    val hasBoth: Boolean get() = trainer && student
}

/** Qual papel está ativo agora. Decide qual grafo de navegação é montado. */
enum class ActiveRole {
    TRAINER,
    STUDENT,
    ;

    companion object {
        fun fromStorage(value: String?): ActiveRole? = entries.firstOrNull { it.storageValue == value }
    }

    /** Valor gravado no Firestore. Fixo — mudar quebra conta existente. */
    val storageValue: String get() = if (this == TRAINER) "trainer" else "student"
}

/**
 * Documento `users/{uid}`: identidade comum aos dois papéis.
 *
 * Não é cacheado no Room de propósito. O backlog escopa o Room a "treino e execução" (§2.5), e a
 * persistência do próprio Firestore já resolve leitura offline de identidade. Room aqui seria uma
 * segunda cópia para manter em dia sem ganho correspondente.
 *
 * O que **não** mora aqui: dado de saúde. Anamnese, medidas e restrições vivem em `students/{uid}`,
 * com regra e consentimento próprios (README, "Acesso e privacidade").
 *
 * @param birthDate data de nascimento, sem hora. Serve para ajustar faixas de esforço e para a
 *   barreira de idade do cadastro.
 * @param phone celular apenas com dígitos (DDD + número), como o usuário digitou, sem máscara.
 * @param acceptedTermsVersion versão dos termos que esta conta aceitou, ou `null` se nunca aceitou.
 *   É a versão, e não um booleano, porque o que importa é **o quê** foi aceito: termos novos
 *   tornam o aceite antigo insuficiente sem apagá-lo.
 */
data class UserProfile(
    val uid: String,
    val displayName: String?,
    val roles: UserRoles,
    val activeRole: ActiveRole?,
    val birthDate: LocalDate? = null,
    val phone: String? = null,
    val acceptedTermsVersion: String? = null,
)
