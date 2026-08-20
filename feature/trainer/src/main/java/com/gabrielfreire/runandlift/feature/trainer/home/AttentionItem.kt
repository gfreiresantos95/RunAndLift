package com.gabrielfreire.runandlift.feature.trainer.home

/**
 * Um aluno de quem o treinador precisa cuidar, com o motivo.
 *
 * **O motivo é obrigatório e não é um número.** "Aderência 40%" não diz o que fazer; "3 treinos
 * seguidos sem registro" diz. Um painel que classifica sem explicar transfere para o treinador o
 * trabalho de descobrir o porquê, que é justamente o trabalho que ele veio delegar.
 *
 * Hoje estes itens são exemplo — não há treino registrado no produto para derivar nível nenhum. É
 * o mesmo motivo pelo qual o bloco inteiro anuncia que é exemplo, e a estrutura já é a que a versão
 * real vai preencher.
 *
 * @param name como o aluno aparece na carteira, que é o nome copiado dentro do vínculo.
 * @param reason a frase que justifica o nível, no tempo verbal do que aconteceu.
 * @param level onde ele está no semáforo.
 */
internal data class AttentionItem(val name: String, val reason: String, val level: AttentionLevel)
