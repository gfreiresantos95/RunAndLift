package com.gabrielfreire.runandlift.data.link

import com.gabrielfreire.runandlift.data.model.InviteCode
import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.LinkStatus

/**
 * Coleções `links` e `inviteCodes` — quem treina quem, e por onde essa relação começa.
 *
 * É o repositório de que todo o resto do produto vai depender: sem vínculo ativo não há prescrição,
 * não há anamnese lida e não há aderência. Por isso ele é pequeno de propósito — cinco perguntas e
 * uma transição —, e tudo o que for painel, resumo ou histórico virá de documento agregado, e não de
 * varrer esta coleção.
 *
 * **As listas custam uma leitura por vínculo, e é o preço declarado destas telas.** Não há como
 * fugir disso hoje: `trainerDashboards/{trainerId}` — o documento-resumo previsto na regra 1 do
 * orçamento (§2.4) — ainda não existe, e é ele que vai substituir a varredura quando a carteira
 * crescer. Até lá as listas param em [LIMIT] itens, que é o que impede a conta de uma tela virar
 * ilimitada sem ninguém perceber.
 *
 * As leituras de vínculo vão ao **servidor**, e essa é a diferença deste repositório para os outros:
 * o que eles leem muda quando o próprio titular mexe, e o que se lê aqui muda quando **a outra
 * pessoa** mexe. Um pedido aceito do outro lado não invalida cache nenhum, e cache-first mostraria a
 * carteira de ontem justamente a quem abriu a tela para ver se alguém entrou. Sem rede, o cache
 * responde — a tela nunca fica vazia por falta de sinal.
 */
interface LinkRepository {

    /**
     * Vínculos em que [trainerId] é o treinador, incluindo os encerrados.
     *
     * Encerrados vêm juntos porque a carteira precisa mostrar quem saiu — sumir sem explicação é
     * como um aluno desaparecer de uma lista sem ninguém saber se foi erro. Filtrar e ordenar é
     * decisão de tela, e por isso acontece lá, onde dá para testar sem Firestore.
     *
     * Custo declarado: 1 leitura por vínculo, no máximo [LIMIT].
     */
    suspend fun trainerLinks(trainerId: String): List<Link>

    /**
     * Vínculos em que [studentId] é o aluno. Mesmo custo e mesma regra de [trainerLinks].
     *
     * É quase sempre um item só — um aluno tem um treinador —, mas é uma lista porque nada impede
     * trocar de treinador e manter o histórico do anterior.
     */
    suspend fun studentLinks(studentId: String): List<Link>

    /**
     * O código de convite deste treinador, ou `null` se ele ainda não gerou nenhum.
     *
     * Vem de `trainerProfiles/{uid}`, e não de uma consulta em `inviteCodes`: procurar o código pelo
     * dono exigiria varrer uma coleção que qualquer autenticado lê, o que transformaria "ler o
     * código que me deram" em "listar todos os códigos que existem".
     *
     * Custo declarado: **0 leitura** com o perfil em cache, 1 quando não está.
     */
    suspend fun inviteCode(trainerId: String): String?

    /**
     * Gera um código novo para [trainerId] e **descarta o anterior**.
     *
     * Um treinador tem um código por vez. Gerar de novo é o que se faz quando o antigo circulou
     * demais, e deixar os dois valendo seria não desfazer nada.
     *
     * As escritas — o convite novo, o campo no perfil e a remoção do antigo — vão no mesmo lote:
     * metade delas deixaria um código sem dono ou um dono apontando para um convite que não existe.
     *
     * Custo declarado: 1 leitura para conferir que o sorteado está livre (raramente mais de uma) e 1
     * escrita em lote com 2 a 3 documentos.
     *
     * @param trainerName copiado para dentro do convite, para o aluno conferir com quem vai se
     *   vincular antes de pedir. Ver [InviteCode].
     */
    suspend fun createInviteCode(trainerId: String, trainerName: String): String

    /**
     * De quem é este código, ou `null` se não existir convite com ele.
     *
     * **Existe separado de [requestLink] para o aluno ver o nome antes de pedir.** Quem digita um
     * código está prestes a autorizar outra pessoa a ler a própria anamnese; confirmar um nome antes
     * disso é o mínimo, e custa a mesma leitura que um resgate cego custaria.
     *
     * `null` é "não existe esse código". **Falha de leitura é exceção**, e a diferença importa: a
     * tela precisa dizer "código não encontrado" num caso e "não consegui verificar" no outro.
     *
     * Custo declarado: 1 leitura.
     */
    suspend fun findInvite(code: String): InviteCode?

    /**
     * Cria o pedido de vínculo em [LinkStatus.REQUESTED].
     *
     * **Nasce pendente mesmo tendo vindo do código do próprio treinador**, e é aí que mora a
     * segurança do fluxo: o código pode ter sido repassado a quem ele não convidou, e a confirmação
     * é o que separa "alguém digitou meu código" de "tenho um aluno novo". É também o que as regras
     * exigem — aluno só cria vínculo em `requested`.
     *
     * Um vínculo **encerrado** com o mesmo treinador é reaberto por aqui, e não duplicado: o id é
     * `{trainerId}_{studentId}` e não existe segundo documento possível para o mesmo par.
     *
     * Custo declarado: **0 leitura** e 1 escrita.
     *
     * @param existing o vínculo que a tela já conhece com esse treinador, ou `null` se não houver.
     *   Vem de fora porque quem chama acabou de listar os próprios vínculos, e porque a leitura que
     *   se evitaria aqui **nem seria permitida**: a regra de `links` compara `resource.data`, e
     *   documento inexistente faz a regra falhar em vez de responder "não existe" — perguntar "já
     *   sou aluno dele?" pelo caminho direto volta como permissão negada no caso mais comum de
     *   todos, o de não ser.
     */
    suspend fun requestLink(
        invite: InviteCode,
        studentId: String,
        studentName: String,
        existing: Link?,
    ): LinkRequestResult

    /**
     * Move o vínculo para [status] — aceitar, pausar, retomar, encerrar.
     *
     * Quem pode fazer o quê está nas Security Rules, e não aqui: transição recusada volta como
     * exceção da chamada. Repetir a máquina de estados no cliente daria duas versões dela, e a que
     * vale é sempre a do servidor.
     *
     * Custo declarado: 1 escrita, 0 leitura.
     */
    suspend fun updateStatus(link: Link, status: LinkStatus): Link

    companion object {

        /**
         * Teto de itens por lista.
         *
         * É a regra 4 do orçamento (§2.4) aplicada a uma coleção que cresce: sem teto, o custo de
         * abrir a carteira cresce com o sucesso do treinador. Cem vínculos é mais que o dobro da
         * maior capacidade que alguém declara no perfil, então o teto não corta ninguém hoje — ele
         * existe para o dia em que cortar seja o comportamento certo.
         */
        const val LIMIT = 100L
    }
}
