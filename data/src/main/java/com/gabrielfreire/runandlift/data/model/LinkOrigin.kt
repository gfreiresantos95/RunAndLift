package com.gabrielfreire.runandlift.data.model

/**
 * Por onde o vínculo começou.
 *
 * Fica gravado no documento porque é a única coisa da história do vínculo que não dá para deduzir
 * depois: [LinkStatus] diz onde ele está, e não como chegou ali. Serve para duas perguntas que vão
 * ser feitas — quanto da carteira veio de indicação e quanto veio da vitrine, e por qual caminho
 * entrou alguém cujo vínculo deu errado.
 *
 * @param stored o que vai ao banco, em minúsculo com separador, como em [LinkStatus]. Aqui nenhuma
 *   Security Rule lê o campo, mas os dois valores são escritos do mesmo jeito de propósito: um
 *   documento em que metade dos enums é `SHOWCASE` e a outra metade é `showcase` cobra atenção toda
 *   vez que alguém abre o console.
 */
enum class LinkOrigin(val stored: String) {

    /** O treinador gerou um código e passou adiante; o aluno digitou. */
    INVITE_CODE("invite_code"),

    /** O aluno achou o treinador procurando e pediu vínculo. */
    SHOWCASE("showcase"),
    ;

    companion object {

        /** O valor gravado de volta ao enum, ou `null` — mesma tolerância de [LinkStatus.fromStored]. */
        fun fromStored(value: String?): LinkOrigin? = entries.firstOrNull { it.stored == value }
    }
}
