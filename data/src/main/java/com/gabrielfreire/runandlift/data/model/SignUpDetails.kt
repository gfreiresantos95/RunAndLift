package com.gabrielfreire.runandlift.data.model

import java.time.LocalDate

/**
 * O que o cadastro sabe sobre a pessoa no instante em que a conta nasce.
 *
 * Todos os campos são opcionais, e isso é intencional: o mesmo tipo serve ao cadastro por
 * formulário, que preenche tudo, e à tela de escolha de papel, que só tem o papel a gravar. Campo
 * nulo significa "não informado" e **não é escrito** no Firestore — o que evita que uma gravação
 * parcial apague o que já estava lá.
 *
 * **Não carrega dado de saúde.** Peso, medidas, restrições e histórico de lesões são dado pessoal
 * sensível (LGPD art. 5º, II) e exigem base legal e consentimento próprios; eles pertencem à
 * anamnese, que acontece depois, com o treinador. Pedi-los na criação de conta seria coletar antes
 * de existir finalidade — e alongaria o cadastro justamente onde o abandono é maior.
 *
 * @param cref registro profissional, **só do treinador**, já normalizado como `012345-G/SP`. É o
 *   único campo aqui que não vai para `users/{uid}`: perfil profissional é público para o aluno
 *   vinculado, e `users/{uid}` é legível apenas pelo titular. Ver [UserProfile] e as regras de
 *   `trainerProfiles`.
 */
data class SignUpDetails(
    val displayName: String? = null,
    val birthDate: LocalDate? = null,
    val phone: String? = null,
    val consent: PrivacyConsent? = null,
    val cref: String? = null,
)
