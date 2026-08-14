package com.gabrielfreire.runandlift.data.model

import java.time.LocalDate

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
 * @param state sigla da unidade da federação (`SP`), e só ela. O nome por extenso vem de
 *   [com.gabrielfreire.runandlift.data.location.LocationRepository] na hora de exibir — ver
 *   [BrazilState].
 * @param city nome do município. Localidade fica aqui, e não em `students/{uid}`, porque não é dado
 *   de saúde e vale para os dois papéis: é por ela que o aluno acha treinador perto, e é ela que
 *   aparece no perfil de quem prescreve.
 */
data class UserProfile(
    val uid: String,
    val displayName: String?,
    val roles: UserRoles,
    val activeRole: ActiveRole?,
    val birthDate: LocalDate? = null,
    val phone: String? = null,
    val acceptedTermsVersion: String? = null,
    val state: String? = null,
    val city: String? = null,
)
