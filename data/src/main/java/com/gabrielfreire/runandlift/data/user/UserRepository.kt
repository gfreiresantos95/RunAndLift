package com.gabrielfreire.runandlift.data.user

import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.SignUpDetails
import com.gabrielfreire.runandlift.data.model.UserProfile

/**
 * Documento `users/{uid}` — identidade, papéis e consentimento (backlog E1-02, E1-08, E1-09).
 *
 * Custo declarado: [profile] gasta **0 leitura** quando o documento já está no cache do Firestore,
 * e 1 quando não está. É a regra 3 do orçamento (§2.4) aplicada onde ela cabe: papel do usuário
 * muda raramente, e ler do servidor a cada abertura seria desperdício.
 */
interface UserRepository {

    /** Perfil do usuário, ou `null` se ainda não houver documento (conta recém-criada). */
    suspend fun profile(uid: String): UserProfile?

    /**
     * Grava identidade e papel de uma vez — é a única escrita do fluxo de entrada.
     *
     * O papel é **somado** ao que já existir, nunca substituído: é o que permite o mesmo usuário
     * ser treinador e aluno de outra pessoa sem segunda conta (§3.2). Campo de [details] que vier
     * nulo não é escrito, então gravar só o papel não apaga o nome que já estava lá.
     *
     * Quando [SignUpDetails.cref] vem preenchido, a mesma chamada abre `trainerProfiles/{uid}`.
     * São **dois documentos porque são dois públicos**: `users/{uid}` só o titular lê, e o registro
     * profissional precisa ser legível pelo aluno vinculado. As duas escritas vão num
     * [com.google.firebase.firestore.WriteBatch] — meia gravação deixaria um treinador com papel e
     * sem registro.
     *
     * Custo declarado: 1 escrita (2 com registro profissional, numa ida só), mais a leitura de
     * [profile] para descobrir os papéis atuais — **0 do orçamento** quando o documento está no
     * cache, que é o caso logo após o cadastro.
     *
     * @param role papel a somar. `null` grava apenas a identidade, para o cadastro que ainda não
     *   sabe o papel — a escolha vem na tela seguinte.
     */
    suspend fun saveProfile(uid: String, role: ActiveRole?, details: SignUpDetails = SignUpDetails()): UserProfile

    /** Troca o papel ativo, sem alterar os papéis que a conta possui. */
    suspend fun setActiveRole(uid: String, role: ActiveRole)

    /**
     * Nome e celular **substituídos** pelo que o titular acabou de digitar.
     *
     * Existe separado de [saveProfile] porque as duas escritas querem coisas opostas do mesmo
     * campo. [saveProfile] preenche o que falta e **preserva** o nome existente — é o que impede o
     * cadastro e a conclusão pós-Google de atropelar um nome real. Uma tela de edição precisa
     * exatamente do contrário: quem digitou um nome novo está corrigindo o antigo, e preservá-lo
     * faria o botão de salvar não fazer nada.
     *
     * Não toca em papel, nascimento nem consentimento: o que esta chamada escreve é o que ela diz
     * no nome.
     *
     * Custo declarado: 1 escrita, 0 leitura.
     *
     * @param phone `null` apaga o número, que é o que "esvaziei o campo de propósito" significa
     *   numa tela de edição — diferente de [saveProfile], onde nulo quer dizer "não informado".
     */
    suspend fun updateIdentity(uid: String, displayName: String, phone: String?)

    /**
     * Registro no CREF gravado em `trainerProfiles/{uid}`, ou `null` quando não há nenhum.
     *
     * Existe separado de [profile] porque mora em outro documento, e porque só interessa a quem é
     * treinador — cobrar essa leitura de todo aluno seria pagar por um dado que ele não tem.
     *
     * Custo declarado: **0 leitura** com o documento em cache, 1 quando não está. Chamado no
     * caminho frio de conferir se o cadastro do treinador está completo, nunca em tela de treino.
     */
    suspend fun trainerRegistration(uid: String): String?
}
