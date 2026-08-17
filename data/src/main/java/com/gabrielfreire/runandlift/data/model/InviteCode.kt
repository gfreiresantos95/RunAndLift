package com.gabrielfreire.runandlift.data.model

/**
 * Documento `inviteCodes/{code}` — o convite que o treinador passa adiante.
 *
 * O código **é o próprio caminho**: ele é o id do documento, e ler exige conhecê-lo por inteiro.
 * Não há consulta que liste convites de ninguém, e é isso que faz uma coleção legível por qualquer
 * autenticado não ser um vazamento.
 *
 * **O código não é uma senha, e o desenho conta com isso.** Quem o digita não entra na carteira de
 * ninguém: cria um pedido, e o treinador confirma. É essa confirmação que carrega a segurança do
 * fluxo — um código repassado ao vizinho errado vira um pedido recusável, e não um aluno dentro.
 *
 * O código é **do treinador, e reaproveitável**: um só, o mesmo em todas as conversas, como um
 * código de indicação. Convite de uso único obrigaria a apagar o documento no momento do resgate, e
 * quem resgata não pode apagá-lo — as regras deixam isso com o dono. Fazer valer o uso único
 * exigiria uma Cloud Function, e ela não compraria nada que a confirmação já não garanta.
 *
 * @param trainerName nome do treinador no momento em que o código foi gerado. Viaja aqui porque é
 *   com ele que o aluno confere se digitou o código certo **antes** de pedir vínculo — e porque é
 *   daqui que o nome é copiado para o documento do vínculo. Ver [Link].
 */
data class InviteCode(val code: String, val trainerId: String, val trainerName: String = "")
