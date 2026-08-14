package com.gabrielfreire.runandlift.data.model

/**
 * Consentimento específico para dado de saúde.
 *
 * Separado de [PrivacyConsent] porque **é outra base legal e outra finalidade**. O aceite dos
 * termos permite a conta existir; peso, altura e histórico de lesão são dado pessoal *sensível*
 * (LGPD art. 5º, II), cujo tratamento exige consentimento destacado para finalidade específica
 * (art. 11, I). Pedir os dois na mesma caixa transformaria dois consentimentos em um, que é
 * exatamente o que o art. 8º, §4º proíbe.
 *
 * Guarda **versão**, e não um booleano, pela mesma razão de [PrivacyConsent]: o que importa é o
 * quê foi consentido. Mudou o texto que descreve o uso do dado de saúde, o aceite anterior deixa de
 * cobrir o novo — sem apagar o registro de que ele existiu.
 *
 * O momento é gravado pelo servidor, junto do documento, e não pelo relógio do aparelho.
 */
data class HealthDataConsent(val version: String) {

    companion object {
        /**
         * Versão vigente do aviso de dado de saúde, em data ISO.
         *
         * Mudou o que se faz com peso, altura ou histórico de lesão, muda esta constante — é o que
         * permite descobrir depois quem consentiu com a versão antiga.
         */
        const val CURRENT_VERSION = "2026-08-13"
    }
}
