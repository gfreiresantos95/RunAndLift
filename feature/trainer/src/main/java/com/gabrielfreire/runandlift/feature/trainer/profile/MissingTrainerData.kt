package com.gabrielfreire.runandlift.feature.trainer.profile

/**
 * O que ainda falta no perfil profissional do treinador.
 *
 * Um campo por pergunta, e não uma contagem, porque quem lê isto precisa saber **o quê** falta: o
 * aviso da home diz quantas coisas faltam, e a tela de edição abre já apontando para elas.
 *
 * Apresentação e capacidade só contam como faltando **se a vitrine estiver aceita**. Sem ela, o app
 * não pediu esses campos e não teria onde publicá-los — cobrá-los no aviso seria cobrar uma
 * resposta que a pessoa já recusou dar, o que é o oposto de consentimento.
 *
 * [showcase] falta enquanto o aceite não foi dado nesta versão do aviso. É o mesmo desenho do lado
 * do aluno, e vale a mesma ressalva: o aviso **convida**, não bloqueia, e quem não quer aparecer na
 * vitrine segue trabalhando com os alunos que já tem.
 *
 * O registro no CREF não está aqui: ele é conferido na conclusão de cadastro, que é onde a falta
 * dele de fato impede alguém de seguir — prescrever sem registro é o que a lei veda, e um aviso na
 * home seria brando demais para isso.
 */
internal data class MissingTrainerData(
    val experience: Boolean = false,
    val specialties: Boolean = false,
    val serviceModes: Boolean = false,
    val availableDays: Boolean = false,
    val bio: Boolean = false,
    val capacity: Boolean = false,
    /** O aceite da vitrine, que ainda não foi dado nesta versão do aviso. */
    val showcase: Boolean = false,
) {

    /** Quantas perguntas seguem sem resposta — é o número que o aviso da home mostra. */
    val count: Int
        get() = listOf(experience, specialties, serviceModes, availableDays, bio, capacity, showcase).count { it }

    /** Se há algo a completar. */
    val any: Boolean get() = count > 0
}
