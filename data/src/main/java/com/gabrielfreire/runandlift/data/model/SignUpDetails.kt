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

/**
 * Registro do consentimento dado no cadastro.
 *
 * Guarda **versão e momento** porque a LGPD põe o ônus da prova no controlador (art. 8º, §2º):
 * "a pessoa aceitou" não é afirmável sem dizer *o quê* ela aceitou e *quando*. O momento é gravado
 * pelo servidor, não pelo relógio do aparelho, que o usuário pode mudar.
 *
 * As duas finalidades são separadas de propósito: o aceite dos termos é condição para a conta
 * existir; receber e-mail de marketing não é, e consentimento em bloco não é consentimento
 * (art. 8º, §4º — a finalidade precisa ser destacada).
 */
data class PrivacyConsent(val termsVersion: String, val marketingOptIn: Boolean) {

    companion object {
        /**
         * Versão vigente dos termos, em data ISO.
         *
         * Mudou o texto, muda esta constante: é o que permite descobrir depois quem aceitou a
         * versão antiga e precisa ser consultado de novo.
         */
        const val CURRENT_TERMS_VERSION = "2026-08-08"
    }
}
