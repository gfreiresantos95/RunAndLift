package com.gabrielfreire.runandlift.data.trainer

import com.gabrielfreire.runandlift.data.model.TrainerProfile
import com.gabrielfreire.runandlift.data.model.TrainerProfileDetails

/**
 * Documento `trainerProfiles/{uid}` — o que o aluno precisa saber sobre quem o treina (E3-02).
 *
 * Separado de `UserRepository` pela mesma razão que separa `StudentRepository`: são documentos com
 * **públicos e regras diferentes**. `users/{uid}` só o titular lê; este o aluno vinculado lê, e com
 * a vitrine ligada qualquer pessoa autenticada lê. Juntar os dois numa interface faria uma tela
 * pedir o documento errado sem perceber.
 *
 * `UserRepository.trainerRegistration` continua existindo e continua certo: ele responde uma
 * pergunta do **cadastro** — "esta conta tem registro?" — e é chamado pelo fluxo de entrada, que
 * não conhece perfil profissional nenhum. Aqui o registro chega junto do resto, para a tela que
 * mostra o perfil inteiro.
 *
 * Custo declarado: [profile] gasta **0 leitura** com o documento em cache e 1 quando não está. É
 * caminho frio — abertura do app, passo a passo e edição de perfil —, nunca tela de treino.
 */
interface TrainerRepository {

    /**
     * Perfil do treinador, ou `null` quando ainda não há documento.
     *
     * Nulo é raro aqui, e a diferença em relação ao aluno importa: quem se cadastrou como treinador
     * já tem documento, porque o registro no CREF foi gravado junto da conta. Quem não tem é conta
     * que virou treinador por outro caminho — troca de papel, conclusão de cadastro pós-Google.
     */
    suspend fun profile(uid: String): TrainerProfile?

    /**
     * Grava o que veio preenchido, preservando o resto.
     *
     * Campo nulo em [details] **não é escrito**, então o passo a passo pode gravar uma vez no fim e
     * a edição pode mexer num campo só — nenhum dos dois precisa reler o documento para reenviar o
     * que já estava lá. O registro no CREF nunca é tocado por aqui.
     *
     * **Sem a vitrine aceita, apresentação e capacidade são descartadas aqui**, mesmo que venham
     * preenchidas. A regra mora no repositório, e não na tela, porque tela nova é a forma mais
     * provável de ela ser esquecida — e o que se perde ao esquecê-la é o controle do titular sobre
     * o que dele fica público.
     *
     * Custo declarado: 1 escrita, mais a leitura de [profile] para saber se a vitrine já estava
     * aceita — 0 do orçamento com o documento em cache, que é o caso durante o passo a passo.
     */
    suspend fun save(uid: String, details: TrainerProfileDetails): TrainerProfile
}
