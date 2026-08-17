package com.gabrielfreire.runandlift.data.link

/**
 * Por que um pedido de vínculo não foi criado.
 *
 * São três porque são três conversas diferentes com quem pediu, e não porque o pedido falha de três
 * jeitos. Uma mensagem só — "não foi possível" — deixaria a pessoa tentando de novo justamente nos
 * dois casos em que tentar de novo não muda nada.
 *
 * "Código não existe" **não** está aqui: essa resposta vem antes, de `LinkRepository.findInvite`, e
 * quem não achou treinador nenhum ainda não pediu nada.
 */
enum class LinkRequestFailure {

    /**
     * O código é do próprio usuário, que é treinador e aluno na mesma conta.
     *
     * Vale distinguir: `{uid}_{uid}` é um documento que as regras aceitariam, e o resultado seria a
     * pessoa na própria carteira. Barrar antes é mais honesto do que deixar acontecer e explicar
     * depois.
     */
    OWN_CODE,

    /**
     * Já existe vínculo com esse treinador — pendente, ativo ou pausado.
     *
     * Não é erro de quem digitou: é a resposta certa para quem pediu duas vezes, e a tela mostra em
     * que pé está o pedido em vez de criar outro.
     */
    ALREADY_LINKED,

    /** Rede, permissão ou qualquer outra coisa que o app não sabe nomear. Tentar de novo é útil. */
    UNKNOWN,
}
