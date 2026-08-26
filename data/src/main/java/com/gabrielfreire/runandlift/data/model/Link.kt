package com.gabrielfreire.runandlift.data.model

/**
 * Documento `links/{trainerId}_{studentId}` — o vínculo entre um treinador e um aluno.
 *
 * É a coleção que decide o que todo o resto do produto pode fazer: prescrever, ler anamnese, ver
 * aderência. Por isso mora numa coleção de topo, e não dentro de um dos dois lados — os dois
 * consultam a própria lista, e um subdocumento só serviria a quem o hospedasse.
 *
 * **O id é `{trainerId}_{studentId}`, e não é estética.** Security Rule não consulta, só faz `get()`
 * por caminho exato; é esse formato que torna "treinador só lê aluno com vínculo ativo" uma regra
 * escrevível. Ver [ADR-0007][docs] e `LinkDocument.id`.
 *
 * **Os nomes viajam dentro do vínculo, e isso é deliberado.** `users/{uid}` é legível só pelo
 * titular — é o que protege telefone, nascimento e endereço —, então nem o treinador lê o nome do
 * aluno em `users`, nem o aluno lê o do treinador. Sem a cópia aqui, uma carteira de alunos seria
 * uma lista de identificadores. A cópia envelhece: quem troca de nome depois de vinculado continua
 * aparecendo com o antigo para a contraparte, e a correção chega quando o vínculo for reescrito por
 * outro motivo. É o preço de não abrir `users` e de não gastar uma leitura por linha da lista.
 *
 * Quem escreve cada nome é o dono dele: o aluno grava o próprio ao pedir o vínculo, e o do treinador
 * chega junto do código de convite. Ninguém tem como inventar o nome do outro em benefício próprio —
 * o pior caso é alguém se apresentar mal, que é o mesmo que já pode fazer no cadastro.
 *
 * @param trainerName nome do treinador **como ele estava quando o vínculo nasceu**. Pode ser vazio
 *   se o cadastro dele ainda não tinha nome; a tela mostra o que houver e não inventa.
 * @param studentName idem, do lado do aluno. É o que a carteira lista.
 * @param createdAt quando estas duas pessoas se encontraram pela primeira vez, em milissegundos, ou
 *   `0` quando não se sabe. **Já era gravado desde o começo e ninguém o lia** — `LinkDocument`
 *   escrevia `createdAt` justamente porque é o tipo de dado que não se recupera depois. É ele que
 *   responde "aluno desde" na home, e por isso a data é a do **vínculo** e não a da conta: quem
 *   troca de profissional recomeça a contagem, porque o tempo de acompanhamento é dele com aquela
 *   pessoa e não com o aplicativo.
 *
 *   Zero é resposta legítima e não erro: os vínculos criados antes de alguém ler este campo têm o
 *   carimbo gravado, mas um documento vindo do cache entre a escrita local e a confirmação do
 *   servidor chega sem ele. Quem mostra a data some com a linha em vez de inventar uma.
 *
 * [docs]: https://github.com/gfreiresantos95/RunAndLift/blob/main/docs/adr/0007-security-rules-e-id-de-vinculo-deterministico.md
 */
data class Link(
    val trainerId: String,
    val studentId: String,
    val status: LinkStatus,
    val origin: LinkOrigin,
    val trainerName: String = "",
    val studentName: String = "",
    val createdAt: Long = 0L,
) {

    /** Atalho de [LinkStatus.isPending], para a tela não precisar alcançar dois níveis. */
    val isPending: Boolean
        get() = status.isPending
}
