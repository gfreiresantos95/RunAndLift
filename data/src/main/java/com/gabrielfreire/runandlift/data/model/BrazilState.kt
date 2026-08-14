package com.gabrielfreire.runandlift.data.model

/**
 * Unidade da federação: o que a lista mostra e o que o banco guarda, que **não** são a mesma coisa.
 *
 * Gravado é só [uf] — duas letras, estáveis desde sempre, iguais às que o registro no CREF já usa.
 * Guardar também o nome por extenso seria uma segunda grafia do mesmo estado esperando para
 * divergir: "São Paulo" e "Sao Paulo" viram dois estados na hora de agrupar alunos por região.
 *
 * [label] é a volta desse caminho — a forma como a pessoa escolheu, remontada a partir da sigla. Ela
 * mora aqui, e não numa tela, porque a lista de seleção e o perfil precisam da **mesma** frase: se
 * uma escrever "São Paulo - SP" e a outra "SP - São Paulo", quem escolheu vai achar que mudou.
 *
 * O separador é hífen com espaços, e não vírgula ou barra: não é texto traduzível, é formato de
 * exibição de um par que já vem em português da fonte.
 */
data class BrazilState(val uf: String, val name: String) {

    /** `São Paulo - SP` — nome por extenso primeiro, porque é por ele que se procura. */
    val label: String get() = "$name - $uf"
}
