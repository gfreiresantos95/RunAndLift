package com.gabrielfreire.runandlift.data.model

/**
 * Consentimento do treinador para aparecer na vitrine (E3-02).
 *
 * É o espelho de [HealthDataConsent] do outro lado do vínculo, e a simetria é de forma, não de
 * conteúdo — os dois gates protegem coisas opostas. O do aluno impede que dado sensível seja
 * **guardado** sem autorização (LGPD art. 11, I). Este impede que dado profissional seja
 * **publicado** sem autorização: apresentação, especialidades e capacidade viram legíveis por
 * qualquer pessoa autenticada que esteja procurando treinador, e compartilhar dado pessoal com um
 * público novo é outra finalidade, que pede consentimento próprio (art. 8º, §4º).
 *
 * A regra de leitura já existe em `firestore.rules`: `showcase.enabled == true` é o que abre
 * `trainerProfiles/{uid}` para quem não é o titular nem aluno vinculado. O campo ausente vale como
 * falso — que é o padrão certo de um opt-in.
 *
 * **Carrega [accepted], e não só a versão, porque este consentimento é retirável com efeito
 * imediato.** O do aluno, retirado, é um pedido de exclusão que merece fluxo próprio; este,
 * retirado, precisa simplesmente tirar o perfil do ar — e é `enabled = false` que faz isso. A
 * versão e o momento do aceite **permanecem gravados**: são o registro de que ele existiu, e apagá-
 * los destruiria a prova de que a publicação foi autorizada enquanto durou.
 *
 * @param accepted `true` aceita e publica, `false` retira o perfil da vitrine.
 * @param version versão do aviso que está sendo aceito. Só importa quando [accepted] é verdadeiro.
 */
data class ShowcaseConsent(val accepted: Boolean, val version: String = CURRENT_VERSION) {

    companion object {
        /**
         * Versão vigente do aviso da vitrine, em data ISO.
         *
         * Mudou o que fica visível, para quem, ou por quanto tempo, muda esta constante — é o que
         * permite descobrir depois quem consentiu com a versão antiga.
         */
        const val CURRENT_VERSION = "2026-08-16"
    }
}
