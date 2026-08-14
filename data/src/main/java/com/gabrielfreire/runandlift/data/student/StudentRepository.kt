package com.gabrielfreire.runandlift.data.student

import com.gabrielfreire.runandlift.data.model.StudentProfile
import com.gabrielfreire.runandlift.data.model.StudentProfileDetails

/**
 * Documento `students/{uid}` — o que o treinador precisa saber para prescrever (backlog E2-01).
 *
 * Separado de `UserRepository` porque são dois documentos com **públicos e regras diferentes**:
 * `users/{uid}` só o titular lê, e este aqui o treinador vinculado também lê. Juntar os dois numa
 * interface faria uma tela pedir o documento errado sem perceber.
 *
 * Custo declarado: [profile] gasta **0 leitura** com o documento em cache e 1 quando não está. É
 * caminho frio — abertura do app e edição de perfil —, nunca tela de treino.
 */
interface StudentRepository {

    /** Perfil do aluno, ou `null` quando ainda não há documento (ninguém respondeu nada). */
    suspend fun profile(uid: String): StudentProfile?

    /**
     * Grava o que veio preenchido, preservando o resto.
     *
     * Campo nulo em [details] **não é escrito**, então o onboarding pode gravar passo a passo e a
     * edição pode mexer num campo só — nenhum dos dois precisa reler o documento para reenviar o
     * que já estava lá.
     *
     * **Sem consentimento de saúde, peso, altura e restrições são descartados aqui**, mesmo que
     * venham preenchidos. A regra mora no repositório, e não na tela, porque tela nova é a forma
     * mais provável de ela ser esquecida — e o que se perde ao esquecê-la é a base legal de um dado
     * sensível (LGPD art. 11, I).
     *
     * Custo declarado: 1 escrita, mais a leitura de [profile] para saber se o consentimento já
     * existe — 0 do orçamento com o documento em cache, que é o caso durante o onboarding.
     */
    suspend fun save(uid: String, details: StudentProfileDetails): StudentProfile
}
