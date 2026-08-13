package com.gabrielfreire.runandlift.feature.auth

import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.user.UserRepository

/**
 * O que ainda falta numa conta para ela poder ser usada no papel escolhido.
 *
 * Existe porque **entrar pelo Google não é a mesma coisa que se cadastrar**. A folha do Google
 * devolve nome e e-mail, e mais nada: quem entra por ali nunca respondeu data de nascimento, nunca
 * informou o registro profissional e — o que é mais sério — nunca aceitou os termos. A conta nasce
 * autenticada e incompleta, e a diferença entre as duas coisas precisa ser perguntável.
 *
 * É público porque quem pergunta são dois: o fluxo de entrada, logo depois de autenticar, e a
 * abertura do app, para que fechar o aplicativo no meio não vire uma forma de pular a pergunta.
 */
object ProfileCompletion {

    /**
     * O que falta em `users/{uid}` — e, para treinador, em `trainerProfiles/{uid}`.
     *
     * Custo declarado: **0 leitura** com os documentos em cache; até 1 para o aluno e 2 para o
     * treinador quando não estão. É caminho frio — acontece uma vez por entrada, nunca em tela de
     * treino.
     *
     * **Leitura que falha devolve "não falta nada".** Sem rede e sem cache não dá para afirmar que
     * a conta está incompleta, e prender quem só quer treinar por causa de um palpite é pior do
     * que deixar passar um cadastro pela metade, que a próxima abertura online cobra.
     */
    suspend fun missing(userRepository: UserRepository, uid: String, role: ActiveRole): MissingProfileData {
        val isTrainer = role == ActiveRole.TRAINER

        // As leituras num bloco só: se qualquer uma falhar, a resposta é a mesma — "não sei", e
        // "não sei" aqui significa deixar passar. A do registro só acontece para treinador; cobrá-la
        // de todo aluno seria pagar por um documento que a conta dele nem tem.
        val stored = runCatching {
            val profile = userRepository.profile(uid)
            profile to if (isTrainer) userRepository.trainerRegistration(uid) else null
        }.getOrNull() ?: return MissingProfileData()

        val (profile, registration) = stored

        return MissingProfileData(
            birthDate = profile?.birthDate == null,
            // Celular e registro seguem a mesma régua do formulário de cadastro: exigidos do
            // treinador, dispensados do aluno. Duas réguas para o mesmo dado seria uma tela
            // cobrando o que a outra não pediu.
            phone = isTrainer && profile?.phone.isNullOrBlank(),
            cref = isTrainer && registration.isNullOrBlank(),
            // Só a ausência conta. Termos novos são um pedido de re-consentimento, que é outro
            // assunto e outra conversa — tratá-los aqui transformaria uma atualização de texto
            // jurídico num bloqueio de acesso para a base inteira.
            consent = profile?.acceptedTermsVersion == null,
        )
    }
}
