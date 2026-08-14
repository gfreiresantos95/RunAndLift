package com.gabrielfreire.runandlift.data.location

import com.gabrielfreire.runandlift.data.model.BrazilState

/**
 * Estados e municípios do Brasil, para os campos de localidade do cadastro e do perfil.
 *
 * **Custo declarado: nenhuma leitura do Firestore, e uma chamada HTTP ao IBGE por lista.** Este é o
 * único repositório do projeto que sai para fora do Firebase, e é por isso que ele declara o custo
 * em outra moeda: o orçamento de leitura (§2.4) não é afetado, mas a resposta de municípios de um
 * estado grande passa de 380 KB. O cache do implementador é o que faz esse preço ser pago no máximo
 * uma vez por estado, por sessão.
 *
 * **Falha vira exceção, e não uma lista vazia.** Vazio e "não deu para carregar" precisam ser telas
 * diferentes: uma diz que não há nada, a outra oferece tentar de novo. Devolver `emptyList()` numa
 * queda de rede transformaria a segunda na primeira, e a pessoa concluiria que o app não conhece o
 * estado dela. Quem traduz a exceção em estado de tela é o ViewModel, com `runCatching`, como já
 * fazem os que conversam com [com.gabrielfreire.runandlift.data.user.UserRepository].
 */
interface LocationRepository {

    /** As 27 unidades da federação, em ordem alfabética de nome. */
    suspend fun states(): List<BrazilState>

    /**
     * Municípios da UF, só os nomes, em ordem alfabética.
     *
     * Nome e não código do IBGE porque é o nome que é gravado: município não tem uma sigla estável
     * como o estado, e guardar o código obrigaria uma consulta à rede só para exibir "Campinas".
     */
    suspend fun cities(uf: String): List<String>

    /**
     * O estado de uma sigla gravada, para remontar `São Paulo - SP` a partir de `SP`.
     *
     * Devolve `null` quando a sigla não existe **ou** quando a lista não pôde ser carregada — os
     * dois casos levam à mesma decisão na tela, que é mostrar só a sigla. É a única função daqui
     * que não propaga a falha: um perfil não pode deixar de abrir porque o IBGE está fora do ar.
     */
    suspend fun state(uf: String): BrazilState?
}
