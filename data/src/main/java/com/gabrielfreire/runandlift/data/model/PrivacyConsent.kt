package com.gabrielfreire.runandlift.data.model

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
