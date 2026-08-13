package com.gabrielfreire.runandlift.data.repository

/**
 * Desfecho de uma tentativa de sincronização do catálogo.
 *
 * É um valor de retorno, e não uma exceção, porque falha de rede é estado normal neste produto —
 * a regra 3 da camada de dados. Os três casos existem separados para que quem chamou possa medir a
 * economia: [AlreadyUpToDate] é a prova de que o orçamento de leitura (§2.4) está sendo respeitado.
 */
sealed interface CatalogSyncResult {

    /** A versão local já estava em dia. Nenhuma leitura do Firestore foi gasta. */
    data class AlreadyUpToDate(val version: Int) : CatalogSyncResult

    /** O catálogo foi baixado e gravado. */
    data class Updated(val version: Int, val exerciseCount: Int) : CatalogSyncResult

    /** A sincronização falhou. O catálogo local segue utilizável. */
    data class Failed(val cause: Throwable) : CatalogSyncResult
}
