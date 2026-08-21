package com.gabrielfreire.runandlift.data.program

import com.gabrielfreire.runandlift.data.model.Program

/**
 * Coleção `programs` — os moldes de treino que o treinador monta.
 *
 * **Não é cache-first, e a diferença com o catálogo é deliberada.** O catálogo de exercícios é lido
 * do Room porque o aluno precisa dele em pé na academia, sem sinal; um programa é montado sentado,
 * pelo treinador, e a persistência offline que o SDK do Firestore liga sozinha no Android já cobre
 * o caso de a rede cair no meio. Uma tabela do Room aqui custaria entidade, DAO, migração e
 * sincronização para atender a um cenário que não é o do produto.
 *
 * **Escrever exige rede.** A fila durável de escrita (E0-04, WorkManager) ainda não existe, então
 * salvar sem conexão falha — e falha com mensagem, nunca em silêncio. É a mesma condição de todas as
 * escritas do app hoje; a fila chega junto do registro de série, que é onde perder um dado é
 * inaceitável.
 */
interface ProgramRepository {

    /**
     * Os programas deste treinador, do mais recentemente mexido para o mais antigo.
     *
     * A ordenação acontece **no cliente**, e é uma escolha de custo: `whereEqualTo` mais `orderBy`
     * em campo diferente exigiria um índice composto no Firestore, e um treinador tem dezenas de
     * programas, não milhares. É a mesma decisão que `StudentsUiState` toma com a carteira.
     *
     * Custo declarado: 1 leitura por programa, no máximo [LIMIT].
     */
    suspend fun programs(trainerId: String): List<Program>

    /**
     * Um programa pelo id, ou `null` se não existir.
     *
     * Custo declarado: 1 leitura.
     */
    suspend fun program(programId: String): Program?

    /**
     * Cria ou atualiza, e devolve o programa como ele ficou — com id, se acabou de nascer.
     *
     * Devolve em vez de responder `Unit` porque o id do documento novo só existe depois da escrita,
     * e sem ele a tela não saberia o que abrir em seguida nem o que salvar da próxima vez.
     *
     * Custo declarado: 1 escrita, 0 leitura.
     */
    suspend fun save(program: Program): Program

    /**
     * Apaga o molde.
     *
     * **Não alcança quem já recebeu.** As atribuições carregam a própria cópia dos dias, então
     * apagar um programa não deixa nenhum aluno sem treino — é o outro lado de a cópia ser
     * congelada.
     *
     * Custo declarado: 1 escrita, 0 leitura.
     */
    suspend fun delete(programId: String)

    companion object {

        /**
         * Teto de programas por treinador numa listagem.
         *
         * Mesmo raciocínio do teto de `LinkRepository`: sem ele, o custo de abrir a aba cresce com o
         * tempo de uso do produto. Cinquenta é muito acima do que alguém mantém de fato.
         */
        const val LIMIT = 50L
    }
}
