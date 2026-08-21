package com.gabrielfreire.runandlift.feature.trainer.programeditor

import com.gabrielfreire.runandlift.data.model.Program

/**
 * Estado do editor de programa.
 *
 * @param loading verdadeiro enquanto o programa é lido. Um programa novo não passa por aqui: ele
 *   nasce pronto, em memória, e a tela abre no formulário vazio sem piscar um indicador.
 * @param notFound o id veio na rota e o documento não existe. Estado próprio, e não vazio: um
 *   editor em branco no lugar de um programa que sumiu faria o treinador remontá-lo por cima do
 *   nada, e o antigo continuaria por aí.
 * @param saveFailed a gravação não foi. **Salvar exige rede** — a fila durável de escrita (E0-04)
 *   ainda não existe —, então esta é a situação normal de quem está sem sinal, e a tela precisa
 *   dizer isso em vez de fechar como se tivesse dado certo.
 * @param saving trava o botão enquanto a escrita não volta, para dois toques não virarem dois
 *   documentos.
 */
internal data class ProgramEditorUiState(
    val loading: Boolean = false,
    val notFound: Boolean = false,
    val saving: Boolean = false,
    val saveFailed: Boolean = false,
    val program: Program = Program(id = "", trainerId = "", name = ""),
) {

    /**
     * Se dá para salvar agora.
     *
     * Só o nome é exigido, e é de propósito: **montar um programa leva dias**, e um app que se
     * recusa a guardar trabalho pela metade ensina a pessoa a não confiar nele. Dia vazio e programa
     * sem dia continuam sendo problema — mas na hora de atribuir a alguém, que é quando eles
     * realmente atrapalham. Ver `Program.isAssignable`.
     */
    val canSave: Boolean get() = program.name.isNotBlank() && !saving

    /** O aviso do que ainda falta para o programa poder ir para um aluno. */
    val incomplete: Boolean get() = !program.isAssignable

    /**
     * Se dá para abrir a tela de atribuição.
     *
     * Exige **programa já gravado** — a cópia que o aluno recebe sai do documento, não do rascunho —
     * e exige que ele esteja completo: nome, ao menos um dia, e nenhum dia vazio. Entregar um dia
     * sem exercício a alguém é entregar uma tela em branco na academia.
     */
    val canAssign: Boolean get() = program.id.isNotBlank() && program.isAssignable && !saving
}
