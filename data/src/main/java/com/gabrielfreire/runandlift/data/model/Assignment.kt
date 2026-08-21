package com.gabrielfreire.runandlift.data.model

/**
 * Documento `assignments/{trainerId}_{studentId}` — o treino que um aluno recebeu.
 *
 * **Carrega uma cópia congelada dos dias, e não uma referência ao programa.** Não é otimização: a
 * regra de `programs` é `allow read: if isSelf(resource.data.trainerId)`, ou seja, **o aluno não
 * consegue ler a coleção de programas**. Sem a cópia, ele não teria como ler o próprio treino. Três
 * consequências vêm junto, e todas são desejáveis:
 *
 * - **Ajuste individual sai de graça** — é o que o README promete. O treinador pode editar a cópia
 *   de um aluno sem tocar no molde que os outros usam.
 * - **Editar o programa não muda o treino de quem já está treinando** por efeito colateral.
 *   Prescrição é ato profissional; ela mudar sozinha na madrugada é o comportamento errado.
 * - **Apagar o programa não deixa ninguém sem treino.**
 *
 * O preço, que é real: corrigir um erro no molde **não** corrige quem já recebeu. Reatribuir é o que
 * atualiza.
 *
 * **O id é `{trainerId}_{studentId}`**, e daí decorre que um aluno tem **um treino por treinador**.
 * É o que o produto diz — "o treino do dia", no singular — e é o que evita a tela do aluno ter de
 * escolher entre três programas ativos. Atribuir outro programa substitui este documento; o
 * histórico do que foi *executado* vive em `sessions`, que não passa por aqui.
 *
 * @param studentName copiado para dentro, como em [Link] e pela mesma razão: `users/{uid}` só o
 *   titular lê, então sem a cópia a lista do treinador seria uma lista de identificadores.
 * @param programId de qual molde esta cópia veio. Serve para o treinador saber quem está com o quê;
 *   o aluno nunca o usa, porque não pode ler o molde.
 * @param days a cópia congelada. É isto que o aluno abre na academia.
 */
data class Assignment(
    val trainerId: String,
    val studentId: String,
    val studentName: String,
    val programId: String,
    val programName: String,
    val goal: TrainingGoal? = null,
    val notes: String? = null,
    val days: List<ProgramDay> = emptyList(),
    val status: AssignmentStatus = AssignmentStatus.ACTIVE,
    val updatedAt: Long = 0L,
) {

    /** O treino que o aluno está fazendo agora, e não um encerrado. */
    val isActive: Boolean get() = status == AssignmentStatus.ACTIVE

    /** Quantos exercícios o treino tem somando todos os dias. */
    val totalExercises: Int get() = days.sumOf { it.exercises.size }

    companion object {

        /**
         * O id do documento, sempre `{trainerId}_{studentId}`.
         *
         * Determinístico pela mesma razão de `LinkDocument.id`: Security Rule não consulta, só faz
         * `get()` por caminho exato. Aqui isso ainda não é usado por nenhuma regra, mas o formato
         * mantém a porta aberta — e resolve de graça o problema de não atribuir duas vezes.
         */
        fun id(trainerId: String, studentId: String): String = "${trainerId}_$studentId"

        /**
         * Um programa virando a prescrição de alguém.
         *
         * É aqui que a cópia congela. Depois disto, o molde e o treino do aluno são dois objetos
         * independentes.
         */
        fun from(program: Program, studentId: String, studentName: String): Assignment = Assignment(
            trainerId = program.trainerId,
            studentId = studentId,
            studentName = studentName,
            programId = program.id,
            programName = program.name,
            goal = program.goal,
            notes = program.notes,
            days = program.days,
        )
    }
}
